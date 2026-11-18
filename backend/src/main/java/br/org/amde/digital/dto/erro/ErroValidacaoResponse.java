package br.org.amde.digital.dto.erro;

import java.time.OffsetDateTime;
import java.util.List;

public record ErroValidacaoResponse(
        int status,
        String mensagem,
        List<CampoErroDTO> erros,
        OffsetDateTime timestamp
) {
}
