package br.org.amde.digital.dto.erro;

import java.time.OffsetDateTime;

public record ErroResponse(
        int status,
        String mensagem,
        OffsetDateTime timestamp
) {
}
