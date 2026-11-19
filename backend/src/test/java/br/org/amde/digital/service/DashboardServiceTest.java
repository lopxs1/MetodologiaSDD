package br.org.amde.digital.service;

import br.org.amde.digital.dto.dashboard.ConsultaDiaDTO;
import br.org.amde.digital.dto.dashboard.DashboardResponse;
import br.org.amde.digital.exception.RecursoNaoEncontradoException;
import br.org.amde.digital.exception.ValidacaoCampoException;
import br.org.amde.digital.repository.DashboardRepository;
import br.org.amde.digital.repository.DashboardRepository.ContagemAtivos;
import br.org.amde.digital.repository.DashboardRepository.ContagemConsultas;
import br.org.amde.digital.repository.DashboardRepository.ContagemEspecialidade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @Test
    void deveRejeitarPeriodoInvertidoComValidacaoCampoException() {
        DashboardService service = new DashboardService(dashboardRepository);
        LocalDate inicio = LocalDate.of(2026, 11, 20);
        LocalDate fim = LocalDate.of(2026, 11, 10);

        assertThatThrownBy(() -> service.obterMetricas(inicio, fim, null))
                .isInstanceOf(ValidacaoCampoException.class)
                .hasMessageContaining("data inicial");

        verifyNoInteractions(dashboardRepository);
    }

    @Test
    void deveLancarRecursoNaoEncontradoQuandoFuncionarioIdNaoExiste() {
        DashboardService service = new DashboardService(dashboardRepository);
        when(dashboardRepository.existeFuncionario(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.obterMetricas(null, null, 999L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Profissional");
    }

    @Test
    void deveCalcularLimitesNoFusoAmericaSaoPauloAs23h30ECalcularPercentuais() {
        Clock clock23h30Sp = Clock.fixed(Instant.parse("2026-11-19T02:30:00Z"), DashboardService.SP_ZONE);
        DashboardService service = new DashboardService(dashboardRepository, clock23h30Sp);

        LocalDate filtroInicio = LocalDate.of(2026, 11, 1);
        LocalDate filtroFim = LocalDate.of(2026, 11, 18);

        when(dashboardRepository.existeFuncionario(7L)).thenReturn(true);
        when(dashboardRepository.contarConsultas(any(), any(), eq(7L), any(), any()))
                .thenReturn(new ContagemConsultas(10L, 3L));
        when(dashboardRepository.contarAtivos())
                .thenReturn(new ContagemAtivos(5L, 4L));
        when(dashboardRepository.buscarConsultasAoLongoDoTempo(any(), any(), eq(7L)))
                .thenReturn(List.of(
                        new ConsultaDiaDTO(LocalDate.of(2026, 11, 10), 4L),
                        new ConsultaDiaDTO(LocalDate.of(2026, 11, 18), 6L)
                ));
        when(dashboardRepository.buscarConsultasPorEspecialidade(any(), any(), eq(7L)))
                .thenReturn(List.of(
                        new ContagemEspecialidade("Psicologia", 4L),
                        new ContagemEspecialidade("Fonoaudiologia", 3L),
                        new ContagemEspecialidade("Terapia Ocupacional", 3L)
                ));

        DashboardResponse response = service.obterMetricas(filtroInicio, filtroFim, 7L);

        ArgumentCaptor<OffsetDateTime> inicioPeriodoCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> fimPeriodoCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> inicioHojeCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> fimHojeCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        verify(dashboardRepository).contarConsultas(
                inicioPeriodoCaptor.capture(),
                fimPeriodoCaptor.capture(),
                eq(7L),
                inicioHojeCaptor.capture(),
                fimHojeCaptor.capture()
        );

        assertThat(inicioPeriodoCaptor.getValue())
                .isEqualTo(OffsetDateTime.parse("2026-11-01T00:00:00-03:00"));
        assertThat(fimPeriodoCaptor.getValue())
                .isEqualTo(OffsetDateTime.parse("2026-11-19T00:00:00-03:00"));
        assertThat(inicioHojeCaptor.getValue())
                .isEqualTo(OffsetDateTime.parse("2026-11-18T00:00:00-03:00"));
        assertThat(fimHojeCaptor.getValue())
                .isEqualTo(OffsetDateTime.parse("2026-11-19T00:00:00-03:00"));

        assertThat(response.contadores().totalConsultas()).isEqualTo(10L);
        assertThat(response.contadores().consultasHoje()).isEqualTo(3L);
        assertThat(response.contadores().totalPacientes()).isEqualTo(5L);
        assertThat(response.contadores().profissionaisAtivos()).isEqualTo(4L);
        assertThat(response.consultasPorEspecialidade())
                .extracting("especialidade", "quantidade", "percentual")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Psicologia", 4L, 40.0),
                        org.assertj.core.groups.Tuple.tuple("Fonoaudiologia", 3L, 30.0),
                        org.assertj.core.groups.Tuple.tuple("Terapia Ocupacional", 3L, 30.0)
                );
    }

    @Test
    void deveRetornarContadoresZeradosESeriesVaziasQuandoNaoHaConsultas() {
        DashboardService service = new DashboardService(dashboardRepository);
        when(dashboardRepository.contarConsultas(any(), any(), eq(null), any(), any()))
                .thenReturn(new ContagemConsultas(0L, 0L));
        when(dashboardRepository.contarAtivos())
                .thenReturn(new ContagemAtivos(0L, 0L));
        when(dashboardRepository.buscarConsultasAoLongoDoTempo(any(), any(), eq(null)))
                .thenReturn(List.of());
        when(dashboardRepository.buscarConsultasPorEspecialidade(any(), any(), eq(null)))
                .thenReturn(List.of());

        DashboardResponse response = service.obterMetricas(null, null, null);

        assertThat(response.contadores().totalConsultas()).isZero();
        assertThat(response.contadores().consultasHoje()).isZero();
        assertThat(response.contadores().totalPacientes()).isZero();
        assertThat(response.contadores().profissionaisAtivos()).isZero();
        assertThat(response.consultasAoLongoDoTempo()).isEmpty();
        assertThat(response.consultasPorEspecialidade()).isEmpty();
    }
}

