package br.org.amde.digital.dto.auth;

import java.time.OffsetDateTime;

public record LoginResponse(
        String token,
        String tipo,
        OffsetDateTime expiraEm,
        UsuarioSessaoDTO usuario
) {
}
