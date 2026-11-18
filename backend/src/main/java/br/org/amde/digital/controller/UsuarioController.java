package br.org.amde.digital.controller;

import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.dto.usuario.AtualizarUsuarioRequest;
import br.org.amde.digital.dto.usuario.CadastrarUsuarioRequest;
import br.org.amde.digital.dto.usuario.ListaUsuariosResponse;
import br.org.amde.digital.dto.usuario.UsuarioDetalheResponse;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioDetalheResponse> cadastrar(@Valid @RequestBody CadastrarUsuarioRequest request) {
        UsuarioDetalheResponse criado = usuarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    public ResponseEntity<ListaUsuariosResponse> listar(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) NivelAcesso nivelAcesso,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        return ResponseEntity.ok(usuarioService.listar(termo, nivelAcesso, page, size, sort, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDetalheResponse> consultarPorId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean revelarCelular
    ) {
        return ResponseEntity.ok(usuarioService.consultarPorId(id, revelarCelular));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDetalheResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarUsuarioRequest request
    ) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado autenticado
    ) {
        usuarioService.excluirLogicamente(id, autenticado.id());
        return ResponseEntity.noContent().build();
    }
}
