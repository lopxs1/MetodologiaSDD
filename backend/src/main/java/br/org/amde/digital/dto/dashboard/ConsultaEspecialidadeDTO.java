package br.org.amde.digital.dto.dashboard;

public record ConsultaEspecialidadeDTO(
        String especialidade,
        long quantidade,
        double percentual
) {
}

