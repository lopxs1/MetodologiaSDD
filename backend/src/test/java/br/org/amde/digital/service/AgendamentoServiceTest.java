package br.org.amde.digital.service;

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
import br.org.amde.digital.dto.agendamento.AgendamentoResponse;
import br.org.amde.digital.dto.agendamento.CriarAgendamentoRequest;
import br.org.amde.digital.dto.agendamento.EditarAgendamentoRequest;
import br.org.amde.digital.dto.agendamento.PresencaPacienteInputDTO;
import br.org.amde.digital.dto.agendamento.RegistrarPresencaRequest;
import br.org.amde.digital.exception.ConflitoException;
import br.org.amde.digital.exception.RecursoNaoEncontradoException;
import br.org.amde.digital.exception.ValidacaoCampoException;
import br.org.amde.digital.repository.AgendamentoPacienteRepository;
import br.org.amde.digital.repository.AgendamentoRepository;
import br.org.amde.digital.repository.FuncionarioRepository;
import br.org.amde.digital.repository.PacienteRepository;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.auditoria.AuditoriaService;
import br.org.amde.digital.service.auditoria.RecursoAuditoriaFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AgendamentoPacienteRepository agendamentoPacienteRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @Spy
    private RecursoAuditoriaFormatter recursoFormatter = new RecursoAuditoriaFormatter();

    @InjectMocks
    private AgendamentoService agendamentoService;

    @Test
    void deveCriarSessaoEmGrupoEAuditarQuandoAdminSolicita() {
        UsuarioAutenticado admin = new UsuarioAutenticado(1L, "Admin", "admin@amde.org.br", NivelAcesso.ADMINISTRADOR);
        Funcionario func = criarFuncionario(3L, 10L, "Dra. Ana", "Psicologia", NivelAcesso.MEDICO);
        Paciente p1 = criarPaciente(11L, 21L, "Beatriz");
        Paciente p2 = criarPaciente(12L, 22L, "Carlos");

        OffsetDateTime inicio = OffsetDateTime.now().plusDays(2);
        OffsetDateTime fim = inicio.plusHours(1);

        when(funcionarioRepository.findAtivoByIdForUpdate(3L)).thenReturn(Optional.of(func));
        when(pacienteRepository.findAtivoByIdComUsuario(11L)).thenReturn(Optional.of(p1));
        when(pacienteRepository.findAtivoByIdComUsuario(12L)).thenReturn(Optional.of(p2));
        when(agendamentoRepository.existsConflitoHorario(3L, inicio, fim, null)).thenReturn(false);
        when(agendamentoRepository.saveAndFlush(any(Agendamento.class))).thenAnswer(inv -> {
            Agendamento a = inv.getArgument(0);
            a.setId(42L);
            a.setCriadoEm(OffsetDateTime.now());
            a.setAtualizadoEm(OffsetDateTime.now());
            return a;
        });

        AgendamentoResponse resposta = agendamentoService.criar(
                new CriarAgendamentoRequest(3L, inicio, fim, TipoAgendamento.TERAPIA, List.of(12L, 11L)),
                admin
        );

        assertThat(resposta.id()).isEqualTo(42L);
        assertThat(resposta.status()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(resposta.possuiPresencaFuncionario()).isFalse();
        assertThat(resposta.pacientes()).extracting("nomePaciente").containsExactly("Beatriz", "Carlos");
        assertThat(resposta.pacientes()).allSatisfy(p -> assertThat(p.possuiPresenca()).isFalse());
        verify(agendamentoPacienteRepository).saveAllAndFlush(anyList());
        verify(auditoriaService).registrarAsync(1L, AcaoAuditoria.AGENDAMENTO, "agendamento:42:CRIAR");
    }

    @Test
    void deveBloquearMedicoCriandoParaOutroProfissionalCom403() {
        UsuarioAutenticado medico = new UsuarioAutenticado(20L, "Dr. João", "joao@amde.org.br", NivelAcesso.MEDICO);
        Funcionario meuFunc = criarFuncionario(5L, 20L, "Dr. João", "Terapia", NivelAcesso.MEDICO);
        when(funcionarioRepository.findAtivoByUsuarioId(20L)).thenReturn(Optional.of(meuFunc));

        OffsetDateTime inicio = OffsetDateTime.now().plusDays(1);
        CriarAgendamentoRequest request = new CriarAgendamentoRequest(
                99L, inicio, inicio.plusHours(1), TipoAgendamento.CONSULTA, List.of(11L)
        );

        assertThatThrownBy(() -> agendamentoService.criar(request, medico))
                .isInstanceOf(AccessDeniedException.class);
        verify(agendamentoRepository, never()).saveAndFlush(any());
    }

    @Test
    void deveRejeitarDatasInvalidasEDuplicidadeDePacientesCom400() {
        UsuarioAutenticado admin = new UsuarioAutenticado(1L, "Admin", "admin@amde.org.br", NivelAcesso.ADMINISTRADOR);
        OffsetDateTime passado = OffsetDateTime.now().minusHours(2);

        assertThatThrownBy(() -> agendamentoService.criar(
                new CriarAgendamentoRequest(3L, passado, passado.minusMinutes(10), TipoAgendamento.EXAME, List.of(1L, 1L)),
                admin
        )).isInstanceOf(ValidacaoCampoException.class);
    }

    @Test
    void deveRejeitarConflitoDeHorarioCom409SemGerarAuditoria() {
        UsuarioAutenticado medico = new UsuarioAutenticado(20L, "Dr. João", "joao@amde.org.br", NivelAcesso.MEDICO);
        Funcionario meuFunc = criarFuncionario(5L, 20L, "Dr. João", "Terapia", NivelAcesso.MEDICO);
        Paciente pac = criarPaciente(11L, 21L, "Beatriz");

        OffsetDateTime inicio = OffsetDateTime.now().plusDays(2);
        OffsetDateTime fim = inicio.plusHours(1);

        when(funcionarioRepository.findAtivoByUsuarioId(20L)).thenReturn(Optional.of(meuFunc));
        when(funcionarioRepository.findAtivoByIdForUpdate(5L)).thenReturn(Optional.of(meuFunc));
        when(pacienteRepository.findAtivoByIdComUsuario(11L)).thenReturn(Optional.of(pac));
        when(agendamentoRepository.existsConflitoHorario(5L, inicio, fim, null)).thenReturn(true);

        assertThatThrownBy(() -> agendamentoService.criar(
                new CriarAgendamentoRequest(null, inicio, fim, TipoAgendamento.CONSULTA, List.of(11L)),
                medico
        )).isInstanceOf(ConflitoException.class);

        verify(auditoriaService, never()).registrarAsync(any(), any(), any());
    }

    @Test
    void deveRejeitarProfissionalOuPacienteInativoOuInexistenteCom404() {
        UsuarioAutenticado admin = new UsuarioAutenticado(1L, "Admin", "admin@amde.org.br", NivelAcesso.ADMINISTRADOR);
        OffsetDateTime inicio = OffsetDateTime.now().plusDays(2);
        when(funcionarioRepository.findAtivoByIdForUpdate(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendamentoService.criar(
                new CriarAgendamentoRequest(999L, inicio, inicio.plusHours(1), TipoAgendamento.CONSULTA, List.of(11L)),
                admin
        )).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveRejeitarTrocaDeProfissionalEEstadoFinalNaEdicao() {
        UsuarioAutenticado admin = new UsuarioAutenticado(1L, "Admin", "admin@amde.org.br", NivelAcesso.ADMINISTRADOR);
        Funcionario func = criarFuncionario(3L, 10L, "Dra. Ana", "Psicologia", NivelAcesso.MEDICO);
        Paciente p1 = criarPaciente(11L, 21L, "Beatriz");
        Agendamento agendamento = criarAgendamento(
                42L, func, OffsetDateTime.now().plusDays(3), OffsetDateTime.now().plusDays(3).plusHours(1),
                StatusAgendamento.AGENDADO, List.of(p1), false
        );

        when(agendamentoRepository.findByIdComDetalhes(42L)).thenReturn(Optional.of(agendamento));

        OffsetDateTime novoInicio = OffsetDateTime.now().plusDays(4);
        assertThatThrownBy(() -> agendamentoService.editar(
                42L,
                new EditarAgendamentoRequest(9L, novoInicio, novoInicio.plusHours(1), TipoAgendamento.RETORNO, List.of(11L)),
                admin
        )).isInstanceOf(ValidacaoCampoException.class);

        agendamento.setStatus(StatusAgendamento.CONCLUIDO);
        assertThatThrownBy(() -> agendamentoService.editar(
                42L,
                new EditarAgendamentoRequest(3L, novoInicio, novoInicio.plusHours(1), TipoAgendamento.RETORNO, List.of(11L)),
                admin
        )).isInstanceOf(ConflitoException.class);
    }

    @Test
    void devePreservarPresencaDePacienteMantidoNaEdicao() {
        UsuarioAutenticado admin = new UsuarioAutenticado(1L, "Admin", "admin@amde.org.br", NivelAcesso.ADMINISTRADOR);
        Funcionario func = criarFuncionario(3L, 10L, "Dra. Ana", "Psicologia", NivelAcesso.MEDICO);
        Paciente p1 = criarPaciente(11L, 21L, "Beatriz");
        Paciente p2 = criarPaciente(12L, 22L, "Carlos");
        Agendamento agendamento = criarAgendamento(
                42L, func, OffsetDateTime.now().plusDays(2), OffsetDateTime.now().plusDays(2).plusHours(1),
                StatusAgendamento.AGENDADO, List.of(p1), true
        );

        when(agendamentoRepository.findByIdComDetalhes(42L)).thenReturn(Optional.of(agendamento));
        when(funcionarioRepository.findAtivoByIdForUpdate(3L)).thenReturn(Optional.of(func));
        when(pacienteRepository.findAtivoByIdComUsuario(11L)).thenReturn(Optional.of(p1));
        when(pacienteRepository.findAtivoByIdComUsuario(12L)).thenReturn(Optional.of(p2));
        when(agendamentoRepository.existsConflitoHorario(eq(3L), any(), any(), eq(42L))).thenReturn(false);
        when(agendamentoRepository.saveAndFlush(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));

        OffsetDateTime novoInicio = OffsetDateTime.now().plusDays(5);
        AgendamentoResponse resposta = agendamentoService.editar(
                42L,
                new EditarAgendamentoRequest(3L, novoInicio, novoInicio.plusHours(1), TipoAgendamento.TERAPIA, List.of(11L, 12L)),
                admin
        );

        assertThat(resposta.pacientes()).hasSize(2);
        assertThat(resposta.pacientes().get(0).possuiPresenca()).isTrue();
        assertThat(resposta.pacientes().get(1).possuiPresenca()).isFalse();
        verify(auditoriaService).registrarAsync(1L, AcaoAuditoria.AGENDAMENTO, "agendamento:42:EDITAR");
    }

    @Test
    void deveBloquearPresencaEConclusaoAntesDoInicioOuCanceladoEControlarPresenca() {
        UsuarioAutenticado admin = new UsuarioAutenticado(1L, "Admin", "admin@amde.org.br", NivelAcesso.ADMINISTRADOR);
        Funcionario func = criarFuncionario(3L, 10L, "Dra. Ana", "Psicologia", NivelAcesso.MEDICO);
        Paciente p1 = criarPaciente(11L, 21L, "Beatriz");

        Agendamento futuro = criarAgendamento(
                50L, func, OffsetDateTime.now().plusHours(2), OffsetDateTime.now().plusHours(3),
                StatusAgendamento.AGENDADO, List.of(p1), false
        );
        when(agendamentoRepository.findByIdComDetalhes(50L)).thenReturn(Optional.of(futuro));

        assertThatThrownBy(() -> agendamentoService.concluir(50L, admin)).isInstanceOf(ConflitoException.class);
        assertThatThrownBy(() -> agendamentoService.registrarPresenca(
                50L, new RegistrarPresencaRequest(true, List.of()), admin
        )).isInstanceOf(ConflitoException.class);

        Agendamento emCurso = criarAgendamento(
                51L, func, OffsetDateTime.now().minusMinutes(15), OffsetDateTime.now().plusMinutes(45),
                StatusAgendamento.AGENDADO, List.of(p1), false
        );
        when(agendamentoRepository.findByIdComDetalhes(51L)).thenReturn(Optional.of(emCurso));
        when(agendamentoRepository.saveAndFlush(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));

        AgendamentoResponse comPresenca = agendamentoService.registrarPresenca(
                51L,
                new RegistrarPresencaRequest(true, List.of(new PresencaPacienteInputDTO(11L, true))),
                admin
        );
        assertThat(comPresenca.possuiPresencaFuncionario()).isTrue();
        assertThat(comPresenca.pacientes().get(0).possuiPresenca()).isTrue();

        AgendamentoResponse concluido = agendamentoService.concluir(51L, admin);
        assertThat(concluido.status()).isEqualTo(StatusAgendamento.CONCLUIDO);
        verify(auditoriaService).registrarAsync(1L, AcaoAuditoria.AGENDAMENTO, "agendamento:51:PRESENCA");
        verify(auditoriaService).registrarAsync(1L, AcaoAuditoria.AGENDAMENTO, "agendamento:51:CONCLUIR");
    }

    @Test
    void deveAplicarMinimizacaoLgpdParaPacienteEmSessaoEmGrupo() {
        UsuarioAutenticado pacienteAuth = new UsuarioAutenticado(21L, "Beatriz", "bea@amde.org.br", NivelAcesso.PACIENTE);
        Funcionario func = criarFuncionario(3L, 10L, "Dra. Ana", "Psicologia", NivelAcesso.MEDICO);
        Paciente p1 = criarPaciente(11L, 21L, "Beatriz");
        Paciente p2 = criarPaciente(12L, 22L, "Carlos");
        Paciente p3 = criarPaciente(13L, 23L, "Terceiro");

        Agendamento grupo = criarAgendamento(
                60L, func, OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(1),
                StatusAgendamento.AGENDADO, List.of(p1, p2), false
        );
        Agendamento deTerceiro = criarAgendamento(
                61L, func, OffsetDateTime.now().plusDays(2), OffsetDateTime.now().plusDays(2).plusHours(1),
                StatusAgendamento.AGENDADO, List.of(p3), false
        );

        when(pacienteRepository.findAtivoByUsuarioIdComUsuario(21L)).thenReturn(Optional.of(p1));
        when(agendamentoRepository.findByIdComDetalhes(60L)).thenReturn(Optional.of(grupo));
        when(agendamentoRepository.findByIdComDetalhes(61L)).thenReturn(Optional.of(deTerceiro));

        AgendamentoResponse resposta = agendamentoService.consultarPorId(60L, pacienteAuth);
        assertThat(resposta.pacientes()).hasSize(1);
        assertThat(resposta.pacientes().get(0).pacienteId()).isEqualTo(11L);
        assertThat(resposta.pacientes().get(0).nomePaciente()).isEqualTo("Beatriz");

        assertThatThrownBy(() -> agendamentoService.consultarPorId(61L, pacienteAuth))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deveCancelarAgendamentoEAlterarParaCanceladoSemExclusao() {
        UsuarioAutenticado admin = new UsuarioAutenticado(1L, "Admin", "admin@amde.org.br", NivelAcesso.ADMINISTRADOR);
        Funcionario func = criarFuncionario(3L, 10L, "Dra. Ana", "Psicologia", NivelAcesso.MEDICO);
        Paciente p1 = criarPaciente(11L, 21L, "Beatriz");
        Agendamento agendamento = criarAgendamento(
                70L, func, OffsetDateTime.now().plusDays(2), OffsetDateTime.now().plusDays(2).plusHours(1),
                StatusAgendamento.AGENDADO, List.of(p1), false
        );
        when(agendamentoRepository.findByIdComDetalhes(70L)).thenReturn(Optional.of(agendamento));

        agendamentoService.cancelar(70L, admin);

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
        verify(auditoriaService).registrarAsync(1L, AcaoAuditoria.AGENDAMENTO, "agendamento:70:CANCELAR");
    }

    private Funcionario criarFuncionario(Long id, Long usuarioId, String nome, String cargo, NivelAcesso nivel) {
        Usuario u = new Usuario();
        u.setId(usuarioId);
        u.setNome(nome);
        u.setNivelAcesso(nivel);
        Funcionario f = new Funcionario();
        f.setId(id);
        f.setUsuario(u);
        f.setCargo(cargo);
        return f;
    }

    private Paciente criarPaciente(Long id, Long usuarioId, String nome) {
        Usuario u = new Usuario();
        u.setId(usuarioId);
        u.setNome(nome);
        u.setNivelAcesso(NivelAcesso.PACIENTE);
        Paciente p = new Paciente();
        p.setId(id);
        p.setUsuario(u);
        p.setGrauSuporte(GrauSuporte.NIVEL_1);
        return p;
    }

    private Agendamento criarAgendamento(
            Long id,
            Funcionario func,
            OffsetDateTime inicio,
            OffsetDateTime fim,
            StatusAgendamento status,
            List<Paciente> pacientes,
            boolean presencaInicial
    ) {
        Agendamento ag = new Agendamento();
        ag.setId(id);
        ag.setFuncionario(func);
        ag.setDataInicio(inicio);
        ag.setDataFim(fim);
        ag.setTipoAgendamento(TipoAgendamento.TERAPIA);
        ag.setStatus(status);
        ag.setPossuiPresencaFuncionario(presencaInicial);
        ag.setPacientes(new LinkedHashSet<>());
        for (Paciente p : pacientes) {
            AgendamentoPaciente ap = new AgendamentoPaciente();
            ap.setId(new AgendamentoPacienteId(id, p.getId()));
            ap.setAgendamento(ag);
            ap.setPaciente(p);
            ap.setPossuiPresenca(presencaInicial);
            ag.getPacientes().add(ap);
        }
        return ag;
    }
}

