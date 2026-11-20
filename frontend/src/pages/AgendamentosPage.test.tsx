import { AxiosError } from 'axios';
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useAuth } from '@/contexts/AuthContext';
import { AgendamentosPage } from '@/pages/AgendamentosPage';
import { agendamentoService } from '@/services/agendamentoService';
import { pacienteService } from '@/services/pacienteService';
import type { AgendamentoItem } from '@/types/agendamento';
import type { UsuarioSessao } from '@/types/usuario';

vi.mock('@/contexts/AuthContext', () => ({
  useAuth: vi.fn(),
}));

vi.mock('@/services/agendamentoService', () => ({
  agendamentoService: {
    listarAgendamentos: vi.fn(),
    consultarAgendamento: vi.fn(),
    criarAgendamento: vi.fn(),
    editarAgendamento: vi.fn(),
    cancelarAgendamento: vi.fn(),
    registrarPresenca: vi.fn(),
    concluirAgendamento: vi.fn(),
    listarProfissionaisClinicos: vi.fn(),
  },
}));

vi.mock('@/services/pacienteService', () => ({
  pacienteService: {
    listarPacientes: vi.fn(),
    consultarFichaPaciente: vi.fn(),
    consultarMeuProntuario: vi.fn(),
    atualizarDadosClinicos: vi.fn(),
  },
}));

const ADMIN_SESSAO: UsuarioSessao = {
  id: 1,
  nome: 'Administrador Teste',
  email: 'admin@amde.example.org',
  nivelAcesso: 'ADMINISTRADOR',
};

const PACIENTE_SESSAO: UsuarioSessao = {
  id: 30,
  nome: 'Lucas Álvares',
  email: 'lucas@amde.example.org',
  nivelAcesso: 'PACIENTE',
};

const AGENDAMENTO_FUTURO: AgendamentoItem = {
  id: 42,
  funcionarioId: 3,
  nomeProfissional: 'Dra. Ana Paula',
  cargoProfissional: 'Psicologia',
  dataInicio: '2026-12-20T14:00:00-03:00',
  dataFim: '2026-12-20T15:00:00-03:00',
  tipoAgendamento: 'TERAPIA',
  status: 'AGENDADO',
  possuiPresencaFuncionario: false,
  pacientes: [
    { pacienteId: 11, nomePaciente: 'Lucas Álvares', possuiPresenca: false },
    { pacienteId: 12, nomePaciente: 'Marina Costa', possuiPresenca: false },
  ],
  criadoEm: '2026-11-18T10:00:00-03:00',
  atualizadoEm: '2026-11-18T10:00:00-03:00',
};

const AGENDAMENTO_EM_CURSO: AgendamentoItem = {
  id: 43,
  funcionarioId: 3,
  nomeProfissional: 'Dra. Ana Paula',
  cargoProfissional: 'Psicologia',
  dataInicio: '2025-01-10T10:00:00-03:00',
  dataFim: '2027-01-10T11:00:00-03:00',
  tipoAgendamento: 'CONSULTA',
  status: 'AGENDADO',
  possuiPresencaFuncionario: false,
  pacientes: [{ pacienteId: 11, nomePaciente: 'Lucas Álvares', possuiPresenca: false }],
  criadoEm: '2025-01-01T10:00:00-03:00',
  atualizadoEm: '2025-01-01T10:00:00-03:00',
};

function configurarUsuario(usuario: UsuarioSessao) {
  vi.mocked(useAuth).mockReturnValue({
    usuario,
    token: 'fake-jwt-token',
    autenticado: true,
    login: vi.fn(),
    logout: vi.fn(),
  });
}

describe('AgendamentosPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    configurarUsuario(ADMIN_SESSAO);
    vi.mocked(agendamentoService.listarProfissionaisClinicos).mockResolvedValue([
      {
        funcionarioId: 3,
        usuarioId: 10,
        nome: 'Dra. Ana Paula',
        cargo: 'Psicologia',
        crm: 'CRP 06/1234',
      },
    ]);
    vi.mocked(pacienteService.listarPacientes).mockResolvedValue({
      itens: [
        {
          id: 11,
          usuarioId: 30,
          nome: 'Lucas Álvares',
          email: 'lucas@amde.example.org',
          grauSuporte: 'NIVEL_1',
          numCelular: null,
        },
        {
          id: 12,
          usuarioId: 31,
          nome: 'Marina Costa',
          email: 'marina@amde.example.org',
          grauSuporte: 'NIVEL_2',
          numCelular: null,
        },
      ],
      pagina: 0,
      tamanho: 50,
      totalItens: 2,
      totalPaginas: 1,
    });
  });

  it('renderiza horários em America/Sao_Paulo, estado de erro com recarregar e estado vazio', async () => {
    vi.mocked(agendamentoService.listarAgendamentos)
      .mockRejectedValueOnce(new Error('Falha de rede'))
      .mockResolvedValueOnce({
        itens: [AGENDAMENTO_FUTURO],
        pagina: 0,
        tamanho: 10,
        totalItens: 1,
        totalPaginas: 1,
      })
      .mockResolvedValueOnce({
        itens: [],
        pagina: 0,
        tamanho: 10,
        totalItens: 0,
        totalPaginas: 0,
      });

    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <AgendamentosPage />
      </MemoryRouter>
    );

    const botaoRecarregar = await screen.findByRole('button', { name: /tentar novamente/i });
    await user.click(botaoRecarregar);

    await screen.findByText(/20\/12\/2026, 14:00/i);
    expect(screen.getByText(/Grupo \(2 pacientes\)/i)).toBeInTheDocument();

    await user.selectOptions(screen.getByLabelText(/filtrar por situação/i), 'CANCELADO');
    await screen.findByText(/nenhum agendamento encontrado/i);
  });

  it('exibe conflito 409 no formulário junto aos campos de horário ao tentar criar horário ocupado', async () => {
    vi.mocked(agendamentoService.listarAgendamentos).mockResolvedValue({
      itens: [],
      pagina: 0,
      tamanho: 10,
      totalItens: 0,
      totalPaginas: 0,
    });

    const erro409 = new AxiosError(
      'Conflict',
      'ERR_BAD_REQUEST',
      undefined,
      undefined,
      {
        status: 409,
        data: {
          status: 409,
          mensagem: 'Já existe um agendamento conflitante neste horário',
          timestamp: '2026-11-18T10:00:00-03:00',
        },
      } as never
    );
    vi.mocked(agendamentoService.criarAgendamento).mockRejectedValue(erro409);

    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <AgendamentosPage />
      </MemoryRouter>
    );

    await user.click(await screen.findByRole('button', { name: /novo agendamento/i }));
    const dialog = await screen.findByRole('dialog');

    fireEvent.change(within(dialog).getByLabelText(/início/i), { target: { value: '2026-12-20T14:30' } });
    fireEvent.change(within(dialog).getByLabelText(/término/i), { target: { value: '2026-12-20T15:30' } });
    await user.click(within(dialog).getByRole('checkbox', { name: /lucas álvares/i }));
    await user.click(within(dialog).getByRole('button', { name: /criar agendamento/i }));

    await waitFor(() => {
      expect(within(dialog).getByText(/conflito de horário: já existe um agendamento conflitante/i)).toBeInTheDocument();
    });
  });

  it('abre diálogo de cancelamento com suporte a Esc e registra presença em sessão iniciada', async () => {
    vi.mocked(agendamentoService.listarAgendamentos).mockResolvedValue({
      itens: [AGENDAMENTO_EM_CURSO],
      pagina: 0,
      tamanho: 10,
      totalItens: 1,
      totalPaginas: 1,
    });
    vi.mocked(agendamentoService.cancelarAgendamento).mockResolvedValue();
    vi.mocked(agendamentoService.registrarPresenca).mockResolvedValue({
      ...AGENDAMENTO_EM_CURSO,
      possuiPresencaFuncionario: true,
      pacientes: [{ pacienteId: 11, nomePaciente: 'Lucas Álvares', possuiPresenca: true }],
    });

    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <AgendamentosPage />
      </MemoryRouter>
    );

    await screen.findByText('Dra. Ana Paula');

    await user.click(screen.getByRole('button', { name: /^presença$/i }));
    const modalPresenca = await screen.findByRole('dialog');
    await user.click(within(modalPresenca).getByRole('checkbox', { name: /dra\. ana paula/i }));
    await user.click(within(modalPresenca).getByRole('checkbox', { name: /lucas álvares/i }));
    await user.click(within(modalPresenca).getByRole('button', { name: /salvar presenças/i }));

    await waitFor(() => {
      expect(agendamentoService.registrarPresenca).toHaveBeenCalledWith(43, {
        possuiPresencaFuncionario: true,
        pacientes: [{ pacienteId: 11, possuiPresenca: true }],
      });
    });

    await user.click(screen.getByRole('button', { name: /^cancelar$/i }));
    await screen.findByRole('heading', { name: /confirmar cancelamento/i });
    fireEvent.keyDown(window, { key: 'Escape' });
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });
    expect(agendamentoService.cancelarAgendamento).not.toHaveBeenCalled();

    await user.click(screen.getByRole('button', { name: /^cancelar$/i }));
    const modalCancelar = await screen.findByRole('dialog');
    await user.click(within(modalCancelar).getByRole('button', { name: /confirmar cancelamento/i }));
    await waitFor(() => {
      expect(agendamentoService.cancelarAgendamento).toHaveBeenCalledWith(43);
    });
  });

  it('restringe ações e exibe apenas própria presença para perfil PACIENTE', async () => {
    configurarUsuario(PACIENTE_SESSAO);

    const itemVisaoPaciente: AgendamentoItem = {
      ...AGENDAMENTO_FUTURO,
      pacientes: [{ pacienteId: 11, nomePaciente: 'Lucas Álvares', possuiPresenca: true }],
    };

    vi.mocked(agendamentoService.listarAgendamentos).mockResolvedValue({
      itens: [itemVisaoPaciente],
      pagina: 0,
      tamanho: 10,
      totalItens: 1,
      totalPaginas: 1,
    });
    vi.mocked(agendamentoService.consultarAgendamento).mockResolvedValue(itemVisaoPaciente);

    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <AgendamentosPage />
      </MemoryRouter>
    );

    await screen.findByText('Dra. Ana Paula');
    expect(screen.queryByRole('button', { name: /novo agendamento/i })).not.toBeInTheDocument();
    expect(screen.queryByText(/marina costa/i)).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /detalhes/i }));
    const modalDetalhe = await screen.findByRole('dialog');
    await within(modalDetalhe).findByText('Minha Presença');
    expect(within(modalDetalhe).getByText('Lucas Álvares')).toBeInTheDocument();
    expect(within(modalDetalhe).queryByText(/marina costa/i)).not.toBeInTheDocument();
    expect(within(modalDetalhe).queryByRole('button', { name: /editar/i })).not.toBeInTheDocument();
  });
});
