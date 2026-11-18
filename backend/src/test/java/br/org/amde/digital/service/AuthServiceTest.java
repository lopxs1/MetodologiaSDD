package br.org.amde.digital.service;

import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.domain.enums.AcaoAuditoria;
import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.dto.auth.LoginRequest;
import br.org.amde.digital.dto.auth.LoginResponse;
import br.org.amde.digital.dto.auth.UsuarioSessaoDTO;
import br.org.amde.digital.exception.CredenciaisInvalidasException;
import br.org.amde.digital.exception.ValidacaoCampoException;
import br.org.amde.digital.mapper.UsuarioMapper;
import br.org.amde.digital.repository.UsuarioRepository;
import br.org.amde.digital.security.JwtService;
import br.org.amde.digital.service.auditoria.AuditoriaService;
import br.org.amde.digital.service.auditoria.RecursoAuditoriaFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private AuditoriaService auditoriaService;

    @Spy
    private RecursoAuditoriaFormatter recursoFormatter = new RecursoAuditoriaFormatter();

    @InjectMocks
    private AuthService authService;

    @Test
    void deveAutenticarNormalizandoEmailEDisparandoAuditoriaLogin() {
        Usuario usuario = criarUsuario(10L, "admin@amde.example.org", "$2a$10$hash", NivelAcesso.ADMINISTRADOR);
        OffsetDateTime expiraEm = OffsetDateTime.now().plusHours(8);
        UsuarioSessaoDTO sessao = new UsuarioSessaoDTO(10L, "Admin", "admin@amde.example.org", NivelAcesso.ADMINISTRADOR);

        when(usuarioRepository.findByEmailIgnoreCase("admin@amde.example.org")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Senha@2026", "$2a$10$hash")).thenReturn(true);
        when(jwtService.gerarToken(usuario)).thenReturn(new JwtService.TokenGerado("jwt-token", expiraEm));
        when(usuarioMapper.toSessaoDTO(usuario)).thenReturn(sessao);

        LoginResponse response = authService.autenticar(new LoginRequest("  ADMIN@amde.example.org ", "Senha@2026"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tipo()).isEqualTo("Bearer");
        assertThat(response.expiraEm()).isEqualTo(expiraEm);
        assertThat(response.usuario()).isEqualTo(sessao);
        verify(auditoriaService).registrarAsync(10L, AcaoAuditoria.LOGIN, "usuario:10:LOGIN");
    }

    @Test
    void deveLancarCredenciaisInvalidasQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.findByEmailIgnoreCase("inexistente@amde.example.org")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.autenticar(new LoginRequest("inexistente@amde.example.org", "Senha@2026")))
                .isInstanceOf(CredenciaisInvalidasException.class);

        verify(auditoriaService, never()).registrarAsync(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void deveLancarCredenciaisInvalidasQuandoSenhaEstiverIncorreta() {
        Usuario usuario = criarUsuario(10L, "admin@amde.example.org", "$2a$10$hash", NivelAcesso.ADMINISTRADOR);
        when(usuarioRepository.findByEmailIgnoreCase("admin@amde.example.org")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "$2a$10$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.autenticar(new LoginRequest("admin@amde.example.org", "errada")))
                .isInstanceOf(CredenciaisInvalidasException.class);

        verify(jwtService, never()).gerarToken(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deveRejeitarEmailComFormatoInvalido() {
        assertThatThrownBy(() -> authService.autenticar(new LoginRequest("email-invalido", "Senha@2026")))
                .isInstanceOf(ValidacaoCampoException.class);
    }

    private Usuario criarUsuario(Long id, String email, String senha, NivelAcesso nivel) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome("Admin");
        u.setEmail(email);
        u.setSenha(senha);
        u.setNivelAcesso(nivel);
        return u;
    }
}
