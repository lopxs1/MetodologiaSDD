package br.org.amde.digital.controller;

import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.domain.enums.SituacaoDocumento;
import br.org.amde.digital.dto.prontuario.ProntuarioResponse;
import br.org.amde.digital.exception.ApiExceptionHandler;
import br.org.amde.digital.exception.ErroStorageException;
import br.org.amde.digital.repository.UsuarioRepository;
import br.org.amde.digital.security.JwtAuthenticationFilter;
import br.org.amde.digital.security.JwtService;
import br.org.amde.digital.security.SecurityConfig;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.ProntuarioService;
import br.org.amde.digital.service.ProntuarioService.DownloadProntuarioResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProntuarioController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class, JwtAuthenticationFilter.class})
class ProntuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProntuarioService prontuarioService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveRetornar401QuandoNaoAutenticadoEBloquearEscritaParaPaciente() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "laudo.pdf", "application/pdf", "%PDF-1.7".getBytes(StandardCharsets.US_ASCII)
        );

        mockMvc.perform(get("/api/prontuarios/10/download"))
                .andExpect(status().isUnauthorized());

        Authentication pacAuth = auth(25L, NivelAcesso.PACIENTE);

        mockMvc.perform(multipart("/api/prontuarios/upload")
                        .file(arquivo)
                        .param("pacienteId", "12")
                        .param("tituloDocumento", "Laudo")
                        .param("categoria", "Laudos")
                        .with(authentication(pacAuth)))
                .andExpect(status().isForbidden());

        mockMvc.perform(multipart("/api/prontuarios/10/substituir")
                        .file(arquivo)
                        .with(authentication(pacAuth)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/prontuarios/10/categoria")
                        .with(authentication(pacAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\":\"Arquivado\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/prontuarios/10").with(authentication(pacAuth)))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirUploadSubstituicaoReclassificacaoDownloadEExclusaoParaPerfisAutorizados() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "laudo-2026.pdf", "application/pdf", "%PDF-1.7".getBytes(StandardCharsets.US_ASCII)
        );
        ProntuarioResponse resp = new ProntuarioResponse(
                105L, 12L, "Laudos", null, "Laudo neuropsicológico", "laudo-2026.pdf",
                SituacaoDocumento.VIGENTE, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(prontuarioService.enviar(eq(12L), eq("Laudo neuropsicológico"), eq("Laudos"), eq(null), any(), any()))
                .thenReturn(resp);
        when(prontuarioService.substituir(eq(105L), eq("Laudo v2"), any(), any()))
                .thenReturn(new ProntuarioResponse(
                        106L, 12L, "Laudos", 105L, "Laudo v2", "laudo-2026.pdf",
                        SituacaoDocumento.VIGENTE, OffsetDateTime.now(), OffsetDateTime.now()
                ));
        when(prontuarioService.reclassificarCategoria(eq(105L), any()))
                .thenReturn(new ProntuarioResponse(
                        105L, 12L, "Arquivado", null, "Laudo neuropsicológico", "laudo-2026.pdf",
                        SituacaoDocumento.ARQUIVADO, OffsetDateTime.now(), OffsetDateTime.now()
                ));

        Authentication medico = auth(2L, NivelAcesso.MEDICO);

        mockMvc.perform(multipart("/api/prontuarios/upload")
                        .file(arquivo)
                        .param("pacienteId", "12")
                        .param("tituloDocumento", "Laudo neuropsicológico")
                        .param("categoria", "Laudos")
                        .with(authentication(medico)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(105L))
                .andExpect(content().string(not(containsString("nomeArquivoArmazenado"))));

        mockMvc.perform(multipart("/api/prontuarios/105/substituir")
                        .file(arquivo)
                        .param("tituloDocumento", "Laudo v2")
                        .with(authentication(medico)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registroSubstituidoId").value(105L));

        mockMvc.perform(patch("/api/prontuarios/105/categoria")
                        .with(authentication(medico))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\":\"Arquivado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ARQUIVADO"));

        byte[] pdfBytes = "%PDF-1.7\nconteudo".getBytes(StandardCharsets.US_ASCII);
        when(prontuarioService.prepararDownload(eq(105L), any()))
                .thenReturn(new DownloadProntuarioResult(new ByteArrayResource(pdfBytes), "laudo-2026.pdf"));

        mockMvc.perform(get("/api/prontuarios/105/download").with(authentication(auth(25L, NivelAcesso.PACIENTE))))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("laudo-2026.pdf")))
                .andExpect(content().bytes(pdfBytes));

        when(prontuarioService.prepararDownload(eq(999L), any()))
                .thenThrow(new ErroStorageException("Caminho secreto /uploads/interno/xyz.pdf"));
        mockMvc.perform(get("/api/prontuarios/999/download").with(authentication(medico)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(not(containsString("/uploads/interno"))));

        mockMvc.perform(delete("/api/prontuarios/105").with(authentication(auth(1L, NivelAcesso.ADMINISTRADOR))))
                .andExpect(status().isNoContent());
    }

    private Authentication auth(Long usuarioId, NivelAcesso nivel) {
        UsuarioAutenticado principal = new UsuarioAutenticado(
                usuarioId, "Usuario Teste", "teste" + usuarioId + "@amde.example.org", nivel
        );
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + nivel.name()))
        );
    }
}

