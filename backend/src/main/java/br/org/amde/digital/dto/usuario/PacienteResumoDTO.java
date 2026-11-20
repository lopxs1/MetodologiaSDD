package br.org.amde.digital.dto.usuario;

import br.org.amde.digital.domain.enums.GrauSuporte;

public record PacienteResumoDTO(
        Long id,
        GrauSuporte grauSuporte,
        String descricao
) {
}
