package br.org.amde.digital.integration;

import br.org.amde.digital.domain.entity.Categoria;
import br.org.amde.digital.domain.entity.Funcionario;
import br.org.amde.digital.domain.entity.Paciente;
import br.org.amde.digital.domain.entity.RegistroProntuario;
import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.domain.enums.GrauSuporte;
import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.repository.CategoriaRepository;
import br.org.amde.digital.repository.FuncionarioRepository;
import br.org.amde.digital.repository.PacienteRepository;
import br.org.amde.digital.repository.RegistroProntuarioRepository;
import br.org.amde.digital.repository.UsuarioRepository;
import br.org.amde.digital.security.JwtService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PacienteIntegrationTest {

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EntityManager entityManager;

    private String tokenMedico;
    private String tokenPaciente;
    private Paciente pacienteJose;
    private Paciente pacienteMarina;
    private Paciente pacienteInativo;

    @BeforeEach
    void prepararCenario() {
        Categoria catExames = garantirCategoria("Exames");
        Categoria catLaudos = garantirCategoria("Laudos");
        garantirCategoria("Receituario");
        Categoria catArquivado = garantirCategoria("Arquivado");

        String senhaHash = passwordEncoder.encode("Senha@2026");

        Usuario uMedico = criarUsuario("Dra. Helena Teste", "helena.it@amde.example.org", senhaHash, NivelAcesso.MEDICO, "15999990002");
        Funcionario fMedico = new Funcionario();
        fMedico.setUsuario(uMedico);
        fMedico.setCargo("Psicologia");
        funcionarioRepository.saveAndFlush(fMedico);

        Usuario uJose = criarUsuario("José Lucas Integração", "jose.lucas.it@amde.example.org", senhaHash, NivelAcesso.PACIENTE, "15999990011");
        pacienteJose = criarPaciente(uJose, GrauSuporte.NIVEL_2, "Descrição confidencial do José");

        Usuario uMarina = criarUsuario("Marina Silva Integração", "marina.it@amde.example.org", senhaHash, NivelAcesso.PACIENTE, "15999990022");
        pacienteMarina = criarPaciente(uMarina, GrauSuporte.NIVEL_1, "Descrição confidencial da Marina");

        Usuario uInativo = criarUsuario("Carlos Inativo Integração", "carlos.it@amde.example.org", senhaHash, NivelAcesso.PACIENTE, "15999990033");
        uInativo.setExcluidoEm(OffsetDateTime.now());
        usuarioRepository.saveAndFlush(uInativo);
        pacienteInativo = criarPaciente(uInativo, GrauSuporte.NIVEL_3, "Inativo");
        pacienteInativo.setExcluidoEm(OffsetDateTime.now());
        pacienteRepository.saveAndFlush(pacienteInativo);

        RegistroProntuario laudoAntigo = criarRegistro(pacienteJose, catLaudos, null, "Laudo Inicial", "laudo-1.pdf");
        criarRegistro(pacienteJose, catLaudos, laudoAntigo, "Laudo Revisado", "laudo-2.pdf");
        criarRegistro(pacienteJose, catExames, null, "Exame Audiológico", "exame-1.pdf");
        criarRegistro(pacienteJose, catArquivado, null, "Documento Antigo", "arq-1.pdf");

        RegistroProntuario docExcluido = criarRegistro(pacienteJose, catExames, null, "Exame Removido", "removido.pdf");
        docExcluido.setExcluidoEm(OffsetDateTime.now());
        registroProntuarioRepository.saveAndFlush(docExcluido);

        entityManager.flush();
        entityManager.clear();

        tokenMedico = jwtService.gerarToken(uMedico).token();
        tokenPaciente = jwtService.gerarToken(uJose).token();
    }

    @Test
    void deveBuscarComUnaccentFiltrarEOrdenarSemVazarDescricaoClinica() throws Exception {
        mockMvc.perform(get("/api/pacientes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedico)
                        .param("termo", "jose")
                        .param("grauSuporte", "NIVEL_2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].nome").value("José Lucas Integração"))
                .andExpect(jsonPath("$.itens[0].numCelular").value("(15) *****-0011"))
                .andExpect(content().string(not(containsString("Descrição confidencial"))));
    }

    @Test
    void deveRetornarFichaComHistoricoAgrupadoERevelarCelularParaMedico() throws Exception {
        mockMvc.perform(get("/api/pacientes/" + pacienteJose.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedico)
                        .param("revelarCelular", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numCelular").value("(15) 99999-0011"))
                .andExpect(jsonPath("$.celularMascarado").value(false))
                .andExpect(jsonPath("$.historicoPorCategoria[0].categoria").value("Exames"))
                .andExpect(jsonPath("$.historicoPorCategoria[0].totalDocumentos").value(1))
                .andExpect(jsonPath("$.historicoPorCategoria[1].categoria").value("Laudos"))
                .andExpect(jsonPath("$.historicoPorCategoria[1].totalDocumentos").value(2))
                .andExpect(jsonPath("$.historicoPorCategoria[3].categoria").value("Arquivado"))
                .andExpect(jsonPath("$.historicoPorCategoria[3].totalDocumentos").value(1));
    }

    @Test
    void deveRestringirAcessoDoPacienteEResponder404ParaInativado() throws Exception {
        mockMvc.perform(get("/api/pacientes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPaciente))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/pacientes/" + pacienteMarina.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPaciente))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/pacientes/meu-prontuario")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPaciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pacienteJose.getId()))
                .andExpect(jsonPath("$.numCelular").value("(15) *****-0011"))
                .andExpect(jsonPath("$.celularMascarado").value(true));

        mockMvc.perform(get("/api/pacientes/" + pacienteInativo.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedico))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarDadosClinicosEPersistirNoPostgres18() throws Exception {
        mockMvc.perform(put("/api/pacientes/" + pacienteJose.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grauSuporte\":\"NIVEL_3\",\"descricao\":\"Evolução integrada validada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grauSuporte").value("NIVEL_3"))
                .andExpect(jsonPath("$.descricao").value("Evolução integrada validada"));

        entityManager.flush();
        entityManager.clear();

        Paciente atualizado = pacienteRepository.findAtivoByIdComUsuario(pacienteJose.getId()).orElseThrow();
        assertThat(atualizado.getGrauSuporte()).isEqualTo(GrauSuporte.NIVEL_3);
        assertThat(atualizado.getDescricao()).isEqualTo("Evolução integrada validada");
        assertThat(atualizado.getUsuario().getNome()).isEqualTo("José Lucas Integração");
    }

    private Categoria garantirCategoria(String nome) {
        return categoriaRepository.findByNome(nome).orElseGet(() -> {
            Categoria c = new Categoria();
            c.setNome(nome);
            return categoriaRepository.saveAndFlush(c);
        });
    }

    private Usuario criarUsuario(String nome, String email, String senhaHash, NivelAcesso nivel, String celular) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha(senhaHash);
        u.setNivelAcesso(nivel);
        u.setNumCelular(celular);
        u.setPais("Brasil");
        u.setEstado("SP");
        u.setCidade("Sorocaba");
        return usuarioRepository.saveAndFlush(u);
    }

    private Paciente criarPaciente(Usuario u, GrauSuporte grau, String descricao) {
        Paciente p = new Paciente();
        p.setUsuario(u);
        p.setGrauSuporte(grau);
        p.setDescricao(descricao);
        return pacienteRepository.saveAndFlush(p);
    }

    private RegistroProntuario criarRegistro(
            Paciente paciente,
            Categoria categoria,
            RegistroProntuario substituido,
            String titulo,
            String arquivoOriginal
    ) {
        RegistroProntuario r = new RegistroProntuario();
        r.setPaciente(paciente);
        r.setCategoria(categoria);
        r.setRegistroSubstituido(substituido);
        r.setTituloDocumento(titulo);
        r.setNomeArquivoOriginal(arquivoOriginal);
        r.setNomeArquivoArmazenado(UUID.randomUUID() + ".pdf");
        return registroProntuarioRepository.saveAndFlush(r);
    }
}
