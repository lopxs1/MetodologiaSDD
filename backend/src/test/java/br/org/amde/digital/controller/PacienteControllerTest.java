package br.org.amde.digital.controller;

import br.org.amde.digital.domain.enums.GrauSuporte;
import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.dto.paciente.ListaPacientesResponse;
import br.org.amde.digital.dto.paciente.PacienteFichaResponse;
import br.org.amde.digital.dto.paciente.PacienteResumoResponse;
import br.org.amde.digital.exception.ApiExceptionHandler;
import br.org.amde.digital.exception.RecursoNaoEncontradoException;
import br.org.amde.digital.repository.UsuarioRepository;
import br.org.amde.digital.security.JwtAuthenticationFilter;
import br.org.amde.digital.security.JwtService;
import br.org.amde.digital.security.SecurityConfig;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PacienteController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class, JwtAuthenticationFilter.class})
class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PacienteService pacienteService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveRetornar401QuandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveListarParaMedicoSemExporDescricaoClinicaNoJson() throws Exception {
        ListaPacientesResponse resposta = new ListaPacientesResponse(
                List.of(new PacienteResumoResponse(
                        12L, 25L, "José Lucas", "jose@amde.example.org", GrauSuporte.NIVEL_2, "(15) *****-0011"
                )),
                0, 10, 1L, 1
        );
        when(pacienteService.listar(eq("jose"), eq(GrauSuporte.NIVEL_2), eq(0), eq(10), eq("nome"), eq("ASC")))
                .thenReturn(resposta);

        mockMvc.perform(get("/api/pacientes")
                        .with(authentication(auth(2L, NivelAcesso.MEDICO)))
                        .param("termo", "jose")
                        .param("grauSuporte", "NIVEL_2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].nome").value("José Lucas"))
                .andExpect(jsonPath("$.itens[0].numCelular").value("(15) *****-0011"))
                .andExpect(content().string(not(containsString("descricao"))));
    }

    @Test
    void deveBloquearPacienteEmListagemFichaAlheiaEAtualizacao() throws Exception {
        when(pacienteService.buscarFicha(eq(999L), any(), eq(false), eq(0), eq(10)))
                .thenThrow(new AccessDeniedException("Paciente não pode acessar ficha de outro paciente"));

        Authentication pacAuth = auth(25L, NivelAcesso.PACIENTE);

        mockMvc.perform(get("/api/pacientes").with(authentication(pacAuth)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/pacientes/999").with(authentication(pacAuth)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/pacientes/12")
                        .with(authentication(pacAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grauSuporte\":\"NIVEL_2\",\"descricao\":\"Teste\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirMeuProntuarioParaPacienteEAtualizacaoParaMedico() throws Exception {
        PacienteFichaResponse ficha = new PacienteFichaResponse(
                12L, 25L, "José Lucas", "jose@amde.example.org", null,
                "(15) *****-0011", true, "Brasil", "SP", "Sorocaba", null,
                GrauSuporte.NIVEL_2, "Observação clínica", List.of(), OffsetDateTime.now()
        );
        when(pacienteService.buscarMeuProntuario(any(), eq(0), eq(10))).thenReturn(ficha);
        when(pacienteService.atualizarDadosClinicos(eq(12L), any())).thenReturn(ficha);
        when(pacienteService.atualizarDadosClinicos(eq(404L), any()))
                .thenThrow(new RecursoNaoEncontradoException("Paciente não encontrado"));

        mockMvc.perform(get("/api/pacientes/meu-prontuario").with(authentication(auth(25L, NivelAcesso.PACIENTE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.celularMascarado").value(true));

        mockMvc.perform(put("/api/pacientes/12")
                        .with(authentication(auth(2L, NivelAcesso.MEDICO)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grauSuporte\":\"NIVEL_2\",\"descricao\":\"Observação clínica\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grauSuporte").value("NIVEL_2"));

        mockMvc.perform(put("/api/pacientes/12")
                        .with(authentication(auth(2L, NivelAcesso.MEDICO)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Sem grau\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/pacientes/404")
                        .with(authentication(auth(2L, NivelAcesso.ADMINISTRADOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grauSuporte\":\"NIVEL_1\"}"))
                .andExpect(status().isNotFound());
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

