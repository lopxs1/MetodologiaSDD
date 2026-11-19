package br.org.amde.digital.service;

import br.org.amde.digital.dto.dashboard.ConsultaDiaDTO;
import br.org.amde.digital.dto.dashboard.ConsultaEspecialidadeDTO;
import br.org.amde.digital.dto.dashboard.DashboardContadoresDTO;
import br.org.amde.digital.dto.dashboard.DashboardResponse;
import br.org.amde.digital.exception.RecursoNaoEncontradoException;
import br.org.amde.digital.exception.ValidacaoCampoException;
import br.org.amde.digital.repository.DashboardRepository;
import br.org.amde.digital.repository.DashboardRepository.ContagemAtivos;
import br.org.amde.digital.repository.DashboardRepository.ContagemConsultas;
import br.org.amde.digital.repository.DashboardRepository.ContagemEspecialidade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class DashboardService {

    public static final ZoneId SP_ZONE = ZoneId.of("America/Sao_Paulo");

    private final DashboardRepository dashboardRepository;
    private final Clock clock;

    @Autowired
    public DashboardService(DashboardRepository dashboardRepository) {
        this(dashboardRepository, Clock.system(SP_ZONE));
    }

    public DashboardService(DashboardRepository dashboardRepository, Clock clock) {
        this.dashboardRepository = dashboardRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardResponse obterMetricas(LocalDate dataInicio, LocalDate dataFim, Long funcionarioId) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new ValidacaoCampoException("dataInicio", "A data inicial não pode ser posterior à data final");
        }
        if (funcionarioId != null && !dashboardRepository.existeFuncionario(funcionarioId)) {
            throw new RecursoNaoEncontradoException("Profissional não encontrado");
        }

        OffsetDateTime inicioPeriodo = dataInicio != null
                ? dataInicio.atStartOfDay(SP_ZONE).toOffsetDateTime()
                : null;
        OffsetDateTime fimPeriodo = dataFim != null
                ? dataFim.plusDays(1).atStartOfDay(SP_ZONE).toOffsetDateTime()
                : null;

        LocalDate hojeSp = LocalDate.now(clock.withZone(SP_ZONE));
        OffsetDateTime inicioHojeSp = hojeSp.atStartOfDay(SP_ZONE).toOffsetDateTime();
        OffsetDateTime fimHojeSp = hojeSp.plusDays(1).atStartOfDay(SP_ZONE).toOffsetDateTime();

        ContagemConsultas consultas = dashboardRepository.contarConsultas(
                inicioPeriodo,
                fimPeriodo,
                funcionarioId,
                inicioHojeSp,
                fimHojeSp
        );
        ContagemAtivos ativos = dashboardRepository.contarAtivos();
        List<ConsultaDiaDTO> serieTempo = dashboardRepository.buscarConsultasAoLongoDoTempo(
                inicioPeriodo,
                fimPeriodo,
                funcionarioId
        );
        List<ContagemEspecialidade> serieEspecialidadesBruta = dashboardRepository.buscarConsultasPorEspecialidade(
                inicioPeriodo,
                fimPeriodo,
                funcionarioId
        );

        long totalConsultas = consultas != null ? consultas.totalConsultas() : 0L;
        long consultasHoje = consultas != null ? consultas.consultasHoje() : 0L;
        long totalPacientes = ativos != null ? ativos.totalPacientes() : 0L;
        long profissionaisAtivos = ativos != null ? ativos.profissionaisAtivos() : 0L;

        List<ConsultaEspecialidadeDTO> serieEspecialidades = serieEspecialidadesBruta.stream()
                .map(item -> new ConsultaEspecialidadeDTO(
                        item.especialidade(),
                        item.quantidade(),
                        calcularPercentual(item.quantidade(), totalConsultas)
                ))
                .toList();

        DashboardContadoresDTO contadores = new DashboardContadoresDTO(
                totalConsultas,
                consultasHoje,
                totalPacientes,
                profissionaisAtivos
        );

        return new DashboardResponse(contadores, serieTempo, serieEspecialidades);
    }

    private double calcularPercentual(long quantidade, long totalConsultas) {
        if (totalConsultas <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(quantidade * 100.0 / totalConsultas)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}

