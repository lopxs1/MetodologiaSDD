package br.org.amde.digital.dto.paciente;

import java.util.List;

public record ListaPacientesResponse(
        List<PacienteResumoResponse> itens,
        int pagina,
        int tamanho,
        long totalItens,
        int totalPaginas
) {
}

