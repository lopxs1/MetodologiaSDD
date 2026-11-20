package br.org.amde.digital.service;

import br.org.amde.digital.domain.entity.Funcionario;
import br.org.amde.digital.domain.entity.Paciente;
import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.domain.enums.AcaoAuditoria;
import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.domain.enums.StatusAgendamento;
import br.org.amde.digital.dto.usuario.AtualizarUsuarioRequest;
import br.org.amde.digital.dto.usuario.CadastrarUsuarioRequest;
import br.org.amde.digital.dto.usuario.FuncionarioInputDTO;
import br.org.amde.digital.dto.usuario.ListaUsuariosResponse;
import br.org.amde.digital.dto.usuario.PacienteInputDTO;
import br.org.amde.digital.dto.usuario.TotalizadoresPerfilDTO;
import br.org.amde.digital.dto.usuario.UsuarioDetalheResponse;
import br.org.amde.digital.dto.usuario.UsuarioResumoResponse;
import br.org.amde.digital.exception.ConflitoException;
import br.org.amde.digital.exception.RecursoNaoEncontradoException;
import br.org.amde.digital.exception.ValidacaoCampoException;
import br.org.amde.digital.exception.ValidacaoCampoException.CampoErro;
import br.org.amde.digital.mapper.UsuarioMapper;
import br.org.amde.digital.repository.AgendamentoRepository;
import br.org.amde.digital.repository.FuncionarioRepository;
import br.org.amde.digital.repository.PacienteRepository;
import br.org.amde.digital.repository.UsuarioRepository;
import br.org.amde.digital.service.auditoria.AuditoriaService;
import br.org.amde.digital.service.auditoria.RecursoAuditoriaFormatter;
import br.org.amde.digital.service.usuario.CelularMasker;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final ZoneId ZONE_SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate DATA_NASCIMENTO_MINIMA = LocalDate.of(1900, 1, 1);
    private static final Pattern CELULAR_PATTERN = Pattern.compile("^[0-9]{10,11}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern UF_PATTERN = Pattern.compile("^[A-Z]{2}$");
    private static final Set<String> UFS_BRASIL = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA",
            "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN",
            "RS", "RO", "RR", "SC", "SP", "SE", "TO"
    );
    private static final Set<String> CAMPOS_ORDENACAO = Set.of("nome", "email", "nivelAcesso", "criadoEm");

    private final UsuarioRepository usuarioRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PacienteRepository pacienteRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final CelularMasker celularMasker;
    private final AuditoriaService auditoriaService;
    private final RecursoAuditoriaFormatter recursoFormatter;

    @Transactional
    public UsuarioDetalheResponse cadastrar(CadastrarUsuarioRequest request) {
        List<CampoErro> erros = new ArrayList<>();
        String nome = normalizarObrigatorio(request.nome(), "nome", "Nome", 2, 100, erros);
        String email = normalizarEmail(request.email(), erros);
        validarSenha(request.senha(), erros);
        validarCamposOpcionais(
                request.dataNascimento(),
                request.numCelular(),
                request.pais(),
                request.estado(),
                request.cidade(),
                request.rua(),
                erros
        );
        validarEspecializacaoCadastro(request.nivelAcesso(), request.funcionario(), request.paciente(), erros);

        if (!erros.isEmpty()) {
            throw new ValidacaoCampoException(erros);
        }

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("E-mail já utilizado por outro usuário ativo");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setNivelAcesso(request.nivelAcesso());
        usuario.setDataNascimento(request.dataNascimento());
        usuario.setNumCelular(normalizarOpcional(request.numCelular()));
        usuario.setPais(request.pais() == null || request.pais().isBlank() ? "Brasil" : request.pais().trim());
        usuario.setEstado(normalizarOpcional(request.estado()));
        usuario.setCidade(normalizarOpcional(request.cidade()));
        usuario.setRua(normalizarOpcional(request.rua()));

        try {
            Usuario salvo = usuarioRepository.saveAndFlush(usuario);
            if (salvo.getNivelAcesso() == NivelAcesso.MEDICO) {
                Funcionario func = new Funcionario();
                func.setUsuario(salvo);
                func.setCargo(request.funcionario().cargo().trim());
                func.setCrm(normalizarOpcional(request.funcionario().crm()));
                salvo.setFuncionario(funcionarioRepository.saveAndFlush(func));
            } else if (salvo.getNivelAcesso() == NivelAcesso.PACIENTE) {
                Paciente pac = new Paciente();
                pac.setUsuario(salvo);
                pac.setGrauSuporte(request.paciente().grauSuporte());
                pac.setDescricao(normalizarOpcional(request.paciente().descricao()));
                salvo.setPaciente(pacienteRepository.saveAndFlush(pac));
            }
            return usuarioMapper.toDetalheResponse(salvo, false, celularMasker);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflitoException("E-mail já utilizado por outro usuário ativo");
        }
    }

    @Transactional(readOnly = true)
    public ListaUsuariosResponse listar(
            String termo,
            NivelAcesso nivelAcesso,
            int page,
            int size,
            String sort,
            String direction
    ) {
        List<CampoErro> erros = new ArrayList<>();
        String termoNormalizado = null;
        if (termo != null && !termo.isBlank()) {
            termoNormalizado = termo.trim();
            if (termoNormalizado.length() > 100) {
                erros.add(new CampoErro("termo", "Termo de busca deve ter no máximo 100 caracteres"));
            }
        }
        if (page < 0) {
            erros.add(new CampoErro("page", "Página deve ser maior ou igual a zero"));
        }
        if (size < 1 || size > 50) {
            erros.add(new CampoErro("size", "Tamanho da página deve estar entre 1 e 50"));
        }
        String campoSort = (sort == null || sort.isBlank()) ? "nome" : sort.trim();
        if (!CAMPOS_ORDENACAO.contains(campoSort)) {
            erros.add(new CampoErro("sort", "Campo de ordenação inválido"));
        }
        Sort.Direction sortDirection = Sort.Direction.ASC;
        if (direction != null && !direction.isBlank()) {
            try {
                sortDirection = Sort.Direction.fromString(direction.trim());
            } catch (IllegalArgumentException ex) {
                erros.add(new CampoErro("direction", "Direção de ordenação deve ser ASC ou DESC"));
            }
        }
        if (!erros.isEmpty()) {
            throw new ValidacaoCampoException(erros);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, campoSort));
        Specification<Usuario> spec = criarSpecificationBusca(termoNormalizado, nivelAcesso);
        Page<Usuario> resultado = usuarioRepository.findAll(spec, pageable);

        List<UsuarioResumoResponse> itens = resultado.getContent().stream()
                .map(u -> usuarioMapper.toResumoResponse(u, celularMasker))
                .toList();

        return new ListaUsuariosResponse(
                itens,
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalElements(),
                resultado.getTotalPages(),
                calcularTotalizadores()
        );
    }

    @Transactional(readOnly = true)
    public UsuarioDetalheResponse consultarPorId(Long id, boolean revelarCelular) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        return usuarioMapper.toDetalheResponse(usuario, revelarCelular, celularMasker);
    }

    @Transactional
    public UsuarioDetalheResponse atualizar(Long id, AtualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        List<CampoErro> erros = new ArrayList<>();
        if (request.nivelAcesso() != null && request.nivelAcesso() != usuario.getNivelAcesso()) {
            erros.add(new CampoErro(
                    "nivelAcesso",
                    "O perfil de acesso do usuário não pode ser alterado após o cadastro"
            ));
        }

        String nome = normalizarObrigatorio(request.nome(), "nome", "Nome", 2, 100, erros);
        String email = normalizarEmail(request.email(), erros);
        boolean alterarSenha = request.senha() != null && !request.senha().isEmpty();
        if (alterarSenha) {
            validarSenha(request.senha(), erros);
        }
        validarCamposOpcionais(
                request.dataNascimento(),
                request.numCelular(),
                request.pais(),
                request.estado(),
                request.cidade(),
                request.rua(),
                erros
        );
        validarEspecializacaoAtualizacao(usuario.getNivelAcesso(), request.funcionario(), request.paciente(), erros);

        if (!erros.isEmpty()) {
            throw new ValidacaoCampoException(erros);
        }

        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflitoException("Novo e-mail já utilizado por outro usuário ativo");
        }

        usuario.setNome(nome);
        usuario.setEmail(email);
        if (alterarSenha) {
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }
        usuario.setDataNascimento(request.dataNascimento());
        usuario.setNumCelular(normalizarOpcional(request.numCelular()));
        usuario.setPais(normalizarOpcional(request.pais()));
        usuario.setEstado(normalizarOpcional(request.estado()));
        usuario.setCidade(normalizarOpcional(request.cidade()));
        usuario.setRua(normalizarOpcional(request.rua()));

        try {
            Usuario salvo = usuarioRepository.saveAndFlush(usuario);
            if (salvo.getNivelAcesso() == NivelAcesso.MEDICO && request.funcionario() != null) {
                Funcionario func = salvo.getFuncionario();
                if (func == null) {
                    func = new Funcionario();
                    func.setUsuario(salvo);
                }
                func.setCargo(request.funcionario().cargo().trim());
                func.setCrm(normalizarOpcional(request.funcionario().crm()));
                salvo.setFuncionario(funcionarioRepository.saveAndFlush(func));
            } else if (salvo.getNivelAcesso() == NivelAcesso.PACIENTE && request.paciente() != null) {
                Paciente pac = salvo.getPaciente();
                if (pac == null) {
                    pac = new Paciente();
                    pac.setUsuario(salvo);
                }
                pac.setGrauSuporte(request.paciente().grauSuporte());
                pac.setDescricao(normalizarOpcional(request.paciente().descricao()));
                salvo.setPaciente(pacienteRepository.saveAndFlush(pac));
            }
            return usuarioMapper.toDetalheResponse(salvo, false, celularMasker);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflitoException("Novo e-mail já utilizado por outro usuário ativo");
        }
    }

    @Transactional
    public void excluirLogicamente(Long id, Long administradorAutenticadoId) {
        if (id.equals(administradorAutenticadoId)) {
            throw new ConflitoException("O administrador não pode excluir a própria conta");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        if (usuario.getNivelAcesso() == NivelAcesso.MEDICO && usuario.getFuncionario() != null) {
            boolean possuiAgendamentosAtivos = agendamentoRepository.existsByFuncionarioIdAndStatus(
                    usuario.getFuncionario().getId(),
                    StatusAgendamento.AGENDADO
            );
            if (possuiAgendamentosAtivos) {
                throw new ConflitoException(
                        "Não é possível excluir profissional com agendamentos em situação AGENDADO"
                );
            }
        }

        if (usuario.getNivelAcesso() == NivelAcesso.PACIENTE && usuario.getPaciente() != null) {
            boolean possuiAgendamentosAtivos = agendamentoRepository.existsByPacienteIdAndStatus(
                    usuario.getPaciente().getId(),
                    StatusAgendamento.AGENDADO
            );
            if (possuiAgendamentosAtivos) {
                throw new ConflitoException(
                        "Não é possível excluir paciente com agendamentos em situação AGENDADO"
                );
            }
        }

        OffsetDateTime agora = OffsetDateTime.now();
        usuario.setExcluidoEm(agora);
        if (usuario.getFuncionario() != null) {
            usuario.getFuncionario().setExcluidoEm(agora);
            funcionarioRepository.save(usuario.getFuncionario());
        }
        if (usuario.getPaciente() != null) {
            usuario.getPaciente().setExcluidoEm(agora);
            pacienteRepository.save(usuario.getPaciente());
        }
        usuarioRepository.saveAndFlush(usuario);

        auditoriaService.registrarAsync(
                administradorAutenticadoId,
                AcaoAuditoria.EXCLUSAO,
                recursoFormatter.formatarExclusaoUsuario(id)
        );
    }

    private TotalizadoresPerfilDTO calcularTotalizadores() {
        long admins = 0;
        long medicos = 0;
        long pacientes = 0;
        for (Object[] linha : usuarioRepository.contarAtivosPorNivelAcesso()) {
            NivelAcesso nivel = (NivelAcesso) linha[0];
            long qtd = ((Number) linha[1]).longValue();
            switch (nivel) {
                case ADMINISTRADOR -> admins = qtd;
                case MEDICO -> medicos = qtd;
                case PACIENTE -> pacientes = qtd;
            }
        }
        long total = admins + medicos + pacientes;
        return new TotalizadoresPerfilDTO(total, admins, medicos, pacientes);
    }

    private Specification<Usuario> criarSpecificationBusca(String termo, NivelAcesso nivelAcesso) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (nivelAcesso != null) {
                predicates.add(cb.equal(root.get("nivelAcesso"), nivelAcesso));
            }
            if (termo != null && !termo.isBlank()) {
                String semAcento = Normalizer.normalize(termo, Normalizer.Form.NFD)
                        .replaceAll("\\p{M}+", "")
                        .toLowerCase(Locale.ROOT);
                String escapado = semAcento
                        .replace("!", "!!")
                        .replace("%", "!%")
                        .replace("_", "!_");
                String pattern = "%" + escapado + "%";

                var nomeNormalizado = cb.function("public.unaccent", String.class, cb.lower(root.get("nome")));
                var emailNormalizado = cb.function("public.unaccent", String.class, cb.lower(root.get("email")));
                predicates.add(cb.or(
                        cb.like(nomeNormalizado, pattern, '!'),
                        cb.like(emailNormalizado, pattern, '!')
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private String normalizarObrigatorio(
            String valor,
            String campo,
            String rotulo,
            int min,
            int max,
            List<CampoErro> erros
    ) {
        if (valor == null || valor.trim().isEmpty()) {
            erros.add(new CampoErro(campo, rotulo + " é obrigatório"));
            return null;
        }
        String trimmed = valor.trim();
        if (trimmed.length() < min || trimmed.length() > max) {
            erros.add(new CampoErro(campo, rotulo + " deve ter entre " + min + " e " + max + " caracteres"));
        }
        return trimmed;
    }

    private String normalizarEmail(String rawEmail, List<CampoErro> erros) {
        if (rawEmail == null || rawEmail.trim().isEmpty()) {
            erros.add(new CampoErro("email", "E-mail é obrigatório"));
            return null;
        }
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (email.length() < 5 || email.length() > 254 || !EMAIL_PATTERN.matcher(email).matches()) {
            erros.add(new CampoErro("email", "E-mail inválido"));
        }
        return email;
    }

    private void validarSenha(String senha, List<CampoErro> erros) {
        if (senha == null || senha.isEmpty()) {
            erros.add(new CampoErro("senha", "Senha é obrigatória"));
            return;
        }
        if (senha.trim().isEmpty()) {
            erros.add(new CampoErro("senha", "Senha não pode ser formada apenas por espaços"));
        }
        if (senha.length() < 8) {
            erros.add(new CampoErro("senha", "Senha deve ter no mínimo 8 caracteres"));
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            erros.add(new CampoErro("senha", "Senha deve ter no máximo 72 bytes em UTF-8"));
        }
    }

    private void validarCamposOpcionais(
            LocalDate dataNascimento,
            String numCelular,
            String pais,
            String estado,
            String cidade,
            String rua,
            List<CampoErro> erros
    ) {
        if (dataNascimento != null) {
            LocalDate hoje = LocalDate.now(ZONE_SAO_PAULO);
            if (dataNascimento.isBefore(DATA_NASCIMENTO_MINIMA) || dataNascimento.isAfter(hoje)) {
                erros.add(new CampoErro(
                        "dataNascimento",
                        "Data de nascimento deve estar entre 1900-01-01 e a data atual"
                ));
            }
        }
        if (numCelular != null) {
            String celularLimpo = numCelular.trim();
            if (!CELULAR_PATTERN.matcher(celularLimpo).matches()) {
                erros.add(new CampoErro("numCelular", "Celular deve conter apenas 10 ou 11 dígitos numéricos"));
            }
        }
        if (pais != null && !pais.isEmpty()) {
            String trimmed = pais.trim();
            if (trimmed.length() < 2 || trimmed.length() > 60) {
                erros.add(new CampoErro("pais", "País deve ter entre 2 e 60 caracteres"));
            }
        }
        if (estado != null) {
            String uf = estado.trim();
            if (!UF_PATTERN.matcher(uf).matches() || !UFS_BRASIL.contains(uf)) {
                erros.add(new CampoErro("estado", "Estado deve ser uma sigla de UF brasileira válida"));
            }
        }
        if (cidade != null) {
            String trimmed = cidade.trim();
            if (trimmed.length() < 2 || trimmed.length() > 100) {
                erros.add(new CampoErro("cidade", "Cidade deve ter entre 2 e 100 caracteres"));
            }
        }
        if (rua != null) {
            String trimmed = rua.trim();
            if (trimmed.length() < 2 || trimmed.length() > 150) {
                erros.add(new CampoErro("rua", "Rua deve ter entre 2 e 150 caracteres"));
            }
        }
    }

    private void validarEspecializacaoCadastro(
            NivelAcesso nivelAcesso,
            FuncionarioInputDTO funcionario,
            PacienteInputDTO paciente,
            List<CampoErro> erros
    ) {
        if (nivelAcesso == null) {
            erros.add(new CampoErro("nivelAcesso", "Perfil de acesso é obrigatório"));
            return;
        }
        switch (nivelAcesso) {
            case MEDICO -> {
                if (paciente != null) {
                    erros.add(new CampoErro("paciente", "Dados de paciente são incompatíveis com o perfil MEDICO"));
                }
                if (funcionario == null) {
                    erros.add(new CampoErro("funcionario", "Dados profissionais são obrigatórios para o perfil MEDICO"));
                } else {
                    validarDadosFuncionario(funcionario, erros);
                }
            }
            case PACIENTE -> {
                if (funcionario != null) {
                    erros.add(new CampoErro(
                            "funcionario",
                            "Dados profissionais são incompatíveis com o perfil PACIENTE"
                    ));
                }
                if (paciente == null) {
                    erros.add(new CampoErro("paciente", "Dados clínicos são obrigatórios para o perfil PACIENTE"));
                } else {
                    validarDadosPaciente(paciente, erros);
                }
            }
            case ADMINISTRADOR -> {
                if (funcionario != null) {
                    erros.add(new CampoErro(
                            "funcionario",
                            "Perfil ADMINISTRADOR não possui especialização de funcionário"
                    ));
                }
                if (paciente != null) {
                    erros.add(new CampoErro(
                            "paciente",
                            "Perfil ADMINISTRADOR não possui especialização de paciente"
                    ));
                }
            }
        }
    }

    private void validarEspecializacaoAtualizacao(
            NivelAcesso perfilAtual,
            FuncionarioInputDTO funcionario,
            PacienteInputDTO paciente,
            List<CampoErro> erros
    ) {
        switch (perfilAtual) {
            case MEDICO -> {
                if (paciente != null) {
                    erros.add(new CampoErro("paciente", "Dados de paciente são incompatíveis com o perfil MEDICO"));
                }
                if (funcionario != null) {
                    validarDadosFuncionario(funcionario, erros);
                }
            }
            case PACIENTE -> {
                if (funcionario != null) {
                    erros.add(new CampoErro(
                            "funcionario",
                            "Dados profissionais são incompatíveis com o perfil PACIENTE"
                    ));
                }
                if (paciente != null) {
                    validarDadosPaciente(paciente, erros);
                }
            }
            case ADMINISTRADOR -> {
                if (funcionario != null) {
                    erros.add(new CampoErro(
                            "funcionario",
                            "Perfil ADMINISTRADOR não possui especialização de funcionário"
                    ));
                }
                if (paciente != null) {
                    erros.add(new CampoErro(
                            "paciente",
                            "Perfil ADMINISTRADOR não possui especialização de paciente"
                    ));
                }
            }
        }
    }

    private void validarDadosFuncionario(FuncionarioInputDTO funcionario, List<CampoErro> erros) {
        normalizarObrigatorio(funcionario.cargo(), "funcionario.cargo", "Cargo", 2, 80, erros);
        if (funcionario.crm() != null) {
            String crm = funcionario.crm().trim();
            if (crm.length() < 3 || crm.length() > 30) {
                erros.add(new CampoErro(
                        "funcionario.crm",
                        "Registro profissional deve ter entre 3 e 30 caracteres"
                ));
            }
        }
    }

    private void validarDadosPaciente(PacienteInputDTO paciente, List<CampoErro> erros) {
        if (paciente.grauSuporte() == null) {
            erros.add(new CampoErro("paciente.grauSuporte", "Grau de suporte é obrigatório para PACIENTE"));
        }
        if (paciente.descricao() != null && paciente.descricao().trim().length() > 5000) {
            erros.add(new CampoErro(
                    "paciente.descricao",
                    "Descrição clínica deve ter no máximo 5000 caracteres"
            ));
        }
    }

    private String normalizarOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
