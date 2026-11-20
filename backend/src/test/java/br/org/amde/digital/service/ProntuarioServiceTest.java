package br.org.amde.digital.service;

import br.org.amde.digital.domain.entity.Categoria;
import br.org.amde.digital.domain.entity.Paciente;
import br.org.amde.digital.domain.entity.RegistroProntuario;
import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.domain.enums.AcaoAuditoria;
import br.org.amde.digital.domain.enums.GrauSuporte;
import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.domain.enums.SituacaoDocumento;
import br.org.amde.digital.dto.prontuario.ProntuarioResponse;
import br.org.amde.digital.dto.prontuario.ReclassificarCategoriaRequest;
import br.org.amde.digital.exception.ConflitoException;
import br.org.amde.digital.exception.ErroAuditoriaPreviaException;
import br.org.amde.digital.exception.ErroStorageException;
import br.org.amde.digital.repository.CategoriaRepository;
import br.org.amde.digital.repository.PacienteRepository;
import br.org.amde.digital.repository.RegistroProntuarioRepository;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.ProntuarioService.DownloadProntuarioResult;
import br.org.amde.digital.service.auditoria.AuditoriaService;
import br.org.amde.digital.service.auditoria.ClientIpResolver;
import br.org.amde.digital.service.auditoria.RecursoAuditoriaFormatter;
import br.org.amde.digital.service.prontuario.PdfValidator;
import br.org.amde.digital.service.storage.ProntuarioStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProntuarioServiceTest {

    @Mock
    private RegistroProntuarioRepository registroProntuarioRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Spy
    private PdfValidator pdfValidator = new PdfValidator();

    @Mock
    private ProntuarioStorageService storageService;

    @Mock
    private AuditoriaService auditoriaService;

    @Spy
    private RecursoAuditoriaFormatter recursoFormatter = new RecursoAuditoriaFormatter();

    @Mock
    private ClientIpResolver clientIpResolver;

    @InjectMocks
    private ProntuarioService prontuarioService;

    @Test
    void deveEnviarDocumentoComCompensacaoEmFalhaDePersistencia() {
        Paciente paciente = criarPaciente(12L, 25L);
        Categoria laudos = criarCategoria(2L, "Laudos");
        MockMultipartFile arquivo = criarPdfMock("laudo-2026.pdf");
        UsuarioAutenticado medico = new UsuarioAutenticado(2L, "Dra. Helena", "helena@amde.example.org", NivelAcesso.MEDICO);

        when(pacienteRepository.findAtivoByIdComUsuario(12L)).thenReturn(Optional.of(paciente));
        when(categoriaRepository.findByNome("Laudos")).thenReturn(Optional.of(laudos));
        when(registroProntuarioRepository.saveAndFlush(any(RegistroProntuario.class))).thenAnswer(inv -> {
            RegistroProntuario r = inv.getArgument(0);
            r.setId(105L);
            return r;
        });

        ProntuarioResponse resposta = prontuarioService.enviar(
                12L, "  Laudo neuropsicológico  ", "Laudos", null, arquivo, medico
        );

        assertThat(resposta.id()).isEqualTo(105L);
        assertThat(resposta.tituloDocumento()).isEqualTo("Laudo neuropsicológico");
        assertThat(resposta.nomeArquivoOriginal()).isEqualTo("laudo-2026.pdf");
        assertThat(resposta.situacao()).isEqualTo(SituacaoDocumento.VIGENTE);
        verify(storageService).armazenar(eq(12L), any(), any());
        verify(auditoriaService).registrarAsync(2L, AcaoAuditoria.UPLOAD, "documento:105:UPLOAD");

        when(registroProntuarioRepository.saveAndFlush(any(RegistroProntuario.class)))
                .thenThrow(new DataIntegrityViolationException("Erro simulado"));

        assertThatThrownBy(() -> prontuarioService.enviar(
                12L, "Laudo falha", "Laudos", null, criarPdfMock("falha.pdf"), medico
        )).isInstanceOf(DataIntegrityViolationException.class);
        verify(storageService).removerSilencioso(eq(12L), any());
    }

    @Test
    void deveSubstituirDocumentoVigenteERejeitarSubstituicaoDeArquivadoOuJaSubstituido() {
        Paciente paciente = criarPaciente(12L, 25L);
        Categoria exames = criarCategoria(1L, "Exames");
        Categoria arquivado = criarCategoria(4L, "Arquivado");
        RegistroProntuario vigente = criarRegistro(10L, paciente, exames, "Exame Original", "orig.pdf", "u1.pdf");
        UsuarioAutenticado admin = new UsuarioAutenticado(1L, "Admin", "admin@amde.example.org", NivelAcesso.ADMINISTRADOR);

        when(registroProntuarioRepository.findAtivoByIdForUpdate(10L)).thenReturn(Optional.of(vigente));
        when(registroProntuarioRepository.existsSubstitutoAtivoPara(10L)).thenReturn(false);
        when(registroProntuarioRepository.saveAndFlush(any(RegistroProntuario.class))).thenAnswer(inv -> {
            RegistroProntuario r = inv.getArgument(0);
            r.setId(11L);
            return r;
        });

        ProntuarioResponse novaVersao = prontuarioService.substituir(
                10L, null, criarPdfMock("revisado.pdf"), admin
        );
        assertThat(novaVersao.id()).isEqualTo(11L);
        assertThat(novaVersao.registroSubstituidoId()).isEqualTo(10L);
        assertThat(novaVersao.tituloDocumento()).isEqualTo("Exame Original");
        assertThat(novaVersao.categoria()).isEqualTo("Exames");
        verify(auditoriaService).registrarAsync(1L, AcaoAuditoria.UPLOAD, "documento:11:SUBSTITUICAO");

        when(registroProntuarioRepository.existsSubstitutoAtivoPara(10L)).thenReturn(true);
        assertThatThrownBy(() -> prontuarioService.substituir(10L, "Novo", criarPdfMock("v3.pdf"), admin))
                .isInstanceOf(ConflitoException.class);

        RegistroProntuario docArq = criarRegistro(20L, paciente, arquivado, "Antigo", "ant.pdf", "u2.pdf");
        when(registroProntuarioRepository.findAtivoByIdForUpdate(20L)).thenReturn(Optional.of(docArq));
        assertThatThrownBy(() -> prontuarioService.substituir(20L, "Novo", criarPdfMock("v2.pdf"), admin))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void deveReclassificarDocumentoEntreCategorias() {
        Paciente paciente = criarPaciente(12L, 25L);
        Categoria exames = criarCategoria(1L, "Exames");
        Categoria arquivado = criarCategoria(4L, "Arquivado");
        RegistroProntuario doc = criarRegistro(30L, paciente, exames, "Exame", "exame.pdf", "u3.pdf");

        when(registroProntuarioRepository.findAtivoByIdComPacienteECategoria(30L)).thenReturn(Optional.of(doc));
        when(categoriaRepository.findByNome("Arquivado")).thenReturn(Optional.of(arquivado));
        when(registroProntuarioRepository.saveAndFlush(doc)).thenReturn(doc);
        when(registroProntuarioRepository.existsSubstitutoAtivoPara(30L)).thenReturn(false);

        ProntuarioResponse resposta = prontuarioService.reclassificarCategoria(
                30L, new ReclassificarCategoriaRequest("Arquivado")
        );

        assertThat(resposta.categoria()).isEqualTo("Arquivado");
        assertThat(resposta.situacao()).isEqualTo(SituacaoDocumento.ARQUIVADO);
    }

    @Test
    void deveAutorizarDownloadParaTitularEBloquearTerceiroOuArquivoAusente() {
        Paciente pacienteA = criarPaciente(12L, 25L);
        Categoria laudos = criarCategoria(2L, "Laudos");
        RegistroProntuario doc = criarRegistro(40L, pacienteA, laudos, "Laudo", "laudo-a.pdf", "u4.pdf");

        when(registroProntuarioRepository.findAtivoByIdComPacienteECategoria(40L)).thenReturn(Optional.of(doc));

        UsuarioAutenticado outroPaciente = new UsuarioAutenticado(99L, "Outro", "outro@amde.example.org", NivelAcesso.PACIENTE);
        assertThatThrownBy(() -> prontuarioService.prepararDownload(40L, outroPaciente))
                .isInstanceOf(AccessDeniedException.class);
        verify(storageService, never()).carregar(any(), any());
        verify(auditoriaService, never()).registrarAsync(any(), any(), any());

        UsuarioAutenticado titular = new UsuarioAutenticado(25L, "Titular", "titular@amde.example.org", NivelAcesso.PACIENTE);
        ByteArrayResource pdfResource = new ByteArrayResource("%PDF-1.7".getBytes(StandardCharsets.US_ASCII));
        when(storageService.carregar(12L, "u4.pdf")).thenReturn(pdfResource);

        DownloadProntuarioResult resultado = prontuarioService.prepararDownload(40L, titular);
        assertThat(resultado.nomeArquivoOriginal()).isEqualTo("laudo-a.pdf");
        assertThat(resultado.recurso()).isEqualTo(pdfResource);
        verify(auditoriaService).registrarAsync(25L, AcaoAuditoria.DOWNLOAD, "documento:40:DOWNLOAD");

        when(storageService.carregar(12L, "u4.pdf")).thenThrow(new ErroStorageException("Ausente"));
        assertThatThrownBy(() -> prontuarioService.prepararDownload(40L, titular))
                .isInstanceOf(ErroStorageException.class);
    }

    @Test
    void deveExecutarExclusaoEm3PassosETratarFalhasParciais() {
        Paciente paciente = criarPaciente(12L, 25L);
        Categoria laudos = criarCategoria(2L, "Laudos");
        RegistroProntuario doc = criarRegistro(50L, paciente, laudos, "Laudo", "laudo.pdf", "u5.pdf");
        UsuarioAutenticado medico = new UsuarioAutenticado(2L, "Dra. Helena", "helena@amde.example.org", NivelAcesso.MEDICO);

        when(registroProntuarioRepository.findAtivoByIdComPacienteECategoria(50L)).thenReturn(Optional.of(doc));
        when(clientIpResolver.resolveClientIp()).thenReturn("127.0.0.1");

        prontuarioService.excluir(50L, medico);

        InOrder ordem = inOrder(auditoriaService, storageService, registroProntuarioRepository);
        ordem.verify(auditoriaService).registrarSincronoPreExclusao(2L, "documento:50:EXCLUSAO", "127.0.0.1");
        ordem.verify(storageService).remover(12L, "u5.pdf");
        ordem.verify(registroProntuarioRepository).saveAndFlush(doc);
        assertThat(doc.getExcluidoEm()).isNotNull();

        RegistroProntuario docFalhaAuditoria = criarRegistro(51L, paciente, laudos, "Laudo 2", "l2.pdf", "u51.pdf");
        when(registroProntuarioRepository.findAtivoByIdComPacienteECategoria(51L)).thenReturn(Optional.of(docFalhaAuditoria));
        doThrow(new ErroAuditoriaPreviaException("Falha auditoria", null))
                .when(auditoriaService).registrarSincronoPreExclusao(2L, "documento:51:EXCLUSAO", "127.0.0.1");

        assertThatThrownBy(() -> prontuarioService.excluir(51L, medico))
                .isInstanceOf(ErroAuditoriaPreviaException.class);
        verify(storageService, never()).remover(12L, "u51.pdf");
        assertThat(docFalhaAuditoria.getExcluidoEm()).isNull();

        RegistroProntuario docFalhaStorage = criarRegistro(52L, paciente, laudos, "Laudo 3", "l3.pdf", "u52.pdf");
        when(registroProntuarioRepository.findAtivoByIdComPacienteECategoria(52L)).thenReturn(Optional.of(docFalhaStorage));
        doThrow(new ErroStorageException("Falha disco")).when(storageService).remover(12L, "u52.pdf");

        assertThatThrownBy(() -> prontuarioService.excluir(52L, medico))
                .isInstanceOf(ErroStorageException.class);
        verify(registroProntuarioRepository, never()).saveAndFlush(docFalhaStorage);
        assertThat(docFalhaStorage.getExcluidoEm()).isNull();
    }

    private MockMultipartFile criarPdfMock(String nomeArquivo) {
        return new MockMultipartFile(
                "arquivo",
                nomeArquivo,
                "application/pdf",
                "%PDF-1.7\n1 0 obj\n<<>>\nendobj\n".getBytes(StandardCharsets.US_ASCII)
        );
    }

    private Paciente criarPaciente(Long pacienteId, Long usuarioId) {
        Usuario u = new Usuario();
        u.setId(usuarioId);
        u.setNome("Paciente Teste");
        u.setEmail("pac" + usuarioId + "@amde.example.org");
        u.setNivelAcesso(NivelAcesso.PACIENTE);

        Paciente p = new Paciente();
        p.setId(pacienteId);
        p.setUsuario(u);
        p.setGrauSuporte(GrauSuporte.NIVEL_1);
        return p;
    }

    private Categoria criarCategoria(Long id, String nome) {
        Categoria c = new Categoria();
        c.setId(id);
        c.setNome(nome);
        return c;
    }

    private RegistroProntuario criarRegistro(
            Long id,
            Paciente paciente,
            Categoria categoria,
            String titulo,
            String original,
            String armazenado
    ) {
        RegistroProntuario r = new RegistroProntuario();
        r.setId(id);
        r.setPaciente(paciente);
        r.setCategoria(categoria);
        r.setTituloDocumento(titulo);
        r.setNomeArquivoOriginal(original);
        r.setNomeArquivoArmazenado(armazenado);
        r.setCriadoEm(OffsetDateTime.now());
        r.setAtualizadoEm(OffsetDateTime.now());
        return r;
    }
}

