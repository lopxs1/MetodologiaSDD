package br.org.amde.digital.dto.paciente;

import br.org.amde.digital.domain.enums.GrauSuporte;

public record PacienteResumoResponse(
        Long id,
        Long usuarioId,
        String nome,
        String email,
        GrauSuporte grauSuporte,
        String numCelular
) {
}

