package br.org.amde.digital.dto.agendamento;

import br.org.amde.digital.domain.enums.StatusAgendamento;
import br.org.amde.digital.domain.enums.TipoAgendamento;

import java.time.OffsetDateTime;
import java.util.List;

public record AgendamentoResponse(
        Long id,
        Long funcionarioId,
        String nomeProfissional,
        String cargoProfissional,
        OffsetDateTime dataInicio,
        OffsetDateTime dataFim,
        TipoAgendamento tipoAgendamento,
        StatusAgendamento status,
        boolean possuiPresencaFuncionario,
        List<AgendamentoPacienteDTO> pacientes,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}

