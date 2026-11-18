package br.org.amde.digital.dto.usuario;

import br.org.amde.digital.domain.enums.NivelAcesso;

import java.time.OffsetDateTime;

public record UsuarioResumoResponse(
        Long id,
        String nome,
        String email,
        NivelAcesso nivelAcesso,
        String numCelular,
        OffsetDateTime criadoEm
) {
}
