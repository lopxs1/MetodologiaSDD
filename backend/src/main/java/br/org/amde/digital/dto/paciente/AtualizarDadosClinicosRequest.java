package br.org.amde.digital.dto.paciente;

import br.org.amde.digital.domain.enums.GrauSuporte;
import jakarta.validation.constraints.NotNull;

public record AtualizarDadosClinicosRequest(
        @NotNull(message = "Grau de suporte é obrigatório")
        GrauSuporte grauSuporte,
        String descricao
) {
}

