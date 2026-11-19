package br.org.amde.digital.controller;

import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.dto.dashboard.ConsultaDiaDTO;
import br.org.amde.digital.dto.dashboard.ConsultaEspecialidadeDTO;
import br.org.amde.digital.dto.dashboard.DashboardContadoresDTO;
import br.org.amde.digital.dto.dashboard.DashboardResponse;
import br.org.amde.digital.exception.ApiExceptionHandler;
import br.org.amde.digital.exception.RecursoNaoEncontradoException;
import br.org.amde.digital.exception.ValidacaoCampoException;
import br.org.amde.digital.repository.UsuarioRepository;
import br.org.amde.digital.security.JwtAuthenticationFilter;
import br.org.amde.digital.security.JwtService;
import br.org.amde.digital.security.SecurityConfig;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class, JwtAuthenticationFilter.class})
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveExigirAutenticacaoEBloquearMedicoEPacienteCom403() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/dashboard").with(authentication(auth(20L, NivelAcesso.MEDICO))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/dashboard").with(authentication(auth(30L, NivelAcesso.PACIENTE))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornarMetricasParaAdministradorComESemFiltros() throws Exception {
        DashboardResponse resposta = new DashboardResponse(
                new DashboardContadoresDTO(10L, 3L, 5L, 4L),
                List.of(new ConsultaDiaDTO(LocalDate.of(2026, 11, 18), 10L)),
                List.of(new ConsultaEspecialidadeDTO("Psicologia", 10L, 100.0))
        );

        when(dashboardService.obterMetricas(null, null, null)).thenReturn(resposta);
        when(dashboardService.obterMetricas(
                eq(LocalDate.of(2026, 11, 1)),
                eq(LocalDate.of(2026, 11, 18)),
                eq(3L)
        )).thenReturn(resposta);

        Authentication adminAuth = auth(1L, NivelAcesso.ADMINISTRADOR);

        mockMvc.perform(get("/api/dashboard").with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contadores.totalConsultas").value(10))
                .andExpect(jsonPath("$.contadores.consultasHoje").value(3))
                .andExpect(jsonPath("$.contadores.totalPacientes").value(5))
                .andExpect(jsonPath("$.contadores.profissionaisAtivos").value(4))
                .andExpect(jsonPath("$.consultasAoLongoDoTempo[0].data").value("2026-11-18"))
                .andExpect(jsonPath("$.consultasAoLongoDoTempo[0].quantidade").value(10))
                .andExpect(jsonPath("$.consultasPorEspecialidade[0].especialidade").value("Psicologia"))
                .andExpect(jsonPath("$.consultasPorEspecialidade[0].percentual").value(100.0));

        mockMvc.perform(get("/api/dashboard")
                        .param("dataInicio", "2026-11-01")
                        .param("dataFim", "2026-11-18")
                        .param("funcionarioId", "3")
                        .with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contadores.totalConsultas").value(10));
    }

    @Test
    void deveMapear400ParaPeriodoInvalidoE404ParaProfissionalInexistente() throws Exception {
        Authentication adminAuth = auth(1L, NivelAcesso.ADMINISTRADOR);

        when(dashboardService.obterMetricas(
                eq(LocalDate.of(2026, 11, 20)),
                eq(LocalDate.of(2026, 11, 10)),
                eq(null)
        )).thenThrow(new ValidacaoCampoException("dataInicio", "A data inicial não pode ser posterior à data final"));

        mockMvc.perform(get("/api/dashboard")
                        .param("dataInicio", "2026-11-20")
                        .param("dataFim", "2026-11-10")
                        .with(authentication(adminAuth)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("dataInicio"));

        mockMvc.perform(get("/api/dashboard")
                        .param("dataInicio", "data-invalida")
                        .with(authentication(adminAuth)))
                .andExpect(status().isBadRequest());

        when(dashboardService.obterMetricas(null, null, 999L))
                .thenThrow(new RecursoNaoEncontradoException("Profissional não encontrado"));

        mockMvc.perform(get("/api/dashboard")
                        .param("funcionarioId", "999")
                        .with(authentication(adminAuth)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
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

