package br.org.amde.digital.dto.agendamento;

import jakarta.validation.constraints.NotNull;

public record PresencaPacienteInputDTO(
        @NotNull(message = "ID do paciente é obrigatório")
        Long pacienteId,
        @NotNull(message = "Presença do paciente é obrigatória")
        Boolean possuiPresenca
) {
}

