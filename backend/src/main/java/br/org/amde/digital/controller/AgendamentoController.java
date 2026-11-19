package br.org.amde.digital.controller;

import br.org.amde.digital.domain.enums.StatusAgendamento;
import br.org.amde.digital.domain.enums.TipoAgendamento;
import br.org.amde.digital.dto.agendamento.AgendamentoResponse;
import br.org.amde.digital.dto.agendamento.CriarAgendamentoRequest;
import br.org.amde.digital.dto.agendamento.EditarAgendamentoRequest;
import br.org.amde.digital.dto.agendamento.ListaAgendamentosResponse;
import br.org.amde.digital.dto.agendamento.RegistrarPresencaRequest;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<AgendamentoResponse> criar(
            @Valid @RequestBody CriarAgendamentoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado autenticado
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamentoService.criar(request, autenticado));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO', 'PACIENTE')")
    public ResponseEntity<ListaAgendamentosResponse> listar(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dataFim,
            @RequestParam(required = false) StatusAgendamento status,
            @RequestParam(required = false) TipoAgendamento tipoAgendamento,
            @RequestParam(required = false) Long funcionarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dataInicio") String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            @AuthenticationPrincipal UsuarioAutenticado autenticado
    ) {
        return ResponseEntity.ok(agendamentoService.listar(
                termo,
                dataInicio,
                dataFim,
                status,
                tipoAgendamento,
                funcionarioId,
                page,
                size,
                sort,
                direction,
                autenticado
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO', 'PACIENTE')")
    public ResponseEntity<AgendamentoResponse> consultarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado autenticado
    ) {
        return ResponseEntity.ok(agendamentoService.consultarPorId(id, autenticado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<AgendamentoResponse> editar(
            @PathVariable Long id,
            @Valid @RequestBody EditarAgendamentoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado autenticado
    ) {
        return ResponseEntity.ok(agendamentoService.editar(id, request, autenticado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado autenticado
    ) {
        agendamentoService.cancelar(id, autenticado);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/presenca")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<AgendamentoResponse> registrarPresenca(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarPresencaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado autenticado
    ) {
        return ResponseEntity.ok(agendamentoService.registrarPresenca(id, request, autenticado));
    }

    @PatchMapping("/{id}/concluir")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<AgendamentoResponse> concluir(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado autenticado
    ) {
        return ResponseEntity.ok(agendamentoService.concluir(id, autenticado));
    }
}

