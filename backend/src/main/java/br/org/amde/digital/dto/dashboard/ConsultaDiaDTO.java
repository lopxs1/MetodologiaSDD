package br.org.amde.digital.dto.dashboard;

import java.time.LocalDate;

public record ConsultaDiaDTO(
        LocalDate data,
        long quantidade
) {
}

