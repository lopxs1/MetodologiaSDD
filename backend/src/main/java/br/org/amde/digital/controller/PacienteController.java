package br.org.amde.digital.controller;

import br.org.amde.digital.domain.enums.GrauSuporte;
import br.org.amde.digital.dto.paciente.AtualizarDadosClinicosRequest;
import br.org.amde.digital.dto.paciente.ListaPacientesResponse;
import br.org.amde.digital.dto.paciente.PacienteFichaResponse;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<ListaPacientesResponse> listar(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) GrauSuporte grauSuporte,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        return ResponseEntity.ok(pacienteService.listar(termo, grauSuporte, page, size, sort, direction));
    }

    @GetMapping("/meu-prontuario")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<PacienteFichaResponse> consultarMeuProntuario(
            @AuthenticationPrincipal UsuarioAutenticado autenticado,
            @RequestParam(defaultValue = "0") int docPage,
            @RequestParam(defaultValue = "10") int docSize
    ) {
        return ResponseEntity.ok(pacienteService.buscarMeuProntuario(autenticado, docPage, docSize));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO', 'PACIENTE')")
    public ResponseEntity<PacienteFichaResponse> consultarFicha(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado autenticado,
            @RequestParam(defaultValue = "false") boolean revelarCelular,
            @RequestParam(defaultValue = "0") int docPage,
            @RequestParam(defaultValue = "10") int docSize
    ) {
        return ResponseEntity.ok(pacienteService.buscarFicha(id, autenticado, revelarCelular, docPage, docSize));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<PacienteFichaResponse> atualizarDadosClinicos(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarDadosClinicosRequest request
    ) {
        return ResponseEntity.ok(pacienteService.atualizarDadosClinicos(id, request));
    }
}

