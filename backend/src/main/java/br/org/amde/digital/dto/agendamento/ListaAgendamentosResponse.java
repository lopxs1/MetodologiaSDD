package br.org.amde.digital.dto.agendamento;

import java.util.List;

public record ListaAgendamentosResponse(
        List<AgendamentoResponse> itens,
        int pagina,
        int tamanho,
        long totalItens,
        int totalPaginas
) {
}

