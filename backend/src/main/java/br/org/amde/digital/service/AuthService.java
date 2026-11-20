package br.org.amde.digital.service;

import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.domain.enums.AcaoAuditoria;
import br.org.amde.digital.dto.auth.LoginRequest;
import br.org.amde.digital.dto.auth.LoginResponse;
import br.org.amde.digital.exception.CredenciaisInvalidasException;
import br.org.amde.digital.exception.ValidacaoCampoException;
import br.org.amde.digital.mapper.UsuarioMapper;
import br.org.amde.digital.repository.UsuarioRepository;
import br.org.amde.digital.security.JwtService;
import br.org.amde.digital.service.auditoria.AuditoriaService;
import br.org.amde.digital.service.auditoria.RecursoAuditoriaFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;
    private final AuditoriaService auditoriaService;
    private final RecursoAuditoriaFormatter recursoFormatter;

    @Transactional(readOnly = true)
    public LoginResponse autenticar(LoginRequest request) {
        String emailNormalizado = request.email().trim().toLowerCase(Locale.ROOT);
        if (emailNormalizado.length() < 5
                || emailNormalizado.length() > 254
                || !EMAIL_PATTERN.matcher(emailNormalizado).matches()) {
            throw new ValidacaoCampoException("email", "Formato de e-mail inválido");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        JwtService.TokenGerado tokenGerado = jwtService.gerarToken(usuario);
        auditoriaService.registrarAsync(
                usuario.getId(),
                AcaoAuditoria.LOGIN,
                recursoFormatter.formatarLogin(usuario.getId())
        );

        return new LoginResponse(
                tokenGerado.token(),
                "Bearer",
                tokenGerado.expiraEm(),
                usuarioMapper.toSessaoDTO(usuario)
        );
    }
}
