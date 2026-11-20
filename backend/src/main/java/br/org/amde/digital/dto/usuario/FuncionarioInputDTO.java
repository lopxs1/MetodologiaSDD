package br.org.amde.digital.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FuncionarioInputDTO(
        @NotBlank(message = "Cargo é obrigatório para usuário MEDICO")
        @Size(min = 2, max = 80, message = "Cargo deve ter entre 2 e 80 caracteres")
        String cargo,

        @Size(min = 3, max = 30, message = "Registro profissional (CRM/conselho) deve ter entre 3 e 30 caracteres")
        String crm
) {
}
