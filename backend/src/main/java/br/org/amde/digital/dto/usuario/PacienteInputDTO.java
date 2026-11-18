package br.org.amde.digital.dto.usuario;

import br.org.amde.digital.domain.enums.GrauSuporte;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PacienteInputDTO(
        @NotNull(message = "Grau de suporte é obrigatório para usuário PACIENTE")
        GrauSuporte grauSuporte,

        @Size(max = 5000, message = "Descrição clínica deve ter no máximo 5000 caracteres")
        String descricao
) {
}
