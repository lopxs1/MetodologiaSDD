package br.org.amde.digital.service;

import br.org.amde.digital.domain.entity.Agendamento;
import br.org.amde.digital.domain.entity.AgendamentoPaciente;
import br.org.amde.digital.domain.entity.AgendamentoPacienteId;
import br.org.amde.digital.domain.entity.Funcionario;
import br.org.amde.digital.domain.entity.Paciente;
import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.domain.enums.AcaoAuditoria;
import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.domain.enums.StatusAgendamento;
import br.org.amde.digital.domain.enums.TipoAgendamento;
import br.org.amde.digital.dto.agendamento.AgendamentoPacienteDTO;
import br.org.amde.digital.dto.agendamento.AgendamentoResponse;
import br.org.amde.digital.dto.agendamento.CriarAgendamentoRequest;
import br.org.amde.digital.dto.agendamento.EditarAgendamentoRequest;
import br.org.amde.digital.dto.agendamento.ListaAgendamentosResponse;
import br.org.amde.digital.dto.agendamento.PresencaPacienteInputDTO;
import br.org.amde.digital.dto.agendamento.RegistrarPresencaRequest;
import br.org.amde.digital.exception.ConflitoException;
import br.org.amde.digital.exception.RecursoNaoEncontradoException;
import br.org.amde.digital.exception.ValidacaoCampoException;
import br.org.amde.digital.exception.ValidacaoCampoException.CampoErro;
import br.org.amde.digital.repository.AgendamentoPacienteRepository;
import br.org.amde.digital.repository.AgendamentoRepository;
import br.org.amde.digital.repository.FuncionarioRepository;
import br.org.amde.digital.repository.PacienteRepository;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.auditoria.AuditoriaService;
import br.org.amde.digital.service.auditoria.RecursoAuditoriaFormatter;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private static final int TAMANHO_PAGINA_MAXIMO = 50;
    private static final int TAMANHO_TERMO_MAXIMO = 100;
    private static final Set<String> CAMPOS_ORDENACAO = Set.of("dataInicio", "dataFim", "status", "tipoAgendamento");

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoPacienteRepository agendamentoPacienteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PacienteRepository pacienteRepository;
    private final AuditoriaService auditoriaService;
    private final RecursoAuditoriaFormatter recursoFormatter;

    @Transactional
    public AgendamentoResponse criar(CriarAgendamentoRequest request, UsuarioAutenticado autenticado) {
        Long funcionarioIdAlvo = resolverFuncionarioParaCriacao(request.funcionarioId(), autenticado);
        validarDadosBasicosSessao(request.dataInicio(), request.dataFim(), request.pacientesIds());

        Funcionario funcionario = funcionarioRepository.findAtivoByIdForUpdate(funcionarioIdAlvo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado ou inativo"));
        if (funcionario.getUsuario().getNivelAcesso() != NivelAcesso.MEDICO) {
            throw new ValidacaoCampoException(
                    "funcionarioId",
                    "O funcionário informado não possui perfil clínico habilitado para agenda"
            );
        }

        List<Paciente> pacientes = carregarPacientesAtivos(request.pacientesIds());

        if (agendamentoRepository.existsConflitoHorario(
                funcionario.getId(), request.dataInicio(), request.dataFim(), null)) {
            throw new ConflitoException("Já existe um agendamento conflitante neste horário para o profissional");
        }

        Agendamento agendamento = new Agendamento();
        agendamento.setFuncionario(funcionario);
        agendamento.setDataInicio(request.dataInicio());
        agendamento.setDataFim(request.dataFim());
        agendamento.setTipoAgendamento(request.tipoAgendamento());
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setPossuiPresencaFuncionario(false);

        Agendamento salvo = agendamentoRepository.saveAndFlush(agendamento);

        List<AgendamentoPaciente> vinculos = new ArrayList<>(pacientes.size());
        for (Paciente paciente : pacientes) {
            AgendamentoPaciente vinculo = new AgendamentoPaciente();
            vinculo.setId(new AgendamentoPacienteId(salvo.getId(), paciente.getId()));
            vinculo.setAgendamento(salvo);
            vinculo.setPaciente(paciente);
            vinculo.setPossuiPresenca(false);
            vinculos.add(vinculo);
            salvo.getPacientes().add(vinculo);
        }
        agendamentoPacienteRepository.saveAllAndFlush(vinculos);

        auditoriaService.registrarAsync(
                autenticado.id(),
                AcaoAuditoria.AGENDAMENTO,
                recursoFormatter.formatarAgendamento(salvo.getId(), "CRIAR")
        );

        return toResponse(salvo, null);
    }

    @Transactional(readOnly = true)
    public ListaAgendamentosResponse listar(
            String termo,
            OffsetDateTime dataInicio,
            OffsetDateTime dataFim,
            StatusAgendamento status,
            TipoAgendamento tipoAgendamento,
            Long funcionarioId,
            int page,
            int size,
            String sort,
            String direction,
            UsuarioAutenticado autenticado
    ) {
        List<CampoErro> erros = new ArrayList<>();
        String padraoLike = normalizarPadraoBusca(termo, erros);

        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            erros.add(new CampoErro("dataFim", "A data final do filtro deve ser posterior ou igual à inicial"));
        }
        if (page < 0) {
            erros.add(new CampoErro("page", "Página deve ser maior ou igual a zero"));
        }
        if (size < 1) {
            erros.add(new CampoErro("size", "Tamanho da página deve ser maior ou igual a 1"));
        }

        String campoSort = (sort == null || sort.isBlank()) ? "dataInicio" : sort.trim();
        if (!CAMPOS_ORDENACAO.contains(campoSort)) {
            erros.add(new CampoErro("sort", "Campo de ordenação inválido"));
        }
        Sort.Direction sortDirection = parseDirection(direction, erros);

        if (!erros.isEmpty()) {
            throw new ValidacaoCampoException(erros);
        }

        Long escopoFuncionarioId = null;
        Long escopoPacienteId = null;
        if (autenticado.nivelAcesso() == NivelAcesso.MEDICO) {
            escopoFuncionarioId = resolverMeuFuncionario(autenticado.id()).getId();
        } else if (autenticado.nivelAcesso() == NivelAcesso.PACIENTE) {
            escopoPacienteId = resolverMeuPaciente(autenticado.id()).getId();
        } else {
            escopoFuncionarioId = funcionarioId;
        }

        final Long filtroFuncionarioId = escopoFuncionarioId;
        final Long filtroPacienteId = escopoPacienteId;

        Specification<Agendamento> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtroFuncionarioId != null) {
                predicates.add(cb.equal(root.get("funcionario").get("id"), filtroFuncionarioId));
            }
            if (filtroPacienteId != null) {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<AgendamentoPaciente> apRoot = sub.from(AgendamentoPaciente.class);
                sub.select(cb.literal(1L));
                sub.where(
                        cb.equal(apRoot.get("agendamento"), root),
                        cb.equal(apRoot.get("paciente").get("id"), filtroPacienteId)
                );
                predicates.add(cb.exists(sub));
            }
            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataInicio"), dataInicio));
            }
            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataInicio"), dataFim));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (tipoAgendamento != null) {
                predicates.add(cb.equal(root.get("tipoAgendamento"), tipoAgendamento));
            }
            if (padraoLike != null) {
                Join<Agendamento, Funcionario> funcJoin = root.join("funcionario");
                Join<Funcionario, Usuario> funcUserJoin = funcJoin.join("usuario");
                Expression<String> nomeProfExpr = cb.function(
                        "public.unaccent",
                        String.class,
                        cb.lower(funcUserJoin.get("nome"))
                );
                Predicate matchProf = cb.like(nomeProfExpr, padraoLike, '!');

                Subquery<Long> subPac = query.subquery(Long.class);
                Root<AgendamentoPaciente> apSub = subPac.from(AgendamentoPaciente.class);
                Join<AgendamentoPaciente, Paciente> pacJoin = apSub.join("paciente");
                Join<Paciente, Usuario> pacUserJoin = pacJoin.join("usuario");
                Expression<String> nomePacExpr = cb.function(
                        "public.unaccent",
                        String.class,
                        cb.lower(pacUserJoin.get("nome"))
                );
                List<Predicate> subPreds = new ArrayList<>();
                subPreds.add(cb.equal(apSub.get("agendamento"), root));
                if (filtroPacienteId != null) {
                    subPreds.add(cb.equal(pacJoin.get("id"), filtroPacienteId));
                }
                subPreds.add(cb.like(nomePacExpr, padraoLike, '!'));
                subPac.select(cb.literal(1L)).where(subPreds.toArray(Predicate[]::new));

                predicates.add(cb.or(matchProf, cb.exists(subPac)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };

        int tamanhoEfetivo = Math.min(size, TAMANHO_PAGINA_MAXIMO);
        Pageable pageable = PageRequest.of(
                page,
                tamanhoEfetivo,
                Sort.by(sortDirection, campoSort).and(Sort.by(Sort.Direction.ASC, "id"))
        );

        Page<Agendamento> paginaResultado = agendamentoRepository.findAll(spec, pageable);
        List<Long> ids = paginaResultado.getContent().stream().map(Agendamento::getId).toList();
        Map<Long, Agendamento> detalhesPorId = ids.isEmpty()
                ? Map.of()
                : agendamentoRepository.findAllByIdInComDetalhes(ids).stream()
                        .collect(Collectors.toMap(Agendamento::getId, Function.identity()));

        List<AgendamentoResponse> itens = paginaResultado.getContent().stream()
                .map(a -> toResponse(detalhesPorId.getOrDefault(a.getId(), a), filtroPacienteId))
                .toList();

        return new ListaAgendamentosResponse(
                itens,
                paginaResultado.getNumber(),
                paginaResultado.getSize(),
                paginaResultado.getTotalElements(),
                paginaResultado.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public AgendamentoResponse consultarPorId(Long id, UsuarioAutenticado autenticado) {
        Agendamento agendamento = buscarAgendamentoComDetalhes(id);
        Long pacienteEscopoId = validarAcessoLeitura(agendamento, autenticado);
        return toResponse(agendamento, pacienteEscopoId);
    }

    @Transactional
    public AgendamentoResponse editar(Long id, EditarAgendamentoRequest request, UsuarioAutenticado autenticado) {
        Agendamento agendamento = buscarAgendamentoComDetalhes(id);
        validarAcessoEscritaAgenda(agendamento, autenticado);

        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new ConflitoException("Apenas agendamentos em situação AGENDADO podem ser editados");
        }
        if (request.funcionarioId() != null
                && !request.funcionarioId().equals(agendamento.getFuncionario().getId())) {
            throw new ValidacaoCampoException(
                    "funcionarioId",
                    "Não é permitido alterar o profissional responsável na edição"
            );
        }

        validarDadosBasicosSessao(request.dataInicio(), request.dataFim(), request.pacientesIds());

        Funcionario funcionario = funcionarioRepository.findAtivoByIdForUpdate(agendamento.getFuncionario().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional responsável não encontrado ou inativo"));

        List<Paciente> novosPacientes = carregarPacientesAtivos(request.pacientesIds());

        if (agendamentoRepository.existsConflitoHorario(
                funcionario.getId(), request.dataInicio(), request.dataFim(), agendamento.getId())) {
            throw new ConflitoException("Já existe um agendamento conflitante neste horário para o profissional");
        }

        agendamento.setDataInicio(request.dataInicio());
        agendamento.setDataFim(request.dataFim());
        agendamento.setTipoAgendamento(request.tipoAgendamento());

        Set<Long> novosIds = new HashSet<>(request.pacientesIds());
        Set<Long> idsExistentes = new HashSet<>();
        Iterator<AgendamentoPaciente> it = agendamento.getPacientes().iterator();
        while (it.hasNext()) {
            AgendamentoPaciente atual = it.next();
            Long pacId = atual.getPaciente().getId();
            if (!novosIds.contains(pacId)) {
                it.remove();
                agendamentoPacienteRepository.delete(atual);
            } else {
                idsExistentes.add(pacId);
            }
        }
        agendamentoPacienteRepository.flush();

        List<AgendamentoPaciente> adicionados = new ArrayList<>();
        for (Paciente paciente : novosPacientes) {
            if (!idsExistentes.contains(paciente.getId())) {
                AgendamentoPaciente novo = new AgendamentoPaciente();
                novo.setId(new AgendamentoPacienteId(agendamento.getId(), paciente.getId()));
                novo.setAgendamento(agendamento);
                novo.setPaciente(paciente);
                novo.setPossuiPresenca(false);
                adicionados.add(novo);
                agendamento.getPacientes().add(novo);
            }
        }
        if (!adicionados.isEmpty()) {
            agendamentoPacienteRepository.saveAllAndFlush(adicionados);
        }

        Agendamento salvo = agendamentoRepository.saveAndFlush(agendamento);
        auditoriaService.registrarAsync(
                autenticado.id(),
                AcaoAuditoria.AGENDAMENTO,
                recursoFormatter.formatarAgendamento(salvo.getId(), "EDITAR")
        );

        return toResponse(salvo, null);
    }

    @Transactional
    public void cancelar(Long id, UsuarioAutenticado autenticado) {
        Agendamento agendamento = buscarAgendamentoComDetalhes(id);
        validarAcessoEscritaAgenda(agendamento, autenticado);

        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new ConflitoException("Apenas agendamentos em situação AGENDADO podem ser cancelados");
        }

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamentoRepository.saveAndFlush(agendamento);

        auditoriaService.registrarAsync(
                autenticado.id(),
                AcaoAuditoria.AGENDAMENTO,
                recursoFormatter.formatarAgendamento(agendamento.getId(), "CANCELAR")
        );
    }

    @Transactional
    public AgendamentoResponse registrarPresenca(
            Long id,
            RegistrarPresencaRequest request,
            UsuarioAutenticado autenticado
    ) {
        Agendamento agendamento = buscarAgendamentoComDetalhes(id);
        validarAcessoEscritaAgenda(agendamento, autenticado);

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new ConflitoException("Não é possível registrar presença em agendamento cancelado");
        }
        if (OffsetDateTime.now().isBefore(agendamento.getDataInicio())) {
            throw new ConflitoException("O registro de presença só é permitido a partir do horário de início");
        }

        Map<Long, AgendamentoPaciente> porPacienteId = new HashMap<>();
        for (AgendamentoPaciente ap : agendamento.getPacientes()) {
            porPacienteId.put(ap.getPaciente().getId(), ap);
        }

        Map<Long, Boolean> atualizacoes = new LinkedHashMap<>();
        if (request.pacientes() != null) {
            List<CampoErro> erros = new ArrayList<>();
            for (PresencaPacienteInputDTO item : request.pacientes()) {
                if (atualizacoes.containsKey(item.pacienteId())) {
                    erros.add(new CampoErro("pacientes", "Paciente duplicado no registro de presença"));
                } else if (!porPacienteId.containsKey(item.pacienteId())) {
                    erros.add(new CampoErro(
                            "pacientes",
                            "Paciente " + item.pacienteId() + " não pertence a este agendamento"
                    ));
                } else {
                    atualizacoes.put(item.pacienteId(), item.possuiPresenca());
                }
            }
            if (!erros.isEmpty()) {
                throw new ValidacaoCampoException(erros);
            }
        }

        if (request.possuiPresencaFuncionario() != null) {
            agendamento.setPossuiPresencaFuncionario(request.possuiPresencaFuncionario());
        }
        for (Map.Entry<Long, Boolean> entry : atualizacoes.entrySet()) {
            porPacienteId.get(entry.getKey()).setPossuiPresenca(entry.getValue());
        }

        Agendamento salvo = agendamentoRepository.saveAndFlush(agendamento);
        auditoriaService.registrarAsync(
                autenticado.id(),
                AcaoAuditoria.AGENDAMENTO,
                recursoFormatter.formatarAgendamento(salvo.getId(), "PRESENCA")
        );

        return toResponse(salvo, null);
    }

    @Transactional
    public AgendamentoResponse concluir(Long id, UsuarioAutenticado autenticado) {
        Agendamento agendamento = buscarAgendamentoComDetalhes(id);
        validarAcessoEscritaAgenda(agendamento, autenticado);

        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new ConflitoException("Apenas agendamentos em situação AGENDADO podem ser concluídos");
        }
        if (OffsetDateTime.now().isBefore(agendamento.getDataInicio())) {
            throw new ConflitoException("A conclusão do agendamento só é permitida a partir do horário de início");
        }

        agendamento.setStatus(StatusAgendamento.CONCLUIDO);
        Agendamento salvo = agendamentoRepository.saveAndFlush(agendamento);

        auditoriaService.registrarAsync(
                autenticado.id(),
                AcaoAuditoria.AGENDAMENTO,
                recursoFormatter.formatarAgendamento(salvo.getId(), "CONCLUIR")
        );

        return toResponse(salvo, null);
    }

    private Agendamento buscarAgendamentoComDetalhes(Long id) {
        if (id == null || id <= 0) {
            throw new ValidacaoCampoException("id", "Identificador do agendamento inválido");
        }
        return agendamentoRepository.findByIdComDetalhes(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado"));
    }

    private Long resolverFuncionarioParaCriacao(Long funcionarioIdInformado, UsuarioAutenticado autenticado) {
        if (autenticado.nivelAcesso() == NivelAcesso.MEDICO) {
            Funcionario meuFuncionario = resolverMeuFuncionario(autenticado.id());
            if (funcionarioIdInformado != null && !funcionarioIdInformado.equals(meuFuncionario.getId())) {
                throw new AccessDeniedException("Médico só pode criar agendamentos para a própria agenda");
            }
            return meuFuncionario.getId();
        }
        if (autenticado.nivelAcesso() == NivelAcesso.ADMINISTRADOR) {
            if (funcionarioIdInformado == null || funcionarioIdInformado <= 0) {
                throw new ValidacaoCampoException("funcionarioId", "O profissional responsável é obrigatório");
            }
            return funcionarioIdInformado;
        }
        throw new AccessDeniedException("Perfil sem permissão para criar agendamentos");
    }

    private void validarAcessoEscritaAgenda(Agendamento agendamento, UsuarioAutenticado autenticado) {
        if (autenticado.nivelAcesso() == NivelAcesso.ADMINISTRADOR) {
            return;
        }
        if (autenticado.nivelAcesso() == NivelAcesso.MEDICO) {
            Funcionario meuFuncionario = resolverMeuFuncionario(autenticado.id());
            if (!agendamento.getFuncionario().getId().equals(meuFuncionario.getId())) {
                throw new AccessDeniedException("Acesso negado a agendamento de outro profissional");
            }
            return;
        }
        throw new AccessDeniedException("Perfil sem permissão para operar agendamentos");
    }

    private Long validarAcessoLeitura(Agendamento agendamento, UsuarioAutenticado autenticado) {
        if (autenticado.nivelAcesso() == NivelAcesso.ADMINISTRADOR) {
            return null;
        }
        if (autenticado.nivelAcesso() == NivelAcesso.MEDICO) {
            Funcionario meuFuncionario = resolverMeuFuncionario(autenticado.id());
            if (!agendamento.getFuncionario().getId().equals(meuFuncionario.getId())) {
                throw new AccessDeniedException("Acesso negado a agendamento de outro profissional");
            }
            return null;
        }
        if (autenticado.nivelAcesso() == NivelAcesso.PACIENTE) {
            Paciente meuPaciente = resolverMeuPaciente(autenticado.id());
            boolean participa = agendamento.getPacientes().stream()
                    .anyMatch(ap -> ap.getPaciente().getId().equals(meuPaciente.getId()));
            if (!participa) {
                throw new AccessDeniedException("Acesso negado a agendamento de terceiros");
            }
            return meuPaciente.getId();
        }
        throw new AccessDeniedException("Perfil não autorizado");
    }

    private Funcionario resolverMeuFuncionario(Long usuarioId) {
        return funcionarioRepository.findAtivoByUsuarioId(usuarioId)
                .orElseThrow(() -> new AccessDeniedException("Cadastro funcional não localizado ou inativo"));
    }

    private Paciente resolverMeuPaciente(Long usuarioId) {
        return pacienteRepository.findAtivoByUsuarioIdComUsuario(usuarioId)
                .orElseThrow(() -> new AccessDeniedException("Cadastro de paciente não localizado ou inativo"));
    }

    private void validarDadosBasicosSessao(
            OffsetDateTime dataInicio,
            OffsetDateTime dataFim,
            List<Long> pacientesIds
    ) {
        List<CampoErro> erros = new ArrayList<>();
        if (dataInicio == null) {
            erros.add(new CampoErro("dataInicio", "A data de início é obrigatória"));
        } else if (!dataInicio.isAfter(OffsetDateTime.now())) {
            erros.add(new CampoErro("dataInicio", "A data de início deve estar no futuro"));
        }
        if (dataFim == null) {
            erros.add(new CampoErro("dataFim", "A data de fim é obrigatória"));
        } else if (dataInicio != null && !dataFim.isAfter(dataInicio)) {
            erros.add(new CampoErro("dataFim", "A data de fim deve ser posterior à data de início"));
        }
        if (pacientesIds == null || pacientesIds.isEmpty()) {
            erros.add(new CampoErro("pacientesIds", "Informe pelo menos um paciente participante"));
        } else {
            Set<Long> unicos = new HashSet<>();
            for (Long pid : pacientesIds) {
                if (pid == null || pid <= 0) {
                    erros.add(new CampoErro("pacientesIds", "Identificador de paciente inválido"));
                    break;
                }
                if (!unicos.add(pid)) {
                    erros.add(new CampoErro("pacientesIds", "Não é permitido repetir o mesmo paciente na sessão"));
                    break;
                }
            }
        }
        if (!erros.isEmpty()) {
            throw new ValidacaoCampoException(erros);
        }
    }

    private List<Paciente> carregarPacientesAtivos(List<Long> pacientesIds) {
        List<Paciente> pacientes = new ArrayList<>(pacientesIds.size());
        for (Long pid : pacientesIds) {
            Paciente paciente = pacienteRepository.findAtivoByIdComUsuario(pid)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado ou inativo"));
            pacientes.add(paciente);
        }
        return pacientes;
    }

    private AgendamentoResponse toResponse(Agendamento agendamento, Long pacienteEscopoLgpdId) {
        Funcionario func = agendamento.getFuncionario();
        List<AgendamentoPacienteDTO> pacientesDto = agendamento.getPacientes().stream()
                .filter(ap -> pacienteEscopoLgpdId == null || ap.getPaciente().getId().equals(pacienteEscopoLgpdId))
                .sorted(Comparator.comparing(ap -> ap.getPaciente().getUsuario().getNome(), String.CASE_INSENSITIVE_ORDER))
                .map(ap -> new AgendamentoPacienteDTO(
                        ap.getPaciente().getId(),
                        ap.getPaciente().getUsuario().getNome(),
                        Boolean.TRUE.equals(ap.getPossuiPresenca())
                ))
                .toList();

        return new AgendamentoResponse(
                agendamento.getId(),
                func.getId(),
                func.getUsuario().getNome(),
                func.getCargo(),
                agendamento.getDataInicio(),
                agendamento.getDataFim(),
                agendamento.getTipoAgendamento(),
                agendamento.getStatus(),
                Boolean.TRUE.equals(agendamento.getPossuiPresencaFuncionario()),
                pacientesDto,
                agendamento.getCriadoEm(),
                agendamento.getAtualizadoEm()
        );
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

