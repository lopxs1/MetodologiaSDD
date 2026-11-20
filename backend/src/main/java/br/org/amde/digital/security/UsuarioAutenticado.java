package br.org.amde.digital.security;

import br.org.amde.digital.domain.enums.NivelAcesso;

public record UsuarioAutenticado(
        Long id,
        String nome,
        String email,
        NivelAcesso nivelAcesso
) {
}
