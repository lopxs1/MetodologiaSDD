package br.org.amde.digital.dto.agendamento;

public record AgendamentoPacienteDTO(
        Long pacienteId,
        String nomePaciente,
        boolean possuiPresenca
) {
}

