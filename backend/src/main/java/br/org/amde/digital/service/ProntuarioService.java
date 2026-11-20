package br.org.amde.digital.service;

import br.org.amde.digital.domain.entity.Categoria;
import br.org.amde.digital.domain.entity.Paciente;
import br.org.amde.digital.domain.entity.RegistroProntuario;
import br.org.amde.digital.domain.enums.AcaoAuditoria;
import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.domain.enums.SituacaoDocumento;
import br.org.amde.digital.dto.prontuario.ProntuarioResponse;
import br.org.amde.digital.dto.prontuario.ReclassificarCategoriaRequest;
import br.org.amde.digital.exception.ConflitoException;
import br.org.amde.digital.exception.ErroStorageException;
import br.org.amde.digital.exception.RecursoNaoEncontradoException;
import br.org.amde.digital.exception.ValidacaoCampoException;
import br.org.amde.digital.exception.ValidacaoCampoException.CampoErro;
import br.org.amde.digital.repository.CategoriaRepository;
import br.org.amde.digital.repository.PacienteRepository;
import br.org.amde.digital.repository.RegistroProntuarioRepository;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.auditoria.AuditoriaService;
import br.org.amde.digital.service.auditoria.ClientIpResolver;
import br.org.amde.digital.service.auditoria.RecursoAuditoriaFormatter;
import br.org.amde.digital.service.prontuario.PdfValidator;
import br.org.amde.digital.service.storage.ProntuarioStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProntuarioService {

    private static final int TAMANHO_TITULO_MINIMO = 2;
    private static final int TAMANHO_TITULO_MAXIMO = 150;
    private static final Map<String, String> CATEGORIAS_OFICIAIS = Map.of(
            "exames", "Exames",
            "laudos", "Laudos",
            "receituario", "Receituario",
            "receituário", "Receituario",
            "arquivado", "Arquivado"
    );

    private final RegistroProntuarioRepository registroProntuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final PdfValidator pdfValidator;
    private final ProntuarioStorageService storageService;
    private final AuditoriaService auditoriaService;
    private final RecursoAuditoriaFormatter recursoFormatter;
    private final ClientIpResolver clientIpResolver;

    @Transactional
    public ProntuarioResponse enviar(
            Long pacienteId,
            String tituloDocumento,
            String nomeCategoria,
            Long registroSubstituidoId,
            MultipartFile arquivo,
            UsuarioAutenticado autenticado
    ) {
        List<CampoErro> erros = new ArrayList<>();
        if (pacienteId == null || pacienteId <= 0) {
            erros.add(new CampoErro("pacienteId", "Paciente é obrigatório"));
        }
        String tituloNormalizado = validarTituloObrigatorio(tituloDocumento, erros);
        String categoriaOficial = normalizarCategoria(nomeCategoria, erros);
        if (!erros.isEmpty()) {
            throw new ValidacaoCampoException(erros);
        }

        String nomeArquivoOriginal = pdfValidator.validar(arquivo);

        Paciente paciente = pacienteRepository.findAtivoByIdComUsuario(pacienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado"));
        Categoria categoria = categoriaRepository.findByNome(categoriaOficial)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));

        RegistroProntuario substituido = null;
        if (registroSubstituidoId != null) {
            substituido = validarSubstituicaoAlvo(registroSubstituidoId, paciente.getId());
        }

        RegistroProntuario salvo = gravarDocumentoComCompensacao(
                paciente,
                categoria,
                substituido,
                tituloNormalizado,
                nomeArquivoOriginal,
                arquivo
        );

        String recurso = substituido != null
                ? recursoFormatter.formatarSubstituicaoDocumento(salvo.getId())
                : recursoFormatter.formatarUploadDocumento(salvo.getId());
        auditoriaService.registrarAsync(autenticado.id(), AcaoAuditoria.UPLOAD, recurso);

        return toResponse(salvo, false);
    }

    @Transactional
    public ProntuarioResponse substituir(
            Long documentoId,
            String tituloOpcional,
            MultipartFile arquivo,
            UsuarioAutenticado autenticado
    ) {
        String tituloPersonalizado = null;
        if (tituloOpcional != null && !tituloOpcional.trim().isEmpty()) {
            String trimmed = tituloOpcional.trim();
            if (trimmed.length() < TAMANHO_TITULO_MINIMO || trimmed.length() > TAMANHO_TITULO_MAXIMO) {
                throw new ValidacaoCampoException(
                        "tituloDocumento",
                        "Título do documento deve ter entre 2 e 150 caracteres"
                );
            }
            tituloPersonalizado = trimmed;
        }

        String nomeArquivoOriginal = pdfValidator.validar(arquivo);

        RegistroProntuario anterior = registroProntuarioRepository.findAtivoByIdForUpdate(documentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento não encontrado"));

        if (anterior.getPaciente().getExcluidoEm() != null
                || anterior.getPaciente().getUsuario().getExcluidoEm() != null) {
            throw new RecursoNaoEncontradoException("Paciente não encontrado");
        }

        validarElegibilidadeSubstituicao(anterior);

        String tituloFinal = tituloPersonalizado != null ? tituloPersonalizado : anterior.getTituloDocumento();
        RegistroProntuario salvo = gravarDocumentoComCompensacao(
                anterior.getPaciente(),
                anterior.getCategoria(),
                anterior,
                tituloFinal,
                nomeArquivoOriginal,
                arquivo
        );

        auditoriaService.registrarAsync(
                autenticado.id(),
                AcaoAuditoria.UPLOAD,
                recursoFormatter.formatarSubstituicaoDocumento(salvo.getId())
        );

        return toResponse(salvo, false);
    }

    @Transactional
    public ProntuarioResponse reclassificarCategoria(Long documentoId, ReclassificarCategoriaRequest request) {
        List<CampoErro> erros = new ArrayList<>();
        String categoriaOficial = normalizarCategoria(request != null ? request.categoria() : null, erros);
        if (!erros.isEmpty()) {
            throw new ValidacaoCampoException(erros);
        }

        RegistroProntuario documento = registroProntuarioRepository.findAtivoByIdComPacienteECategoria(documentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento não encontrado"));
        Categoria novaCategoria = categoriaRepository.findByNome(categoriaOficial)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));

        documento.setCategoria(novaCategoria);
        RegistroProntuario salvo = registroProntuarioRepository.saveAndFlush(documento);
        boolean substituido = registroProntuarioRepository.existsSubstitutoAtivoPara(salvo.getId());
        return toResponse(salvo, substituido);
    }

    @Transactional(readOnly = true)
    public DownloadProntuarioResult prepararDownload(Long documentoId, UsuarioAutenticado autenticado) {
        if (autenticado == null) {
            throw new AccessDeniedException("Acesso negado para o perfil autenticado");
        }

        RegistroProntuario documento = registroProntuarioRepository.findAtivoByIdComPacienteECategoria(documentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento não encontrado"));

        if (autenticado.nivelAcesso() == NivelAcesso.PACIENTE
                && !documento.getPaciente().getUsuario().getId().equals(autenticado.id())) {
            throw new AccessDeniedException("Paciente não pode baixar documento de outro paciente");
        }

        Resource recurso = storageService.carregar(
                documento.getPaciente().getId(),
                documento.getNomeArquivoArmazenado()
        );

        auditoriaService.registrarAsync(
                autenticado.id(),
                AcaoAuditoria.DOWNLOAD,
                recursoFormatter.formatarDownloadDocumento(documento.getId())
        );

        return new DownloadProntuarioResult(recurso, documento.getNomeArquivoOriginal());
    }

    @Transactional
    public void excluir(Long documentoId, UsuarioAutenticado autenticado) {
        if (autenticado == null) {
            throw new AccessDeniedException("Acesso negado para o perfil autenticado");
        }

        RegistroProntuario documento = registroProntuarioRepository.findAtivoByIdComPacienteECategoria(documentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento não encontrado"));

        auditoriaService.registrarSincronoPreExclusao(
                autenticado.id(),
                recursoFormatter.formatarExclusaoDocumento(documento.getId()),
                clientIpResolver.resolveClientIp()
        );

        storageService.remover(documento.getPaciente().getId(), documento.getNomeArquivoArmazenado());

        documento.setExcluidoEm(OffsetDateTime.now());
        registroProntuarioRepository.saveAndFlush(documento);
    }

    private RegistroProntuario gravarDocumentoComCompensacao(
            Paciente paciente,
            Categoria categoria,
            RegistroProntuario substituido,
            String tituloDocumento,
            String nomeArquivoOriginal,
            MultipartFile arquivo
    ) {
        String nomeArquivoArmazenado = UUID.randomUUID() + ".pdf";
        try (InputStream stream = arquivo.getInputStream()) {
            storageService.armazenar(paciente.getId(), nomeArquivoArmazenado, stream);
        } catch (IOException ex) {
            storageService.removerSilencioso(paciente.getId(), nomeArquivoArmazenado);
            throw new ErroStorageException("Falha ao ler arquivo enviado", ex);
        }

        registrarLimpezaRollback(paciente.getId(), nomeArquivoArmazenado);

        try {
            RegistroProntuario novo = new RegistroProntuario();
            novo.setPaciente(paciente);
            novo.setCategoria(categoria);
            novo.setRegistroSubstituido(substituido);
            novo.setTituloDocumento(tituloDocumento);
            novo.setNomeArquivoOriginal(nomeArquivoOriginal);
            novo.setNomeArquivoArmazenado(nomeArquivoArmazenado);
            return registroProntuarioRepository.saveAndFlush(novo);
        } catch (RuntimeException ex) {
            storageService.removerSilencioso(paciente.getId(), nomeArquivoArmazenado);
            throw ex;
        }
    }

    private void registrarLimpezaRollback(Long pacienteId, String nomeArquivoArmazenado) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    storageService.removerSilencioso(pacienteId, nomeArquivoArmazenado);
                }
            }
        });
    }

    private RegistroProntuario validarSubstituicaoAlvo(Long documentoId, Long pacienteId) {
        RegistroProntuario alvo = registroProntuarioRepository.findAtivoByIdForUpdate(documentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento a substituir não encontrado"));
        if (!alvo.getPaciente().getId().equals(pacienteId)) {
            throw new ValidacaoCampoException(
                    "registroSubstituidoId",
                    "Documento substituído pertence a outro paciente"
            );
        }
        validarElegibilidadeSubstituicao(alvo);
        return alvo;
    }

    private void validarElegibilidadeSubstituicao(RegistroProntuario alvo) {
        if ("Arquivado".equalsIgnoreCase(alvo.getCategoria().getNome())) {
            throw new ConflitoException("Documento arquivado não pode ser substituído");
        }
        if (registroProntuarioRepository.existsSubstitutoAtivoPara(alvo.getId())) {
            throw new ConflitoException("Documento já foi substituído por outra versão");
        }
    }

    private String validarTituloObrigatorio(String titulo, List<CampoErro> erros) {
        if (titulo == null || titulo.trim().isEmpty()) {
            erros.add(new CampoErro("tituloDocumento", "Título do documento é obrigatório"));
            return null;
        }
        String trimmed = titulo.trim();
        if (trimmed.length() < TAMANHO_TITULO_MINIMO || trimmed.length() > TAMANHO_TITULO_MAXIMO) {
            erros.add(new CampoErro("tituloDocumento", "Título do documento deve ter entre 2 e 150 caracteres"));
        }
        return trimmed;
    }

    private String normalizarCategoria(String nomeCategoria, List<CampoErro> erros) {
        if (nomeCategoria == null || nomeCategoria.trim().isEmpty()) {
            erros.add(new CampoErro("categoria", "Categoria é obrigatória"));
            return null;
        }
        String oficial = CATEGORIAS_OFICIAIS.get(nomeCategoria.trim().toLowerCase(Locale.ROOT));
        if (oficial == null) {
            erros.add(new CampoErro("categoria", "Categoria inválida"));
        }
        return oficial;
    }

    private ProntuarioResponse toResponse(RegistroProntuario doc, boolean substituido) {
        String nomeCategoria = doc.getCategoria().getNome();
        SituacaoDocumento situacao;
        if ("Arquivado".equalsIgnoreCase(nomeCategoria)) {
            situacao = SituacaoDocumento.ARQUIVADO;
        } else if (substituido) {
            situacao = SituacaoDocumento.SUBSTITUIDO;
        } else {
            situacao = SituacaoDocumento.VIGENTE;
        }
        Long substituidoId = doc.getRegistroSubstituido() != null ? doc.getRegistroSubstituido().getId() : null;
        return new ProntuarioResponse(
                doc.getId(),
                doc.getPaciente().getId(),
                nomeCategoria,
                substituidoId,
                doc.getTituloDocumento(),
                doc.getNomeArquivoOriginal(),
                situacao,
                doc.getCriadoEm(),
                doc.getAtualizadoEm()
        );
    }

    public record DownloadProntuarioResult(Resource recurso, String nomeArquivoOriginal) {
    }
}
