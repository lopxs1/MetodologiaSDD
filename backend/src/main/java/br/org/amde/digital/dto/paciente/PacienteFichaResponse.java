package br.org.amde.digital.dto.paciente;

import br.org.amde.digital.domain.enums.GrauSuporte;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record PacienteFichaResponse(
        Long id,
        Long usuarioId,
        String nome,
        String email,
        LocalDate dataNascimento,
        String numCelular,
        boolean celularMascarado,
        String pais,
        String estado,
        String cidade,
        String rua,
        GrauSuporte grauSuporte,
        String descricao,
        List<CategoriaHistoricoDTO> historicoPorCategoria,
        OffsetDateTime atualizadoEm
) {
}

