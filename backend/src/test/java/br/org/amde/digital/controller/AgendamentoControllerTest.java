package br.org.amde.digital.controller;

import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.domain.enums.StatusAgendamento;
import br.org.amde.digital.domain.enums.TipoAgendamento;
import br.org.amde.digital.dto.agendamento.AgendamentoPacienteDTO;
import br.org.amde.digital.dto.agendamento.AgendamentoResponse;
import br.org.amde.digital.dto.agendamento.ListaAgendamentosResponse;
import br.org.amde.digital.exception.ApiExceptionHandler;
import br.org.amde.digital.exception.ConflitoException;
import br.org.amde.digital.repository.UsuarioRepository;
import br.org.amde.digital.security.JwtAuthenticationFilter;
import br.org.amde.digital.security.JwtService;
import br.org.amde.digital.security.SecurityConfig;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.AgendamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgendamentoController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class, JwtAuthenticationFilter.class})
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgendamentoService agendamentoService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveExigirAutenticacaoEBloquearEscritaParaPaciente() throws Exception {
        mockMvc.perform(get("/api/agendamentos"))
                .andExpect(status().isUnauthorized());

        Authentication pacAuth = auth(30L, NivelAcesso.PACIENTE);
        String payload = """
                {
                  "funcionarioId": 3,
                  "dataInicio": "2026-12-20T10:00:00-03:00",
                  "dataFim": "2026-12-20T11:00:00-03:00",
                  "tipoAgendamento": "CONSULTA",
                  "pacientesIds": [10]
                }
                """;

        mockMvc.perform(post("/api/agendamentos")
                        .with(authentication(pacAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/agendamentos/10")
                        .with(authentication(pacAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/agendamentos/10")
                        .with(authentication(pacAuth)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/agendamentos/10/presenca")
                        .with(authentication(pacAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"possuiPresencaFuncionario\":true,\"pacientes\":[]}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/agendamentos/10/concluir")
                        .with(authentication(pacAuth)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveCriarAgendamentoEMapearConflito409EValidacao400() throws Exception {
        Authentication medicoAuth = auth(20L, NivelAcesso.MEDICO);
        OffsetDateTime inicio = OffsetDateTime.parse("2026-12-20T10:00:00-03:00");
        OffsetDateTime fim = OffsetDateTime.parse("2026-12-20T11:00:00-03:00");

        AgendamentoResponse criado = new AgendamentoResponse(
                42L,
                3L,
                "Dr. Carlos",
                "Psicologia",
                inicio,
                fim,
                TipoAgendamento.CONSULTA,
                StatusAgendamento.AGENDADO,
                false,
                List.of(new AgendamentoPacienteDTO(10L, "Beatriz", false)),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(agendamentoService.criar(any(), any())).thenReturn(criado);

        String payloadValido = """
                {
                  "funcionarioId": 3,
                  "dataInicio": "2026-12-20T10:00:00-03:00",
                  "dataFim": "2026-12-20T11:00:00-03:00",
                  "tipoAgendamento": "CONSULTA",
                  "pacientesIds": [10]
                }
                """;

        mockMvc.perform(post("/api/agendamentos")
                        .with(authentication(medicoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadValido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.status").value("AGENDADO"));

        mockMvc.perform(post("/api/agendamentos")
                        .with(authentication(medicoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoAgendamento\":\"CONSULTA\",\"pacientesIds\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros").isArray());

        when(agendamentoService.criar(any(), any()))
                .thenThrow(new ConflitoException("Já existe um agendamento conflitante"));

        mockMvc.perform(post("/api/agendamentos")
                        .with(authentication(medicoAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadValido))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveListarConsultarECancelarComStatusCorreto() throws Exception {
        Authentication pacAuth = auth(30L, NivelAcesso.PACIENTE);
        Authentication adminAuth = auth(1L, NivelAcesso.ADMINISTRADOR);

        when(agendamentoService.listar(any(), any(), any(), any(), any(), any(), eq(0), eq(10), eq("dataInicio"), eq("ASC"), any()))
                .thenReturn(new ListaAgendamentosResponse(List.of(), 0, 10, 0L, 0));

        mockMvc.perform(get("/api/agendamentos").with(authentication(pacAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isArray());

        doNothing().when(agendamentoService).cancelar(eq(42L), any());

        mockMvc.perform(delete("/api/agendamentos/42").with(authentication(adminAuth)))
                .andExpect(status().isNoContent());
    }

    private Authentication auth(Long id, NivelAcesso perfil) {
        UsuarioAutenticado principal = new UsuarioAutenticado(id, "Teste", "teste@amde.org.br", perfil);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()))
        );
    }
}

