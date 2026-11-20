package br.org.amde.digital.dto.auth;

import br.org.amde.digital.domain.enums.NivelAcesso;

public record UsuarioSessaoDTO(
        Long id,
        String nome,
        String email,
        NivelAcesso nivelAcesso
) {
}
