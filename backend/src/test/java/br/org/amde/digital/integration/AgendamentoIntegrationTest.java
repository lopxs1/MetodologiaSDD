package br.org.amde.digital.integration;

import br.org.amde.digital.domain.entity.Agendamento;
import br.org.amde.digital.domain.entity.AgendamentoPaciente;
import br.org.amde.digital.domain.entity.AgendamentoPacienteId;
import br.org.amde.digital.domain.entity.Funcionario;
import br.org.amde.digital.domain.entity.Paciente;
import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.domain.enums.AcaoAuditoria;
import br.org.amde.digital.domain.enums.GrauSuporte;
import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.domain.enums.StatusAgendamento;
import br.org.amde.digital.domain.enums.TipoAgendamento;
import br.org.amde.digital.repository.AgendamentoPacienteRepository;
import br.org.amde.digital.repository.AgendamentoRepository;
import br.org.amde.digital.repository.FuncionarioRepository;
import br.org.amde.digital.repository.LogAuditoriaRepository;
import br.org.amde.digital.repository.PacienteRepository;
import br.org.amde.digital.repository.UsuarioRepository;
import br.org.amde.digital.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendamentoIntegrationTest {

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
    private AgendamentoPacienteRepository agendamentoPacienteRepository;

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

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
    private Usuario medicoAUser;
    private Usuario medicoBUser;
    private Funcionario funcionarioA;
    private Funcionario funcionarioB;
    private Paciente pacienteA;
    private Paciente pacienteB;
    private String tokenAdmin;
    private String tokenMedicoA;
    private String tokenMedicoB;
    private String tokenPacienteA;

    @BeforeEach
    void prepararDados() {
        String hash = passwordEncoder.encode("Senha@2026");
        String sufixo = UUID.randomUUID().toString().substring(0, 8);

        adminUser = criarUsuario("Admin " + sufixo, "admin." + sufixo + "@amde.example.org", hash, NivelAcesso.ADMINISTRADOR);
        medicoAUser = criarUsuario("Dr. João " + sufixo, "joao." + sufixo + "@amde.example.org", hash, NivelAcesso.MEDICO);
        medicoBUser = criarUsuario("Dra. Sofia " + sufixo, "sofia." + sufixo + "@amde.example.org", hash, NivelAcesso.MEDICO);

        funcionarioA = criarFuncionario(medicoAUser, "Psicologia");
        funcionarioB = criarFuncionario(medicoBUser, "Fonoaudiologia");

        Usuario pacAUser = criarUsuario("Lucas Álvares " + sufixo, "lucas." + sufixo + "@amde.example.org", hash, NivelAcesso.PACIENTE);
        Usuario pacBUser = criarUsuario("Marina Sigilo " + sufixo, "marina." + sufixo + "@amde.example.org", hash, NivelAcesso.PACIENTE);

        pacienteA = criarPaciente(pacAUser, GrauSuporte.NIVEL_1);
        pacienteB = criarPaciente(pacBUser, GrauSuporte.NIVEL_2);

        tokenAdmin = jwtService.gerarToken(adminUser).token();
        tokenMedicoA = jwtService.gerarToken(medicoAUser).token();
        tokenMedicoB = jwtService.gerarToken(medicoBUser).token();
        tokenPacienteA = jwtService.gerarToken(pacAUser).token();
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
    void deveCriarSessoesAdjacentesReutilizarCanceladoEditarBuscarComUnaccentEAuditar() throws Exception {
        OffsetDateTime base = OffsetDateTime.now(ZoneOffset.ofHours(-3)).plusDays(5).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime slot1Inicio = base.withHour(10);
        OffsetDateTime slot1Fim = base.withHour(11);
        OffsetDateTime slot2Fim = base.withHour(12);

        String bodySlot1 = payloadAgendamento(funcionarioA.getId(), slot1Inicio, slot1Fim, TipoAgendamento.CONSULTA, List.of(pacienteA.getId()));
        MvcResult createRes1 = mockMvc.perform(post("/api/agendamentos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodySlot1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andReturn();
        long id1 = objectMapper.readTree(createRes1.getResponse().getContentAsString()).get("id").asLong();

        // Slot adjacente 11:00-12:00 deve ser aceito
        String bodySlot2 = payloadAgendamento(funcionarioA.getId(), slot1Fim, slot2Fim, TipoAgendamento.AVALIACAO, List.of(pacienteA.getId()));
        mockMvc.perform(post("/api/agendamentos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodySlot2))
                .andExpect(status().isCreated());

        // Sobreposição 10:30-11:30 deve retornar 409
        String bodyConflito = payloadAgendamento(
                funcionarioA.getId(), slot1Inicio.plusMinutes(30), slot1Fim.plusMinutes(30), TipoAgendamento.EXAME, List.of(pacienteA.getId())
        );
        mockMvc.perform(post("/api/agendamentos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyConflito))
                .andExpect(status().isConflict());

        // Cancelar slot 1 e reutilizar o mesmo intervalo
        mockMvc.perform(delete("/api/agendamentos/" + id1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoA))
                .andExpect(status().isNoContent());

        MvcResult recriadoRes = mockMvc.perform(post("/api/agendamentos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodySlot1))
                .andExpect(status().isCreated())
                .andReturn();
        long idRecriado = objectMapper.readTree(recriadoRes.getResponse().getContentAsString()).get("id").asLong();

        // Editar adicionando Paciente B e mudando para TERAPIA
        String bodyEditar = payloadAgendamento(
                funcionarioA.getId(), slot1Inicio, slot1Fim, TipoAgendamento.TERAPIA, List.of(pacienteA.getId(), pacienteB.getId())
        );
        mockMvc.perform(put("/api/agendamentos/" + idRecriado)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyEditar))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoAgendamento").value("TERAPIA"))
                .andExpect(jsonPath("$.pacientes", hasSize(2)));

        // Busca textual sem acento (unaccent)
        mockMvc.perform(get("/api/agendamentos")
                        .param("termo", "Alvares")
                        .param("status", "AGENDADO")
                        .param("tipoAgendamento", "TERAPIA")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].id").value(idRecriado));

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            List<String> recursos = logAuditoriaRepository.findAll().stream()
                    .filter(l -> l.getAcao() == AcaoAuditoria.AGENDAMENTO)
                    .map(l -> l.getRecurso())
                    .toList();
            assertThat(recursos).contains(
                    "agendamento:" + id1 + ":CRIAR",
                    "agendamento:" + id1 + ":CANCELAR",
                    "agendamento:" + idRecriado + ":CRIAR",
                    "agendamento:" + idRecriado + ":EDITAR"
            );
        });
    }

    @Test
    void deveGarantirIsolamentoRbacEMinimizacaoLgpdEmGrupo() throws Exception {
        OffsetDateTime inicioA = OffsetDateTime.now().plusDays(4);
        String bodyGrupo = payloadAgendamento(
                funcionarioA.getId(), inicioA, inicioA.plusHours(1), TipoAgendamento.TERAPIA,
                List.of(pacienteA.getId(), pacienteB.getId())
        );

        MvcResult resGrupo = mockMvc.perform(post("/api/agendamentos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyGrupo))
                .andExpect(status().isCreated())
                .andReturn();
        long idGrupo = objectMapper.readTree(resGrupo.getResponse().getContentAsString()).get("id").asLong();

        // Sessão do Médico B apenas com Paciente B
        String bodyMedicoB = payloadAgendamento(
                funcionarioB.getId(), inicioA, inicioA.plusHours(1), TipoAgendamento.CONSULTA, List.of(pacienteB.getId())
        );
        MvcResult resB = mockMvc.perform(post("/api/agendamentos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyMedicoB))
                .andExpect(status().isCreated())
                .andReturn();
        long idSessaoB = objectMapper.readTree(resB.getResponse().getContentAsString()).get("id").asLong();

        // Médico B bloqueado na agenda do Médico A
        mockMvc.perform(get("/api/agendamentos/" + idGrupo).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoB))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/agendamentos/" + idGrupo).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoB))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/agendamentos/" + idGrupo + "/concluir").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoB))
                .andExpect(status().isForbidden());

        // Paciente A vê apenas sua própria entrada na sessão em grupo
        mockMvc.perform(get("/api/agendamentos/" + idGrupo).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPacienteA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacientes", hasSize(1)))
                .andExpect(jsonPath("$.pacientes[0].pacienteId").value(pacienteA.getId()));

        // Paciente A não descobre colega via filtro termo na listagem
        mockMvc.perform(get("/api/agendamentos")
                        .param("termo", "Marina Sigilo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPacienteA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(0)));

        // Paciente A bloqueado em sessão que não participa
        mockMvc.perform(get("/api/agendamentos/" + idSessaoB).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPacienteA))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRegistrarPresencaConcluirECorrigirPresencaAposConclusao() throws Exception {
        Agendamento ag = new Agendamento();
        ag.setFuncionario(funcionarioA);
        ag.setDataInicio(OffsetDateTime.now().minusMinutes(20));
        ag.setDataFim(OffsetDateTime.now().plusMinutes(40));
        ag.setTipoAgendamento(TipoAgendamento.CONSULTA);
        ag.setStatus(StatusAgendamento.AGENDADO);
        ag.setPossuiPresencaFuncionario(false);
        Agendamento salvo = agendamentoRepository.saveAndFlush(ag);

        AgendamentoPaciente ap = new AgendamentoPaciente();
        ap.setId(new AgendamentoPacienteId(salvo.getId(), pacienteA.getId()));
        ap.setAgendamento(salvo);
        ap.setPaciente(pacienteA);
        ap.setPossuiPresenca(false);
        agendamentoPacienteRepository.saveAndFlush(ap);

        String presencaJson = """
                {
                  "possuiPresencaFuncionario": true,
                  "pacientes": [
                    { "pacienteId": %d, "possuiPresenca": true }
                  ]
                }
                """.formatted(pacienteA.getId());

        mockMvc.perform(patch("/api/agendamentos/" + salvo.getId() + "/presenca")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(presencaJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.possuiPresencaFuncionario").value(true))
                .andExpect(jsonPath("$.pacientes[0].possuiPresenca").value(true));

        mockMvc.perform(patch("/api/agendamentos/" + salvo.getId() + "/concluir")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));

        // Correção de presença após conclusão é permitida (RN42); nova conclusão responde 409
        mockMvc.perform(patch("/api/agendamentos/" + salvo.getId() + "/presenca")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"possuiPresencaFuncionario\":true,\"pacientes\":[{\"pacienteId\":" + pacienteA.getId() + ",\"possuiPresenca\":false}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacientes[0].possuiPresenca").value(false));

        mockMvc.perform(patch("/api/agendamentos/" + salvo.getId() + "/concluir")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoA))
                .andExpect(status().isConflict());
    }

    @Test
    void deveSerializarConcorrenciaDeChoqueDeHorarioGarantindoUm201EUm409() throws Exception {
        OffsetDateTime inicio1 = OffsetDateTime.now().plusDays(10).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime fim1 = inicio1.plusHours(1);
        OffsetDateTime inicio2 = inicio1.plusMinutes(30);
        OffsetDateTime fim2 = fim1.plusMinutes(30);

        String payload1 = payloadAgendamento(funcionarioA.getId(), inicio1, fim1, TipoAgendamento.CONSULTA, List.of(pacienteA.getId()));
        String payload2 = payloadAgendamento(funcionarioA.getId(), inicio2, fim2, TipoAgendamento.CONSULTA, List.of(pacienteB.getId()));

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch largada = new CountDownLatch(1);
            Callable<Integer> t1 = criarChamadaConcorrente(largada, payload1);
            Callable<Integer> t2 = criarChamadaConcorrente(largada, payload2);

            Future<Integer> f1 = pool.submit(t1);
            Future<Integer> f2 = pool.submit(t2);
            largada.countDown();

            assertThat(List.of(f1.get(), f2.get())).containsExactlyInAnyOrder(201, 409);

            JsonNode listagem = objectMapper.readTree(
                    mockMvc.perform(get("/api/agendamentos")
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoA))
                            .andExpect(status().isOk())
                            .andReturn()
                            .getResponse()
                            .getContentAsString()
            );
            assertThat(listagem.get("totalItens").asLong()).isEqualTo(1L);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void deveListarEConsultarAgendamentoHistoricoComProfissionalEPacienteInativadosComoAdmin() throws Exception {
        OffsetDateTime inicio = OffsetDateTime.now().minusDays(2);
        Agendamento ag = new Agendamento();
        ag.setFuncionario(funcionarioB);
        ag.setDataInicio(inicio);
        ag.setDataFim(inicio.plusMinutes(45));
        ag.setTipoAgendamento(TipoAgendamento.CONSULTA);
        ag.setStatus(StatusAgendamento.CONCLUIDO);
        ag.setPossuiPresencaFuncionario(true);
        Agendamento salvo = agendamentoRepository.saveAndFlush(ag);

        AgendamentoPaciente ap = new AgendamentoPaciente();
        ap.setId(new AgendamentoPacienteId(salvo.getId(), pacienteB.getId()));
        ap.setAgendamento(salvo);
        ap.setPaciente(pacienteB);
        ap.setPossuiPresenca(true);
        agendamentoPacienteRepository.saveAndFlush(ap);

        OffsetDateTime agora = OffsetDateTime.now();
        jdbcTemplate.update("UPDATE amde_digital.funcionario SET excluido_em = ? WHERE id = ?", agora, funcionarioB.getId());
        jdbcTemplate.update("UPDATE amde_digital.usuario SET excluido_em = ? WHERE id = ?", agora, medicoBUser.getId());
        jdbcTemplate.update("UPDATE amde_digital.paciente SET excluido_em = ? WHERE id = ?", agora, pacienteB.getId());
        jdbcTemplate.update("UPDATE amde_digital.usuario SET excluido_em = ? WHERE id = ?", agora, pacienteB.getUsuario().getId());

        mockMvc.perform(get("/api/agendamentos")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "dataInicio")
                        .param("direction", "ASC")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isArray());

        mockMvc.perform(get("/api/agendamentos")
                        .param("funcionarioId", funcionarioB.getId().toString())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].id").value(salvo.getId()))
                .andExpect(jsonPath("$.itens[0].nomeProfissional").value(medicoBUser.getNome()))
                .andExpect(jsonPath("$.itens[0].pacientes[0].nomePaciente").value(pacienteB.getUsuario().getNome()));

        mockMvc.perform(get("/api/agendamentos/" + salvo.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.getId()))
                .andExpect(jsonPath("$.nomeProfissional").value(medicoBUser.getNome()))
                .andExpect(jsonPath("$.pacientes[0].nomePaciente").value(pacienteB.getUsuario().getNome()));
    }

    private Callable<Integer> criarChamadaConcorrente(CountDownLatch largada, String payload) {
        return () -> {
            largada.await();
            return mockMvc.perform(post("/api/agendamentos")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedicoA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andReturn()
                    .getResponse()
                    .getStatus();
        };
    }

    private String payloadAgendamento(
            Long funcionarioId,
            OffsetDateTime inicio,
            OffsetDateTime fim,
            TipoAgendamento tipo,
            List<Long> pacientesIds
    ) throws Exception {
        return objectMapper.writeValueAsString(java.util.Map.of(
                "funcionarioId", funcionarioId,
                "dataInicio", inicio.toString(),
                "dataFim", fim.toString(),
                "tipoAgendamento", tipo.name(),
                "pacientesIds", pacientesIds
        ));
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

    private String placeholders(int count) {
        return String.join(",", java.util.Collections.nCopies(count, "?"));
    }
}

