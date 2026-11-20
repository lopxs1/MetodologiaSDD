package br.org.amde.digital.dto.usuario;

import br.org.amde.digital.domain.enums.NivelAcesso;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AtualizarUsuarioRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        String email,

        String senha,

        NivelAcesso nivelAcesso,

        LocalDate dataNascimento,

        @Pattern(regexp = "^[0-9]{10,11}$", message = "Celular deve conter apenas 10 ou 11 dígitos numéricos")
        String numCelular,

        @Size(min = 2, max = 60, message = "País deve ter entre 2 e 60 caracteres")
        String pais,

        @Pattern(regexp = "^[A-Z]{2}$", message = "Estado deve ser uma sigla UF válida com 2 letras maiúsculas")
        String estado,

        @Size(min = 2, max = 100, message = "Cidade deve ter entre 2 e 100 caracteres")
        String cidade,

        @Size(min = 2, max = 150, message = "Rua deve ter entre 2 e 150 caracteres")
        String rua,

        @Valid
        FuncionarioInputDTO funcionario,

        @Valid
        PacienteInputDTO paciente
) {
}
