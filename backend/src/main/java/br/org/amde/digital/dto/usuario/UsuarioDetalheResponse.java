package br.org.amde.digital.dto.usuario;

import br.org.amde.digital.domain.enums.NivelAcesso;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record UsuarioDetalheResponse(
        Long id,
        String nome,
        String email,
        NivelAcesso nivelAcesso,
        LocalDate dataNascimento,
        String numCelular,
        boolean celularMascarado,
        String pais,
        String estado,
        String cidade,
        String rua,
        FuncionarioResumoDTO funcionario,
        PacienteResumoDTO paciente,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
