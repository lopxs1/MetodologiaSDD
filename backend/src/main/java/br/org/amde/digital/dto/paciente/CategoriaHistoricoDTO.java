package br.org.amde.digital.dto.paciente;

import java.util.List;

public record CategoriaHistoricoDTO(
        String categoria,
        long totalDocumentos,
        int pagina,
        int tamanho,
        int totalPaginas,
        List<DocumentoHistoricoResumoDTO> documentos
) {
}

