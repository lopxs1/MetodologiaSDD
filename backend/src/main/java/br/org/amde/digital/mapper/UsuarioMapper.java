package br.org.amde.digital.mapper;

import br.org.amde.digital.domain.entity.Funcionario;
import br.org.amde.digital.domain.entity.Paciente;
import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.dto.auth.UsuarioSessaoDTO;
import br.org.amde.digital.dto.usuario.FuncionarioResumoDTO;
import br.org.amde.digital.dto.usuario.PacienteResumoDTO;
import br.org.amde.digital.dto.usuario.UsuarioDetalheResponse;
import br.org.amde.digital.dto.usuario.UsuarioResumoResponse;
import br.org.amde.digital.service.usuario.CelularMasker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioSessaoDTO toSessaoDTO(Usuario usuario);

    FuncionarioResumoDTO toFuncionarioResumo(Funcionario funcionario);

    PacienteResumoDTO toPacienteResumo(Paciente paciente);

    @Mapping(target = "numCelular", expression = "java(celularMasker.mascarar(usuario.getNumCelular()))")
    UsuarioResumoResponse toResumoResponse(Usuario usuario, CelularMasker celularMasker);

    default UsuarioDetalheResponse toDetalheResponse(
            Usuario usuario,
            boolean revelarCelular,
            CelularMasker celularMasker
    ) {
        String rawCelular = usuario.getNumCelular();
        boolean temCelular = rawCelular != null && !rawCelular.isBlank();
        String celularFormatado = !temCelular
                ? null
                : (revelarCelular ? celularMasker.formatarCompleto(rawCelular) : celularMasker.mascarar(rawCelular));
        boolean celularMascarado = temCelular && !revelarCelular;

        return new UsuarioDetalheResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getNivelAcesso(),
                usuario.getDataNascimento(),
                celularFormatado,
                celularMascarado,
                usuario.getPais(),
                usuario.getEstado(),
                usuario.getCidade(),
                usuario.getRua(),
                usuario.getFuncionario() != null ? toFuncionarioResumo(usuario.getFuncionario()) : null,
                usuario.getPaciente() != null ? toPacienteResumo(usuario.getPaciente()) : null,
                usuario.getCriadoEm(),
                usuario.getAtualizadoEm()
        );
    }
}
