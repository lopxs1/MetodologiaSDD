package br.org.amde.digital.dto.usuario;

import java.util.List;

public record ListaUsuariosResponse(
        List<UsuarioResumoResponse> itens,
        int pagina,
        int tamanho,
        long totalItens,
        int totalPaginas,
        TotalizadoresPerfilDTO totalizadores
) {
}
