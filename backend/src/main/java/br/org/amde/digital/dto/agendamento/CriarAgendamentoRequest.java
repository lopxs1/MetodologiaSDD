package br.org.amde.digital.dto.agendamento;

import br.org.amde.digital.domain.enums.TipoAgendamento;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public record CriarAgendamentoRequest(
        Long funcionarioId,
        @NotNull(message = "A data de início é obrigatória")
        OffsetDateTime dataInicio,
        @NotNull(message = "A data de fim é obrigatória")
        OffsetDateTime dataFim,
        @NotNull(message = "O tipo de agendamento é obrigatório")
        TipoAgendamento tipoAgendamento,
        @NotEmpty(message = "Informe pelo menos um paciente participante")
        List<@NotNull(message = "ID do paciente não pode ser nulo") Long> pacientesIds
) {
}

