package br.org.amde.digital.dto.agendamento;

import jakarta.validation.Valid;

import java.util.List;

public record RegistrarPresencaRequest(
        Boolean possuiPresencaFuncionario,
        List<@Valid PresencaPacienteInputDTO> pacientes
) {
}

