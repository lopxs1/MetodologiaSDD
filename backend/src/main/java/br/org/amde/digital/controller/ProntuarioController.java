package br.org.amde.digital.controller;

import br.org.amde.digital.dto.prontuario.ProntuarioResponse;
import br.org.amde.digital.dto.prontuario.ReclassificarCategoriaRequest;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.ProntuarioService;
import br.org.amde.digital.service.ProntuarioService.DownloadProntuarioResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/prontuarios")
@RequiredArgsConstructor
public class ProntuarioController {

    private final ProntuarioService prontuarioService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<ProntuarioResponse> upload(
            @RequestParam("pacienteId") Long pacienteId,
            @RequestParam("tituloDocumento") String tituloDocumento,
            @RequestParam("categoria") String categoria,
            @RequestParam(value = "registroSubstituidoId", required = false) Long registroSubstituidoId,
            @RequestPart("arquivo") MultipartFile arquivo,
            @AuthenticationPrincipal UsuarioAutenticado autenticado
    ) {
        ProntuarioResponse criado = prontuarioService.enviar(
                pacienteId,
                tituloDocumento,
                categoria,
                registroSubstituidoId,
                arquivo,
                autenticado
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PostMapping(value = "/{id}/substituir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<ProntuarioResponse> substituir(
            @PathVariable Long id,
            @RequestParam(value = "tituloDocumento", required = false) String tituloDocumento,
            @RequestPart("arquivo") MultipartFile arquivo,
            @AuthenticationPrincipal UsuarioAutenticado autenticado
    ) {
        ProntuarioResponse substituido = prontuarioService.substituir(id, tituloDocumento, arquivo, autenticado);
        return ResponseEntity.status(HttpStatus.CREATED).body(substituido);
    }

    @PatchMapping("/{id}/categoria")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<ProntuarioResponse> reclassificarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody ReclassificarCategoriaRequest request
    ) {
        return ResponseEntity.ok(prontuarioService.reclassificarCategoria(id, request));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO', 'PACIENTE')")
    public ResponseEntity<Resource> download(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado autenticado
    ) {
        DownloadProntuarioResult resultado = prontuarioService.prepararDownload(id, autenticado);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(resultado.nomeArquivoOriginal(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resultado.recurso());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado autenticado
    ) {
        prontuarioService.excluir(id, autenticado);
        return ResponseEntity.noContent().build();
    }
}

