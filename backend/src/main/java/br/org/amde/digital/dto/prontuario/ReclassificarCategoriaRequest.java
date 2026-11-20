package br.org.amde.digital.dto.prontuario;

import jakarta.validation.constraints.NotBlank;

public record ReclassificarCategoriaRequest(
        @NotBlank(message = "Categoria é obrigatória")
        String categoria
) {
}

