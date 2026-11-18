package br.org.amde.digital.controller;

import br.org.amde.digital.dto.auth.LoginRequest;
import br.org.amde.digital.dto.auth.LoginResponse;
import br.org.amde.digital.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint publico de autenticacao da plataforma AMDE Digital (RF-01, UC01-UC03).
 */
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> autenticarUsuario(@Valid @RequestBody LoginRequest credenciais) {
        LoginResponse sessao = authService.autenticar(credenciais);
        return ResponseEntity.status(HttpStatus.OK).body(sessao);
    }
}
