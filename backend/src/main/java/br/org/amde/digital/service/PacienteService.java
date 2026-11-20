package br.org.amde.digital.service;

import br.org.amde.digital.domain.entity.Paciente;
import br.org.amde.digital.domain.entity.RegistroProntuario;
import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.domain.enums.GrauSuporte;
import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.domain.enums.SituacaoDocumento;
import br.org.amde.digital.dto.paciente.AtualizarDadosClinicosRequest;
import br.org.amde.digital.dto.paciente.CategoriaHistoricoDTO;
import br.org.amde.digital.dto.paciente.DocumentoHistoricoResumoDTO;
import br.org.amde.digital.dto.paciente.ListaPacientesResponse;
import br.org.amde.digital.dto.paciente.PacienteFichaResponse;
import br.org.amde.digital.dto.paciente.PacienteResumoProjection;
import br.org.amde.digital.dto.paciente.PacienteResumoResponse;
import br.org.amde.digital.exception.RecursoNaoEncontradoException;
import br.org.amde.digital.exception.ValidacaoCampoException;
import br.org.amde.digital.exception.ValidacaoCampoException.CampoErro;
import br.org.amde.digital.repository.PacienteRepository;
import br.org.amde.digital.repository.RegistroProntuarioRepository;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.usuario.CelularMasker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private static final int TAMANHO_PAGINA_MAXIMO = 50;
    private static final int TAMANHO_TERMO_MAXIMO = 100;
    private static final int TAMANHO_DESCRICAO_MAXIMO = 5000;
    private static final List<String> CATEGORIAS_OFICIAIS = List.of("Exames", "Laudos", "Receituario", "Arquivado");
    private static final Map<String, String> CAMPOS_ORDENACAO = Map.of(
            "nome", "usuario.nome",
            "email", "usuario.email",
            "grauSuporte", "grauSuporte"
    );

    private final PacienteRepository pacienteRepository;
    private final RegistroProntuarioRepository registroProntuarioRepository;
    private final CelularMasker celularMasker;

    @Transactional(readOnly = true)
    public ListaPacientesResponse listar(
            String termo,
            GrauSuporte grauSuporte,
            int page,
            int size,
            String sort,
            String direction
    ) {
        List<CampoErro> erros = new ArrayList<>();
        String padraoLike = normalizarPadraoBusca(termo, erros);

        if (page < 0) {
            erros.add(new CampoErro("page", "Página deve ser maior ou igual a zero"));
        }
        if (size < 1) {
            erros.add(new CampoErro("size", "Tamanho da página deve ser maior ou igual a 1"));
        }

        String campoSort = (sort == null || sort.isBlank()) ? "nome" : sort.trim();
        String propriedadeSort = CAMPOS_ORDENACAO.get(campoSort);
        if (propriedadeSort == null) {
            erros.add(new CampoErro("sort", "Campo de ordenação inválido"));
        }

        Sort.Direction sortDirection = parseDirection(direction, erros);
        if (!erros.isEmpty()) {
            throw new ValidacaoCampoException(erros);
        }

        int tamanhoEfetivo = Math.min(size, TAMANHO_PAGINA_MAXIMO);
        Pageable pageable = PageRequest.of(page, tamanhoEfetivo, Sort.by(sortDirection, propriedadeSort));
        Page<PacienteResumoProjection> resultado = pacienteRepository.buscarAtivosPaginado(padraoLike, grauSuporte, pageable);

        List<PacienteResumoResponse> itens = resultado.getContent().stream()
                .map(p -> new PacienteResumoResponse(
                        p.id(),
                        p.usuarioId(),
                        p.nome(),
                        p.email(),
                        p.grauSuporte(),
                        celularMasker.mascarar(p.numCelular())
                ))
                .toList();

        return new ListaPacientesResponse(
                itens,
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalElements(),
                resultado.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PacienteFichaResponse buscarFicha(
            Long id,
            UsuarioAutenticado autenticado,
            boolean revelarCelular,
            int docPage,
            int docSize
    ) {
        int docSizeEfetivo = validarPaginacaoDocumentos(docPage, docSize);
        if (autenticado == null) {
            throw new AccessDeniedException("Acesso negado para o perfil autenticado");
        }

        Paciente paciente;
        boolean podeRevelarCelular;

        if (autenticado.nivelAcesso() == NivelAcesso.PACIENTE) {
            Paciente proprio = pacienteRepository.findAtivoByUsuarioIdComUsuario(autenticado.id())
                    .orElseThrow(() -> new AccessDeniedException("Acesso negado para o perfil autenticado"));
            if (!proprio.getId().equals(id)) {
                throw new AccessDeniedException("Paciente não pode acessar ficha de outro paciente");
            }
            paciente = proprio;
            podeRevelarCelular = false;
        } else {
            paciente = pacienteRepository.findAtivoByIdComUsuario(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado"));
            podeRevelarCelular = revelarCelular;
        }

        return montarFicha(paciente, podeRevelarCelular, docPage, docSizeEfetivo);
    }

    @Transactional(readOnly = true)
    public PacienteFichaResponse buscarMeuProntuario(UsuarioAutenticado autenticado, int docPage, int docSize) {
        int docSizeEfetivo = validarPaginacaoDocumentos(docPage, docSize);
        if (autenticado == null || autenticado.nivelAcesso() != NivelAcesso.PACIENTE) {
            throw new AccessDeniedException("Acesso negado para o perfil autenticado");
        }

        Paciente paciente = pacienteRepository.findAtivoByUsuarioIdComUsuario(autenticado.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ficha do paciente não encontrada"));

        return montarFicha(paciente, false, docPage, docSizeEfetivo);
    }

    @Transactional
    public PacienteFichaResponse atualizarDadosClinicos(Long id, AtualizarDadosClinicosRequest request) {
        List<CampoErro> erros = new ArrayList<>();
        if (request == null || request.grauSuporte() == null) {
            erros.add(new CampoErro("grauSuporte", "Grau de suporte é obrigatório"));
        }

        String descricaoNormalizada = null;
        if (request != null && request.descricao() != null) {
            String trimmed = request.descricao().trim();
            if (!trimmed.isEmpty()) {
                if (trimmed.length() > TAMANHO_DESCRICAO_MAXIMO) {
                    erros.add(new CampoErro("descricao", "Descrição clínica deve ter no máximo 5000 caracteres"));
                } else {
                    descricaoNormalizada = trimmed;
                }
            }
        }

        if (!erros.isEmpty()) {
            throw new ValidacaoCampoException(erros);
        }

        Paciente paciente = pacienteRepository.findAtivoByIdComUsuario(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado"));

        paciente.setGrauSuporte(request.grauSuporte());
        paciente.setDescricao(descricaoNormalizada);
        Paciente salvo = pacienteRepository.saveAndFlush(paciente);

        return montarFicha(salvo, false, 0, 10);
    }

    private PacienteFichaResponse montarFicha(
            Paciente paciente,
            boolean revelarCelular,
            int docPage,
            int docSize
    ) {
        Usuario usuario = paciente.getUsuario();
        String rawCelular = usuario.getNumCelular();
        boolean temCelular = rawCelular != null && !rawCelular.isBlank();
        String celularFormatado = !temCelular
                ? null
                : (revelarCelular ? celularMasker.formatarCompleto(rawCelular) : celularMasker.mascarar(rawCelular));
        boolean celularMascarado = temCelular && !revelarCelular;

        List<CategoriaHistoricoDTO> historico = montarHistoricoPorCategoria(paciente.getId(), docPage, docSize);

        return new PacienteFichaResponse(
                paciente.getId(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getDataNascimento(),
                celularFormatado,
                celularMascarado,
                usuario.getPais(),
                usuario.getEstado(),
                usuario.getCidade(),
                usuario.getRua(),
                paciente.getGrauSuporte(),
                paciente.getDescricao(),
                historico,
                paciente.getAtualizadoEm()
        );
    }

    private List<CategoriaHistoricoDTO> montarHistoricoPorCategoria(Long pacienteId, int docPage, int docSize) {
        List<RegistroProntuario> ativos = registroProntuarioRepository.findAtivosByPacienteIdComCategoria(pacienteId);

        Set<Long> idsSubstituidos = ativos.stream()
                .filter(r -> r.getRegistroSubstituido() != null)
                .map(r -> r.getRegistroSubstituido().getId())
                .collect(Collectors.toSet());

        Map<String, List<DocumentoHistoricoResumoDTO>> agrupados = new LinkedHashMap<>();
        for (String categoria : CATEGORIAS_OFICIAIS) {
            agrupados.put(categoria, new ArrayList<>());
        }

        for (RegistroProntuario registro : ativos) {
            String nomeCategoria = resolverCategoriaOficial(registro.getCategoria().getNome());
            SituacaoDocumento situacao = calcularSituacao(registro, nomeCategoria, idsSubstituidos);
            Long substituidoId = registro.getRegistroSubstituido() != null
                    ? registro.getRegistroSubstituido().getId()
                    : null;

            agrupados.computeIfAbsent(nomeCategoria, k -> new ArrayList<>())
                    .add(new DocumentoHistoricoResumoDTO(
                            registro.getId(),
                            registro.getTituloDocumento(),
                            registro.getNomeArquivoOriginal(),
                            nomeCategoria,
                            situacao,
                            substituidoId,
                            registro.getCriadoEm()
                    ));
        }

        List<CategoriaHistoricoDTO> resultado = new ArrayList<>(CATEGORIAS_OFICIAIS.size());
        for (String categoria : CATEGORIAS_OFICIAIS) {
            List<DocumentoHistoricoResumoDTO> docs = agrupados.getOrDefault(categoria, List.of());
            long total = docs.size();
            int totalPaginas = total == 0 ? 0 : (int) Math.ceil((double) total / docSize);
            int inicio = Math.min(docPage * docSize, docs.size());
            int fim = Math.min(inicio + docSize, docs.size());

            resultado.add(new CategoriaHistoricoDTO(
                    categoria,
                    total,
                    docPage,
                    docSize,
                    totalPaginas,
                    docs.subList(inicio, fim)
            ));
        }

        return resultado;
    }

    private SituacaoDocumento calcularSituacao(
            RegistroProntuario registro,
            String nomeCategoria,
            Set<Long> idsSubstituidos
    ) {
        if ("Arquivado".equalsIgnoreCase(nomeCategoria)) {
            return SituacaoDocumento.ARQUIVADO;
        }
        if (idsSubstituidos.contains(registro.getId())) {
            return SituacaoDocumento.SUBSTITUIDO;
        }
        return SituacaoDocumento.VIGENTE;
    }

    private String resolverCategoriaOficial(String nomeBruto) {
        if (nomeBruto == null) {
            return "Arquivado";
        }
        for (String oficial : CATEGORIAS_OFICIAIS) {
            if (oficial.equalsIgnoreCase(nomeBruto.trim())) {
                return oficial;
            }
        }
        return nomeBruto.trim();
    }

    private int validarPaginacaoDocumentos(int docPage, int docSize) {
        List<CampoErro> erros = new ArrayList<>();
        if (docPage < 0) {
            erros.add(new CampoErro("docPage", "Página de documentos deve ser maior ou igual a zero"));
        }
        if (docSize < 1) {
            erros.add(new CampoErro("docSize", "Tamanho da página de documentos deve ser maior ou igual a 1"));
        }
        if (!erros.isEmpty()) {
            throw new ValidacaoCampoException(erros);
        }
        return Math.min(docSize, TAMANHO_PAGINA_MAXIMO);
    }

    private String normalizarPadraoBusca(String termo, List<CampoErro> erros) {
        if (termo == null || termo.isBlank()) {
            return null;
        }
        String trimmed = termo.trim();
        if (trimmed.length() > TAMANHO_TERMO_MAXIMO) {
            erros.add(new CampoErro("termo", "Termo de busca deve ter no máximo 100 caracteres"));
            return null;
        }
        String semAcento = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        String escapado = semAcento
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return "%" + escapado + "%";
    }

    private Sort.Direction parseDirection(String direction, List<CampoErro> erros) {
        if (direction == null || direction.isBlank()) {
            return Sort.Direction.ASC;
        }
        try {
            return Sort.Direction.fromString(direction.trim());
        } catch (IllegalArgumentException ex) {
            erros.add(new CampoErro("direction", "Direção de ordenação deve ser ASC ou DESC"));
            return Sort.Direction.ASC;
        }
    }
}

