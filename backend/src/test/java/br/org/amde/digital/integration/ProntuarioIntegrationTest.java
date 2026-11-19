package br.org.amde.digital.integration;

import br.org.amde.digital.domain.entity.Categoria;
import br.org.amde.digital.domain.entity.Funcionario;
import br.org.amde.digital.domain.entity.Paciente;
import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.domain.enums.AcaoAuditoria;
import br.org.amde.digital.domain.enums.GrauSuporte;
import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.repository.CategoriaRepository;
import br.org.amde.digital.repository.FuncionarioRepository;
import br.org.amde.digital.repository.LogAuditoriaRepository;
import br.org.amde.digital.repository.PacienteRepository;
import br.org.amde.digital.repository.RegistroProntuarioRepository;
import br.org.amde.digital.repository.UsuarioRepository;
import br.org.amde.digital.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProntuarioIntegrationTest {

    @TempDir
    static Path tempUploadDir;

    @DynamicPropertySource
    static void configurarAmbiente(DynamicPropertyRegistry registry) {
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
        registry.add("amde.storage.upload-dir", () -> tempUploadDir.toAbsolutePath().toString());
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
    private CategoriaRepository categoriaRepository;

    @Autowired
    private RegistroProntuarioRepository registroProntuarioRepository;

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final List<Long> usuariosCriadosIds = new ArrayList<>();
    private final List<Long> pacientesCriadosIds = new ArrayList<>();

    private Usuario medicoUser;
    private Usuario pacienteAUser;
    private Usuario pacienteBUser;
    private Paciente pacienteA;
    private String tokenMedico;
    private String tokenPacienteA;
    private String tokenPacienteB;

    @BeforeEach
    void prepararDados() {
        garantirCategoria("Exames");
        garantirCategoria("Laudos");
        garantirCategoria("Receituario");
        garantirCategoria("Arquivado");

        String hash = passwordEncoder.encode("Senha@2026");
        String sufixo = UUID.randomUUID().toString().substring(0, 8);

        medicoUser = criarUsuario("Dra. Helena " + sufixo, "helena." + sufixo + "@amde.example.org", hash, NivelAcesso.MEDICO);
        Funcionario func = new Funcionario();
        func.setUsuario(medicoUser);
        func.setCargo("Psicologia");
        funcionarioRepository.saveAndFlush(func);

        pacienteAUser = criarUsuario("Lucas A " + sufixo, "lucas.a." + sufixo + "@amde.example.org", hash, NivelAcesso.PACIENTE);
        pacienteA = criarPaciente(pacienteAUser, GrauSuporte.NIVEL_1);

        pacienteBUser = criarUsuario("Marina B " + sufixo, "marina.b." + sufixo + "@amde.example.org", hash, NivelAcesso.PACIENTE);
        criarPaciente(pacienteBUser, GrauSuporte.NIVEL_2);

        tokenMedico = jwtService.gerarToken(medicoUser).token();
        tokenPacienteA = jwtService.gerarToken(pacienteAUser).token();
        tokenPacienteB = jwtService.gerarToken(pacienteBUser).token();
    }

    @AfterEach
    void limparDados() {
        if (!pacientesCriadosIds.isEmpty()) {
            String placeholders = String.join(",", pacientesCriadosIds.stream().map(id -> "?").toList());
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.registro_prontuario WHERE paciente_id IN (" + placeholders + ")",
                    pacientesCriadosIds.toArray()
            );
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.paciente WHERE id IN (" + placeholders + ")",
                    pacientesCriadosIds.toArray()
            );
        }
        if (!usuariosCriadosIds.isEmpty()) {
            String placeholders = String.join(",", usuariosCriadosIds.stream().map(id -> "?").toList());
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.log_auditoria WHERE usuario_id IN (" + placeholders + ")",
                    usuariosCriadosIds.toArray()
            );
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.funcionario WHERE usuario_id IN (" + placeholders + ")",
                    usuariosCriadosIds.toArray()
            );
            jdbcTemplate.update(
                    "DELETE FROM amde_digital.usuario WHERE id IN (" + placeholders + ")",
                    usuariosCriadosIds.toArray()
            );
        }
        pacientesCriadosIds.clear();
        usuariosCriadosIds.clear();
    }

    @Test
    void deveEnviarBaixarComIntegridadeSha256ReclassificarEExcluirEm3Passos() throws Exception {
        byte[] pdfBytes = ("%PDF-1.7\n%Conteudo sintetico TEA\n1 0 obj\n<< /Type /Catalog >>\nendobj\n%%EOF\n")
                .getBytes(StandardCharsets.UTF_8);
        byte[] hashEsperado = MessageDigest.getInstance("SHA-256").digest(pdfBytes);

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "Laudo Avaliação TEA.pdf",
                "application/pdf",
                pdfBytes
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/prontuarios/upload")
                        .file(arquivo)
                        .param("pacienteId", String.valueOf(pacienteA.getId()))
                        .param("tituloDocumento", "Laudo Avaliação Neuropsicológica")
                        .param("categoria", "Laudos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedico))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("VIGENTE"))
                .andReturn();

        Long docId = objectMapper.readTree(uploadResult.getResponse().getContentAsString()).get("id").asLong();

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(logAuditoriaRepository.findByUsuarioIdOrderByCriadoEmDesc(medicoUser.getId()))
                        .anyMatch(l -> l.getAcao() == AcaoAuditoria.UPLOAD
                                && l.getRecurso().equals("documento:" + docId + ":UPLOAD"))
        );

        mockMvc.perform(get("/api/prontuarios/" + docId + "/download")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPacienteB))
                .andExpect(status().isForbidden());

        MvcResult downloadResult = mockMvc.perform(get("/api/prontuarios/" + docId + "/download")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPacienteA))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("Laudo")))
                .andReturn();

        byte[] bytesBaixados = downloadResult.getResponse().getContentAsByteArray();
        assertThat(MessageDigest.getInstance("SHA-256").digest(bytesBaixados)).isEqualTo(hashEsperado);

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(logAuditoriaRepository.findByUsuarioIdOrderByCriadoEmDesc(pacienteAUser.getId()))
                        .anyMatch(l -> l.getAcao() == AcaoAuditoria.DOWNLOAD
                                && l.getRecurso().equals("documento:" + docId + ":DOWNLOAD"))
        );

        mockMvc.perform(patch("/api/prontuarios/" + docId + "/categoria")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\":\"Arquivado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ARQUIVADO"));

        String nomeArmazenado = jdbcTemplate.queryForObject(
                "SELECT nome_arquivo_armazenado FROM amde_digital.registro_prontuario WHERE id = ?",
                String.class,
                docId
        );
        Path caminhoFisico = tempUploadDir.resolve("paciente-" + pacienteA.getId()).resolve(nomeArmazenado);
        assertThat(Files.exists(caminhoFisico)).isTrue();

        mockMvc.perform(delete("/api/prontuarios/" + docId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedico))
                .andExpect(status().isNoContent());

        assertThat(Files.exists(caminhoFisico)).isFalse();
        assertThat(logAuditoriaRepository.findByUsuarioIdOrderByCriadoEmDesc(medicoUser.getId()))
                .anyMatch(l -> l.getAcao() == AcaoAuditoria.EXCLUSAO
                        && l.getRecurso().equals("documento:" + docId + ":EXCLUSAO"));

        mockMvc.perform(get("/api/prontuarios/" + docId + "/download")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedico))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveSuportarUploadsSimultaneosComMesmoNomeESubstituicaoConcorrenteComLock() throws Exception {
        byte[] pdfBytes = "%PDF-1.7\n1 0 obj\n<<>>\nendobj\n".getBytes(StandardCharsets.US_ASCII);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch largadaUploads = new CountDownLatch(1);
            Callable<Integer> uploadTask = () -> {
                largadaUploads.await();
                MockMultipartFile file = new MockMultipartFile("arquivo", "exame.pdf", "application/pdf", pdfBytes);
                return mockMvc.perform(multipart("/api/prontuarios/upload")
                                .file(file)
                                .param("pacienteId", String.valueOf(pacienteA.getId()))
                                .param("tituloDocumento", "Exame Simultaneo")
                                .param("categoria", "Exames")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedico))
                        .andReturn()
                        .getResponse()
                        .getStatus();
            };

            Future<Integer> u1 = pool.submit(uploadTask);
            Future<Integer> u2 = pool.submit(uploadTask);
            largadaUploads.countDown();
            assertThat(List.of(u1.get(), u2.get())).containsExactly(201, 201);

            var docs = registroProntuarioRepository.findAtivosByPacienteIdComCategoria(pacienteA.getId());
            assertThat(docs).hasSize(2);
            assertThat(docs.get(0).getNomeArquivoArmazenado()).isNotEqualTo(docs.get(1).getNomeArquivoArmazenado());

            Long alvoId = docs.get(0).getId();
            CountDownLatch largadaSubs = new CountDownLatch(1);
            Callable<Integer> subTask = () -> {
                largadaSubs.await();
                MockMultipartFile file = new MockMultipartFile("arquivo", "exame-v2.pdf", "application/pdf", pdfBytes);
                return mockMvc.perform(multipart("/api/prontuarios/" + alvoId + "/substituir")
                                .file(file)
                                .param("tituloDocumento", "Exame Revisado")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedico))
                        .andReturn()
                        .getResponse()
                        .getStatus();
            };

            Future<Integer> s1 = pool.submit(subTask);
            Future<Integer> s2 = pool.submit(subTask);
            largadaSubs.countDown();
            assertThat(List.of(s1.get(), s2.get())).containsExactlyInAnyOrder(201, 409);

            JsonNode fichaJson = objectMapper.readTree(
                    mockMvc.perform(get("/api/pacientes/" + pacienteA.getId())
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedico))
                            .andExpect(status().isOk())
                            .andReturn()
                            .getResponse()
                            .getContentAsString()
            );
            assertThat(fichaJson.get("historicoPorCategoria").get(0).get("totalDocumentos").asInt()).isEqualTo(3);
        } finally {
            pool.shutdownNow();
        }
    }

    private void garantirCategoria(String nome) {
        categoriaRepository.findByNome(nome).orElseGet(() -> {
            Categoria c = new Categoria();
            c.setNome(nome);
            return categoriaRepository.saveAndFlush(c);
        });
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

    private Paciente criarPaciente(Usuario u, GrauSuporte grau) {
        Paciente p = new Paciente();
        p.setUsuario(u);
        p.setGrauSuporte(grau);
        Paciente salvo = pacienteRepository.saveAndFlush(p);
        pacientesCriadosIds.add(salvo.getId());
        return salvo;
    }
}
