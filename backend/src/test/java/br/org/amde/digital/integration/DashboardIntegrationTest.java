package br.org.amde.digital.integration;

import br.org.amde.digital.domain.entity.Agendamento;
import br.org.amde.digital.domain.entity.Funcionario;
import br.org.amde.digital.domain.entity.Paciente;
import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.domain.enums.GrauSuporte;
import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.domain.enums.StatusAgendamento;
import br.org.amde.digital.domain.enums.TipoAgendamento;
import br.org.amde.digital.repository.AgendamentoRepository;
import br.org.amde.digital.repository.FuncionarioRepository;
import br.org.amde.digital.repository.PacienteRepository;
import br.org.amde.digital.repository.UsuarioRepository;
import br.org.amde.digital.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardIntegrationTest {

    private static final ZoneId SP_ZONE = ZoneId.of("America/Sao_Paulo");

    @DynamicPropertySource
    static void configurarBanco(DynamicPropertyRegistry registry) {
        String testUrl = System.getenv("TEST_DB_URL");
        if (testUrl == null || testUrl.isBlank()) {
            String port = System.getenv("DB_PORT");
            if (port == null || port.isBlank()) {
                port = "5433";
            }
            testUrl = "jdbc:postgresql://localhost:" + port + "/amde_digital?currentSchema=amde_digital";
        }
        final String jdbcUrl = testUrl;
        registry.add("spring.datasource.url", () -> jdbcUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final List<Long> usuariosCriadosIds = new ArrayList<>();
    private final List<Long> funcionariosCriadosIds = new ArrayList<>();
    private final List<Long> pacientesCriadosIds = new ArrayList<>();

    private Usuario adminUser;
    private Usuario medicoPsi1User;
    private Usuario medicoPsi2InativoUser;
    private Usuario medicoFonoUser;
    private Usuario medicoToUser;
    private Usuario pac1User;
    private Funcionario funcPsi1;
    private Funcionario funcPsi2Inativo;
    private Funcionario funcFono;
    private Funcionario funcTo;
    private String tokenAdmin;
    private String tokenMedico;
    private String tokenPaciente;

    @BeforeEach
    void prepararDados() {
        String hash = passwordEncoder.encode("Senha@2026");
        String sufixo = UUID.randomUUID().toString().substring(0, 8);

        adminUser = criarUsuario("Admin Dash " + sufixo, "admin.dash." + sufixo + "@amde.example.org", hash, NivelAcesso.ADMINISTRADOR);
        medicoPsi1User = criarUsuario("Dra. Clara Psi " + sufixo, "clara.dash." + sufixo + "@amde.example.org", hash, NivelAcesso.MEDICO);
        medicoPsi2InativoUser = criarUsuario("Dr. Bruno Inativo " + sufixo, "bruno.dash." + sufixo + "@amde.example.org", hash, NivelAcesso.MEDICO);
        medicoFonoUser = criarUsuario("Dra. Sofia Fono " + sufixo, "sofia.dash." + sufixo + "@amde.example.org", hash, NivelAcesso.MEDICO);
        medicoToUser = criarUsuario("Dr. Rafael TO " + sufixo, "rafael.dash." + sufixo + "@amde.example.org", hash, NivelAcesso.MEDICO);

        funcPsi1 = criarFuncionario(medicoPsi1User, "Psicologia");
        funcPsi2Inativo = criarFuncionario(medicoPsi2InativoUser, " psicologia ");
        funcFono = criarFuncionario(medicoFonoUser, "Fonoaudiologia");
        funcTo = criarFuncionario(medicoToUser, "Terapia Ocupacional");

        inativarFuncionario(funcPsi2Inativo.getId(), medicoPsi2InativoUser.getId());

        pac1User = criarUsuario("Lucas Paciente " + sufixo, "lucas.dash." + sufixo + "@amde.example.org", hash, NivelAcesso.PACIENTE);
        Usuario pac2User = criarUsuario("Marina Paciente " + sufixo, "marina.dash." + sufixo + "@amde.example.org", hash, NivelAcesso.PACIENTE);
        Usuario pac3InativoUser = criarUsuario("Pedro Inativo " + sufixo, "pedro.dash." + sufixo + "@amde.example.org", hash, NivelAcesso.PACIENTE);

        criarPaciente(pac1User, GrauSuporte.NIVEL_1);
        criarPaciente(pac2User, GrauSuporte.NIVEL_2);
        Paciente pac3Inativo = criarPaciente(pac3InativoUser, GrauSuporte.NIVEL_3);
        inativarPaciente(pac3Inativo.getId(), pac3InativoUser.getId());

        tokenAdmin = jwtService.gerarToken(adminUser).token();
        tokenMedico = jwtService.gerarToken(medicoPsi1User).token();
        tokenPaciente = jwtService.gerarToken(pac1User).token();
    }

    @AfterEach
    void limparDados() {
        if (!funcionariosCriadosIds.isEmpty()) {
            String ph = placeholders(funcionariosCriadosIds.size());
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.agendamento_paciente WHERE agendamento_id IN "
                            + "(SELECT id FROM amde_digital.agendamento WHERE funcionario_id IN (" + ph + "))",
                    funcionariosCriadosIds.toArray()
            );
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.agendamento WHERE funcionario_id IN (" + ph + ")",
                    funcionariosCriadosIds.toArray()
            );
        }
        if (!pacientesCriadosIds.isEmpty()) {
            String ph = placeholders(pacientesCriadosIds.size());
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.agendamento_paciente WHERE paciente_id IN (" + ph + ")",
                    pacientesCriadosIds.toArray()
            );
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.paciente WHERE id IN (" + ph + ")",
                    pacientesCriadosIds.toArray()
            );
        }
        if (!usuariosCriadosIds.isEmpty()) {
            String ph = placeholders(usuariosCriadosIds.size());
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.log_auditoria WHERE usuario_id IN (" + ph + ")",
                    usuariosCriadosIds.toArray()
            );
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.funcionario WHERE usuario_id IN (" + ph + ")",
                    usuariosCriadosIds.toArray()
            );
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.usuario WHERE id IN (" + ph + ")",
                    usuariosCriadosIds.toArray()
            );
        }
        funcionariosCriadosIds.clear();
        pacientesCriadosIds.clear();
        usuariosCriadosIds.clear();
    }

    @Test
    void deveCalcularContadoresSeriesEspecialidadesNormalizadasEPreservarHistoricoInativado() throws Exception {
        LocalDate hoje = LocalDate.now(SP_ZONE);
        LocalDate ontem = hoje.minusDays(1);
        LocalDate anteontem = hoje.minusDays(2);

        criarAgendamento(funcPsi1, hoje.atTime(23, 30).atZone(SP_ZONE).toOffsetDateTime(), TipoAgendamento.CONSULTA, StatusAgendamento.AGENDADO);
        criarAgendamento(funcFono, hoje.atTime(10, 0).atZone(SP_ZONE).toOffsetDateTime(), TipoAgendamento.EXAME, StatusAgendamento.CONCLUIDO);
        criarAgendamento(funcFono, hoje.atTime(14, 0).atZone(SP_ZONE).toOffsetDateTime(), TipoAgendamento.RETORNO, StatusAgendamento.CANCELADO);

        criarAgendamento(funcPsi2Inativo, ontem.atTime(9, 0).atZone(SP_ZONE).toOffsetDateTime(), TipoAgendamento.TERAPIA, StatusAgendamento.CONCLUIDO);
        criarAgendamento(funcPsi2Inativo, ontem.atTime(15, 0).atZone(SP_ZONE).toOffsetDateTime(), TipoAgendamento.AVALIACAO, StatusAgendamento.AGENDADO);

        criarAgendamento(funcPsi1, anteontem.atTime(9, 0).atZone(SP_ZONE).toOffsetDateTime(), TipoAgendamento.CONSULTA, StatusAgendamento.AGENDADO);
        criarAgendamento(funcFono, anteontem.atTime(11, 0).atZone(SP_ZONE).toOffsetDateTime(), TipoAgendamento.TERAPIA, StatusAgendamento.CONCLUIDO);
        criarAgendamento(funcTo, anteontem.atTime(13, 0).atZone(SP_ZONE).toOffsetDateTime(), TipoAgendamento.TERAPIA, StatusAgendamento.CONCLUIDO);

        MvcResult result = mockMvc.perform(get("/api/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contadores.totalConsultas").value(7))
                .andExpect(jsonPath("$.contadores.consultasHoje").value(2))
                .andExpect(jsonPath("$.contadores.totalPacientes").value(2))
                .andExpect(jsonPath("$.contadores.profissionaisAtivos").value(3))
                .andExpect(jsonPath("$.consultasAoLongoDoTempo", hasSize(3)))
                .andExpect(jsonPath("$.consultasAoLongoDoTempo[0].data").value(anteontem.toString()))
                .andExpect(jsonPath("$.consultasAoLongoDoTempo[0].quantidade").value(3))
                .andExpect(jsonPath("$.consultasAoLongoDoTempo[1].data").value(ontem.toString()))
                .andExpect(jsonPath("$.consultasAoLongoDoTempo[1].quantidade").value(2))
                .andExpect(jsonPath("$.consultasAoLongoDoTempo[2].data").value(hoje.toString()))
                .andExpect(jsonPath("$.consultasAoLongoDoTempo[2].quantidade").value(2))
                .andExpect(jsonPath("$.consultasPorEspecialidade", hasSize(3)))
                .andExpect(jsonPath("$.consultasPorEspecialidade[0].especialidade").value("Psicologia"))
                .andExpect(jsonPath("$.consultasPorEspecialidade[0].quantidade").value(4))
                .andExpect(jsonPath("$.consultasPorEspecialidade[0].percentual").value(57.14))
                .andExpect(jsonPath("$.consultasPorEspecialidade[1].especialidade").value("Fonoaudiologia"))
                .andExpect(jsonPath("$.consultasPorEspecialidade[1].quantidade").value(2))
                .andExpect(jsonPath("$.consultasPorEspecialidade[2].especialidade").value("Terapia Ocupacional"))
                .andExpect(jsonPath("$.consultasPorEspecialidade[2].quantidade").value(1))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThat(json)
                .doesNotContain(pac1User.getNome())
                .doesNotContain(pac1User.getEmail())
                .doesNotContain(medicoPsi1User.getEmail())
                .doesNotContain("NIVEL_1");

        JsonNode root = objectMapper.readTree(json);
        assertSomaIgualTotalConsultas(root, 7L);

        // Novo agendamento reflete imediatamente no recarregamento (Cenário 9) e desempate alfabético (RN51)
        criarAgendamento(funcTo, hoje.atTime(16, 0).atZone(SP_ZONE).toOffsetDateTime(), TipoAgendamento.CONSULTA, StatusAgendamento.AGENDADO);

        MvcResult atualizado = mockMvc.perform(get("/api/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contadores.totalConsultas").value(8))
                .andExpect(jsonPath("$.contadores.consultasHoje").value(3))
                .andExpect(jsonPath("$.consultasPorEspecialidade[1].especialidade").value("Fonoaudiologia"))
                .andExpect(jsonPath("$.consultasPorEspecialidade[1].quantidade").value(2))
                .andExpect(jsonPath("$.consultasPorEspecialidade[2].especialidade").value("Terapia Ocupacional"))
                .andExpect(jsonPath("$.consultasPorEspecialidade[2].quantidade").value(2))
                .andReturn();

        assertSomaIgualTotalConsultas(objectMapper.readTree(atualizado.getResponse().getContentAsString()), 8L);
    }

    @Test
    void deveAplicarFiltrosPeriodoEProfissionalERetornarZerosSemConsultas() throws Exception {
        LocalDate hoje = LocalDate.now(SP_ZONE);
        LocalDate ontem = hoje.minusDays(1);

        criarAgendamento(funcPsi1, hoje.atTime(11, 0).atZone(SP_ZONE).toOffsetDateTime(), TipoAgendamento.CONSULTA, StatusAgendamento.AGENDADO);
        criarAgendamento(funcPsi2Inativo, ontem.atTime(10, 0).atZone(SP_ZONE).toOffsetDateTime(), TipoAgendamento.TERAPIA, StatusAgendamento.CONCLUIDO);
        criarAgendamento(funcPsi2Inativo, ontem.minusDays(10).atTime(10, 0).atZone(SP_ZONE).toOffsetDateTime(), TipoAgendamento.TERAPIA, StatusAgendamento.CONCLUIDO);

        MvcResult filtrado = mockMvc.perform(get("/api/dashboard")
                        .param("dataInicio", ontem.toString())
                        .param("dataFim", hoje.toString())
                        .param("funcionarioId", funcPsi2Inativo.getId().toString())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contadores.totalConsultas").value(1))
                .andExpect(jsonPath("$.contadores.consultasHoje").value(0))
                .andExpect(jsonPath("$.consultasAoLongoDoTempo", hasSize(1)))
                .andExpect(jsonPath("$.consultasAoLongoDoTempo[0].data").value(ontem.toString()))
                .andExpect(jsonPath("$.consultasAoLongoDoTempo[0].quantidade").value(1))
                .andExpect(jsonPath("$.consultasPorEspecialidade", hasSize(1)))
                .andExpect(jsonPath("$.consultasPorEspecialidade[0].especialidade").value("Psicologia"))
                .andExpect(jsonPath("$.consultasPorEspecialidade[0].percentual").value(100.0))
                .andReturn();

        assertSomaIgualTotalConsultas(objectMapper.readTree(filtrado.getResponse().getContentAsString()), 1L);

        mockMvc.perform(get("/api/dashboard")
                        .param("dataInicio", hoje.plusDays(30).toString())
                        .param("dataFim", hoje.plusDays(40).toString())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contadores.totalConsultas").value(0))
                .andExpect(jsonPath("$.contadores.consultasHoje").value(0))
                .andExpect(jsonPath("$.consultasAoLongoDoTempo", hasSize(0)))
                .andExpect(jsonPath("$.consultasPorEspecialidade", hasSize(0)));

        mockMvc.perform(get("/api/dashboard")
                        .param("dataInicio", hoje.plusDays(1).toString())
                        .param("dataFim", hoje.toString())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/dashboard")
                        .param("funcionarioId", "9999999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBloquearAcessoDeMedicoEPacienteCom403ESemAuthCom401() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedico))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPaciente))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveAtenderMetaDeDesempenhoP95MenorQue500MsCom10kAgendamentosE1kPacientes() throws Exception {
        String prefixoBench = "bench." + UUID.randomUUID().toString().substring(0, 8) + ".";
        String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

        try {
            jdbcTemplate.update(
                    """
                    WITH novos_usuarios AS (
                        INSERT INTO amde_digital.usuario (nome, email, senha, nivel_acesso)
                        SELECT
                            'Paciente Carga ' || gs,
                            ? || gs || '@amde.example.org',
                            ?,
                            'PACIENTE'
                        FROM generate_series(1, 1000) AS gs
                        RETURNING id
                    )
                    INSERT INTO amde_digital.paciente (usuario_id, grau_suporte)
                    SELECT id, 'NIVEL_1'
                    FROM novos_usuarios
                    """,
                    prefixoBench,
                    hash
            );

            jdbcTemplate.update(
                    """
                    INSERT INTO amde_digital.agendamento (
                        funcionario_id, data_inicio, data_fim, tipo_agendamento, status, possui_presenca_funcionario
                    )
                    SELECT
                        CASE (gs % 3)
                            WHEN 0 THEN ?
                            WHEN 1 THEN ?
                            ELSE ?
                        END,
                        (now() - ((gs % 60) || ' days')::interval) - ((gs % 8) || ' hours')::interval,
                        (now() - ((gs % 60) || ' days')::interval) - ((gs % 8) || ' hours')::interval
                            + interval '45 minutes',
                        'CONSULTA',
                        CASE WHEN gs % 2 = 0 THEN 'CONCLUIDO' ELSE 'AGENDADO' END,
                        (gs % 2 = 0)
                    FROM generate_series(1, 10000) AS gs
                    """,
                    funcPsi1.getId(),
                    funcFono.getId(),
                    funcTo.getId()
            );

            List<Long> duracoesMs = new ArrayList<>();
            JsonNode ultimaResposta = null;

            for (int i = 0; i < 20; i++) {
                long inicioNs = System.nanoTime();
                MvcResult res = mockMvc.perform(get("/api/dashboard")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                        .andExpect(status().isOk())
                        .andReturn();
                long duracaoMs = (System.nanoTime() - inicioNs) / 1_000_000L;
                duracoesMs.add(duracaoMs);
                if (i == 0) {
                    ultimaResposta = objectMapper.readTree(res.getResponse().getContentAsString());
                }
            }

            Collections.sort(duracoesMs);
            int indiceP95 = (int) Math.ceil(duracoesMs.size() * 0.95) - 1;
            long p95Ms = duracoesMs.get(indiceP95);

            assertThat(p95Ms).isLessThan(500L);
            assertThat(ultimaResposta).isNotNull();
            assertThat(ultimaResposta.get("contadores").get("totalConsultas").asLong()).isEqualTo(10000L);
            assertThat(ultimaResposta.get("contadores").get("totalPacientes").asLong()).isGreaterThanOrEqualTo(1000L);
            assertSomaIgualTotalConsultas(ultimaResposta, 10000L);
        } finally {
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.paciente WHERE usuario_id IN "
                            + "(SELECT id FROM amde_digital.usuario WHERE email LIKE ?)",
                    prefixoBench + "%"
            );
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.usuario WHERE email LIKE ?",
                    prefixoBench + "%"
            );
        }
    }

    private void assertSomaIgualTotalConsultas(JsonNode root, long esperado) {
        long somaTempo = 0L;
        for (JsonNode ponto : root.get("consultasAoLongoDoTempo")) {
            somaTempo += ponto.get("quantidade").asLong();
        }
        long somaEspecialidades = 0L;
        for (JsonNode esp : root.get("consultasPorEspecialidade")) {
            somaEspecialidades += esp.get("quantidade").asLong();
        }
        assertThat(root.get("contadores").get("totalConsultas").asLong()).isEqualTo(esperado);
        assertThat(somaTempo).isEqualTo(esperado);
        assertThat(somaEspecialidades).isEqualTo(esperado);
    }

    private Usuario criarUsuario(String nome, String email, String hash, NivelAcesso nivel) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha(hash);
        u.setNivelAcesso(nivel);
        Usuario salvo = usuarioRepository.saveAndFlush(u);
        usuariosCriadosIds.add(salvo.getId());
        return salvo;
    }

    private Funcionario criarFuncionario(Usuario u, String cargo) {
        Funcionario f = new Funcionario();
        f.setUsuario(u);
        f.setCargo(cargo);
        Funcionario salvo = funcionarioRepository.saveAndFlush(f);
        funcionariosCriadosIds.add(salvo.getId());
        return salvo;
    }

    private Paciente criarPaciente(Usuario u, GrauSuporte grau) {
        Paciente p = new Paciente();
        p.setUsuario(u);
        p.setGrauSuporte(grau);
        Paciente salvo = pacienteRepository.saveAndFlush(p);
        pacientesCriadosIds.add(salvo.getId());
        return salvo;
    }

    private void inativarFuncionario(Long funcionarioId, Long usuarioId) {
        OffsetDateTime agora = OffsetDateTime.now();
        jdbcTemplate.update("UPDATE amde_digital.funcionario SET excluido_em = ? WHERE id = ?", agora, funcionarioId);
        jdbcTemplate.update("UPDATE amde_digital.usuario SET excluido_em = ? WHERE id = ?", agora, usuarioId);
    }

    private void inativarPaciente(Long pacienteId, Long usuarioId) {
        OffsetDateTime agora = OffsetDateTime.now();
        jdbcTemplate.update("UPDATE amde_digital.paciente SET excluido_em = ? WHERE id = ?", agora, pacienteId);
        jdbcTemplate.update("UPDATE amde_digital.usuario SET excluido_em = ? WHERE id = ?", agora, usuarioId);
    }

    private void criarAgendamento(
            Funcionario funcionario,
            OffsetDateTime inicio,
            TipoAgendamento tipo,
            StatusAgendamento status
    ) {
        Agendamento ag = new Agendamento();
        ag.setFuncionario(funcionario);
        ag.setDataInicio(inicio);
        ag.setDataFim(inicio.plusMinutes(45));
        ag.setTipoAgendamento(tipo);
        ag.setStatus(status);
        ag.setPossuiPresencaFuncionario(status == StatusAgendamento.CONCLUIDO);
        agendamentoRepository.saveAndFlush(ag);
    }

    private String placeholders(int count) {
        return String.join(",", Collections.nCopies(count, "?"));
    }
}

