package br.org.amde.digital.service;

import br.org.amde.digital.domain.entity.Funcionario;
import br.org.amde.digital.domain.entity.Paciente;
import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.domain.enums.AcaoAuditoria;
import br.org.amde.digital.domain.enums.GrauSuporte;
import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.domain.enums.StatusAgendamento;
import br.org.amde.digital.dto.usuario.AtualizarUsuarioRequest;
import br.org.amde.digital.dto.usuario.CadastrarUsuarioRequest;
import br.org.amde.digital.dto.usuario.FuncionarioInputDTO;
import br.org.amde.digital.dto.usuario.FuncionarioResumoDTO;
import br.org.amde.digital.dto.usuario.PacienteInputDTO;
import br.org.amde.digital.dto.usuario.PacienteResumoDTO;
import br.org.amde.digital.dto.usuario.UsuarioDetalheResponse;
import br.org.amde.digital.exception.ConflitoException;
import br.org.amde.digital.exception.ValidacaoCampoException;
import br.org.amde.digital.mapper.UsuarioMapper;
import br.org.amde.digital.repository.AgendamentoRepository;
import br.org.amde.digital.repository.FuncionarioRepository;
import br.org.amde.digital.repository.PacienteRepository;
import br.org.amde.digital.repository.UsuarioRepository;
import br.org.amde.digital.service.auditoria.AuditoriaService;
import br.org.amde.digital.service.auditoria.RecursoAuditoriaFormatter;
import br.org.amde.digital.service.usuario.CelularMasker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Spy
    private CelularMasker celularMasker = new CelularMasker();

    @Mock
    private AuditoriaService auditoriaService;

    @Spy
    private RecursoAuditoriaFormatter recursoFormatter = new RecursoAuditoriaFormatter();

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveCadastrarMedicoComEspecializacaoFuncionario() {
        CadastrarUsuarioRequest request = new CadastrarUsuarioRequest(
                "Dra. Helena Silva",
                "helena@amde.example.org",
                "Senha@2026",
                NivelAcesso.MEDICO,
                null,
                "15999991234",
                null,
                "SP",
                "Sorocaba",
                null,
                new FuncionarioInputDTO("Psicologia", "CRP/SP 06/12345"),
                null
        );

        when(usuarioRepository.existsByEmailIgnoreCase("helena@amde.example.org")).thenReturn(false);
        when(passwordEncoder.encode("Senha@2026")).thenReturn("hash-bcrypt");
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(20L);
            return u;
        });
        when(funcionarioRepository.saveAndFlush(any(Funcionario.class))).thenAnswer(inv -> {
            Funcionario f = inv.getArgument(0);
            f.setId(30L);
            return f;
        });
        when(usuarioMapper.toDetalheResponse(any(Usuario.class), eq(false), any(CelularMasker.class)))
                .thenAnswer(inv -> {
                    Usuario u = inv.getArgument(0);
                    return new UsuarioDetalheResponse(
                            u.getId(), u.getNome(), u.getEmail(), u.getNivelAcesso(),
                            u.getDataNascimento(), "(15) *****-1234", true,
                            u.getPais(), u.getEstado(), u.getCidade(), u.getRua(),
                            new FuncionarioResumoDTO(u.getFuncionario().getId(), u.getFuncionario().getCargo(), u.getFuncionario().getCrm()),
                            null, OffsetDateTime.now(), OffsetDateTime.now()
                    );
                });

        UsuarioDetalheResponse response = usuarioService.cadastrar(request);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.funcionario()).isNotNull();
        assertThat(response.funcionario().cargo()).isEqualTo("Psicologia");
        assertThat(response.pais()).isEqualTo("Brasil");
        verify(funcionarioRepository).saveAndFlush(any(Funcionario.class));
    }

    @Test
    void deveCadastrarPacienteComEspecializacaoPaciente() {
        CadastrarUsuarioRequest request = new CadastrarUsuarioRequest(
                "Lucas Mendes",
                "lucas@amde.example.org",
                "Senha@2026",
                NivelAcesso.PACIENTE,
                null,
                null,
                "Brasil",
                null,
                null,
                null,
                null,
                new PacienteInputDTO(GrauSuporte.NIVEL_2, "Acompanhamento inicial")
        );

        when(usuarioRepository.existsByEmailIgnoreCase("lucas@amde.example.org")).thenReturn(false);
        when(passwordEncoder.encode("Senha@2026")).thenReturn("hash-bcrypt");
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(21L);
            return u;
        });
        when(pacienteRepository.saveAndFlush(any(Paciente.class))).thenAnswer(inv -> {
            Paciente p = inv.getArgument(0);
            p.setId(40L);
            return p;
        });
        when(usuarioMapper.toDetalheResponse(any(Usuario.class), eq(false), any(CelularMasker.class)))
                .thenAnswer(inv -> {
                    Usuario u = inv.getArgument(0);
                    return new UsuarioDetalheResponse(
                            u.getId(), u.getNome(), u.getEmail(), u.getNivelAcesso(),
                            null, null, false,
                            u.getPais(), null, null, null,
                            null,
                            new PacienteResumoDTO(u.getPaciente().getId(), u.getPaciente().getGrauSuporte(), u.getPaciente().getDescricao()),
                            OffsetDateTime.now(), OffsetDateTime.now()
                    );
                });

        UsuarioDetalheResponse response = usuarioService.cadastrar(request);

        assertThat(response.id()).isEqualTo(21L);
        assertThat(response.paciente()).isNotNull();
        assertThat(response.paciente().grauSuporte()).isEqualTo(GrauSuporte.NIVEL_2);
        verify(pacienteRepository).saveAndFlush(any(Paciente.class));
    }

    @Test
    void deveRejeitarCadastroComEmailDuplicado() {
        CadastrarUsuarioRequest request = new CadastrarUsuarioRequest(
                "Admin Novo", "duplicado@amde.example.org", "Senha@2026",
                NivelAcesso.ADMINISTRADOR, null, null, null, null, null, null, null, null
        );
        when(usuarioRepository.existsByEmailIgnoreCase("duplicado@amde.example.org")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar(request))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void deveValidarRegrasDeSenhaRn21() {
        CadastrarUsuarioRequest curta = new CadastrarUsuarioRequest(
                "Admin", "a@amde.org", "1234567",
                NivelAcesso.ADMINISTRADOR, null, null, null, null, null, null, null, null
        );
        CadastrarUsuarioRequest apenasEspacos = new CadastrarUsuarioRequest(
                "Admin", "a@amde.org", "        ",
                NivelAcesso.ADMINISTRADOR, null, null, null, null, null, null, null, null
        );
        CadastrarUsuarioRequest acima72Bytes = new CadastrarUsuarioRequest(
                "Admin", "a@amde.org", "ç".repeat(37),
                NivelAcesso.ADMINISTRADOR, null, null, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> usuarioService.cadastrar(curta)).isInstanceOf(ValidacaoCampoException.class);
        assertThatThrownBy(() -> usuarioService.cadastrar(apenasEspacos)).isInstanceOf(ValidacaoCampoException.class);
        assertThatThrownBy(() -> usuarioService.cadastrar(acima72Bytes)).isInstanceOf(ValidacaoCampoException.class);
    }

    @Test
    void deveBloquearAlteracaoDePerfilNaAtualizacao() {
        Usuario existente = new Usuario();
        existente.setId(5L);
        existente.setNivelAcesso(NivelAcesso.MEDICO);

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(existente));

        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
                "Nome Atualizado",
                "medico@amde.example.org",
                null,
                NivelAcesso.ADMINISTRADOR,
                null, null, null, null, null, null,
                new FuncionarioInputDTO("Fonoaudiologia", null),
                null
        );

        assertThatThrownBy(() -> usuarioService.atualizar(5L, request))
                .isInstanceOf(ValidacaoCampoException.class);
    }

    @Test
    void deveBloquearAutoExclusao() {
        assertThatThrownBy(() -> usuarioService.excluirLogicamente(1L, 1L))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void deveBloquearExclusaoQuandoHouverAgendamentoAtivo() {
        Usuario medico = new Usuario();
        medico.setId(2L);
        medico.setNivelAcesso(NivelAcesso.MEDICO);
        Funcionario func = new Funcionario();
        func.setId(12L);
        medico.setFuncionario(func);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(medico));
        when(agendamentoRepository.existsByFuncionarioIdAndStatus(12L, StatusAgendamento.AGENDADO)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.excluirLogicamente(2L, 1L))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void deveExcluirLogicamenteEmCascataEDispararAuditoria() {
        Usuario pacienteUser = new Usuario();
        pacienteUser.setId(3L);
        pacienteUser.setNivelAcesso(NivelAcesso.PACIENTE);
        Paciente paciente = new Paciente();
        paciente.setId(15L);
        pacienteUser.setPaciente(paciente);

        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(pacienteUser));
        when(agendamentoRepository.existsByPacienteIdAndStatus(15L, StatusAgendamento.AGENDADO)).thenReturn(false);

        usuarioService.excluirLogicamente(3L, 1L);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(usuarioCaptor.capture());
        verify(pacienteRepository).save(paciente);

        assertThat(usuarioCaptor.getValue().getExcluidoEm()).isNotNull();
        assertThat(paciente.getExcluidoEm()).isNotNull();
        verify(auditoriaService).registrarAsync(1L, AcaoAuditoria.EXCLUSAO, "usuario:3:EXCLUSAO");
    }
}
