import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AppLayout } from '@/components/layout/AppLayout';
import { useAuth } from '@/contexts/AuthContext';
import { DashboardPage } from '@/pages/DashboardPage';
import { dashboardService } from '@/services/dashboardService';
import type { DashboardResponse } from '@/types/dashboard';
import type { UsuarioSessao } from '@/types/usuario';

vi.mock('@/contexts/AuthContext', () => ({
  useAuth: vi.fn(),
}));

vi.mock('@/services/dashboardService', () => ({
  dashboardService: {
    obterMetricas: vi.fn(),
    listarProfissionais: vi.fn(),
  },
}));

const DADOS_DASHBOARD: DashboardResponse = {
  contadores: {
    totalConsultas: 10,
    consultasHoje: 3,
    totalPacientes: 5,
    profissionaisAtivos: 4,
  },
  consultasAoLongoDoTempo: [
    { data: '2026-11-16', quantidade: 4 },
    { data: '2026-11-17', quantidade: 3 },
    { data: '2026-11-18', quantidade: 3 },
  ],
  consultasPorEspecialidade: [
    { especialidade: 'Psicologia', quantidade: 4, percentual: 40.0 },
    { especialidade: 'Fonoaudiologia', quantidade: 3, percentual: 30.0 },
    { especialidade: 'Terapia Ocupacional', quantidade: 3, percentual: 30.0 },
  ],
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

describe('DashboardPage e Menu Gerencial', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    configurarUsuario({
      id: 1,
      nome: 'Administrador AMDE',
      email: 'admin@amde.example.org',
      nivelAcesso: 'ADMINISTRADOR',
    });
    vi.mocked(dashboardService.listarProfissionais).mockResolvedValue([
      {
        funcionarioId: 3,
        usuarioId: 10,
        nome: 'Dra. Helena Silva',
        cargo: 'Psicologia',
        crm: 'CRP 06/1234',
      },
    ]);
  });

  it('renderiza Loading com skeleton, Erro com botão Recarregar e recupera para Sucesso', async () => {
    const obterMock = vi.mocked(dashboardService.obterMetricas);

    obterMock.mockImplementationOnce(() => new Promise(() => {}));
    const { unmount } = render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );
    expect(screen.getByLabelText('Carregando indicadores do dashboard')).toBeInTheDocument();
    unmount();

    obterMock.mockRejectedValueOnce(new Error('Erro HTTP 500'));
    obterMock.mockResolvedValueOnce(DADOS_DASHBOARD);

    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    const alerta = await screen.findByRole('alert');
    expect(alerta).toHaveTextContent('Não foi possível carregar os indicadores do dashboard');
    expect(screen.queryByText('Total de Consultas')).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /Recarregar/i }));

    expect(await screen.findByText('Total de Consultas')).toBeInTheDocument();
    expect(screen.getByText('10')).toBeInTheDocument();
  });

  it('exibe os 4 contadores, os 2 gráficos e as tabelas alternativas acessíveis', async () => {
    vi.mocked(dashboardService.obterMetricas).mockResolvedValueOnce(DADOS_DASHBOARD);

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Total de Consultas')).toBeInTheDocument();
    const secaoIndicadores = screen.getByRole('region', { name: 'Indicadores principais' });
    expect(within(secaoIndicadores).getByText('10')).toBeInTheDocument();
    expect(within(secaoIndicadores).getByText('Consultas Hoje')).toBeInTheDocument();
    expect(within(secaoIndicadores).getByText('3')).toBeInTheDocument();
    expect(within(secaoIndicadores).getByText('Total de Pacientes')).toBeInTheDocument();
    expect(within(secaoIndicadores).getByText('5')).toBeInTheDocument();
    expect(within(secaoIndicadores).getByText('Profissionais Ativos')).toBeInTheDocument();
    expect(within(secaoIndicadores).getByText('4')).toBeInTheDocument();

    const tabelaTempo = screen.getByRole('table', { name: 'Tabela de Consultas ao Longo do Tempo' });
    expect(within(tabelaTempo).getByText('16/11/2026')).toBeInTheDocument();
    expect(within(tabelaTempo).getByText('18/11/2026')).toBeInTheDocument();

    const tabelaEsp = screen.getByRole('table', { name: 'Tabela de Consultas por Especialidade' });
    expect(within(tabelaEsp).getByText('Psicologia')).toBeInTheDocument();
    expect(within(tabelaEsp).getByText('40,0%')).toBeInTheDocument();
    expect(within(tabelaEsp).getByText('Fonoaudiologia')).toBeInTheDocument();
    expect(within(tabelaEsp).getByText('Terapia Ocupacional')).toBeInTheDocument();
  });

  it('exibe estado vazio orientativo sem falhas quando totalConsultas for zero', async () => {
    vi.mocked(dashboardService.obterMetricas).mockResolvedValueOnce({
      contadores: {
        totalConsultas: 0,
        consultasHoje: 0,
        totalPacientes: 2,
        profissionaisAtivos: 1,
      },
      consultasAoLongoDoTempo: [],
      consultasPorEspecialidade: [],
    });

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    const mensagensVazio = await screen.findAllByText('Nenhuma consulta encontrada para os filtros selecionados');
    expect(mensagensVazio).toHaveLength(2);
    expect(screen.getAllByText('0').length).toBeGreaterThanOrEqual(2);
  });

  it('valida período invertido e aplica filtros de período e profissional', async () => {
    const obterMock = vi.mocked(dashboardService.obterMetricas);
    obterMock.mockResolvedValue(DADOS_DASHBOARD);

    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    await screen.findByText('Total de Consultas');

    const inputInicio = screen.getByLabelText('Data inicial');
    const inputFim = screen.getByLabelText('Data final');
    const selectProf = screen.getByLabelText('Profissional');
    const botaoAplicar = screen.getByRole('button', { name: 'Aplicar filtros' });

    fireEvent.change(inputInicio, { target: { value: '2026-11-20' } });
    fireEvent.change(inputFim, { target: { value: '2026-11-10' } });

    expect(screen.getByText('A data inicial não pode ser posterior à data final')).toBeInTheDocument();
    expect(botaoAplicar).toBeDisabled();

    fireEvent.change(inputInicio, { target: { value: '2026-11-01' } });
    fireEvent.change(inputFim, { target: { value: '2026-11-18' } });
    await user.selectOptions(selectProf, '3');
    await user.click(botaoAplicar);

    await waitFor(() => {
      expect(obterMock).toHaveBeenLastCalledWith({
        dataInicio: '2026-11-01',
        dataFim: '2026-11-18',
        funcionarioId: 3,
      });
    });
  });

  it('exibe item Visão Geral / Dashboard apenas para ADMINISTRADOR e oculta para MEDICO e PACIENTE', () => {
    const { rerender } = render(
      <MemoryRouter>
        <AppLayout />
      </MemoryRouter>
    );
    expect(screen.getByRole('link', { name: 'Visão Geral / Dashboard' })).toBeInTheDocument();

    configurarUsuario({
      id: 2,
      nome: 'Dra. Helena',
      email: 'helena@amde.example.org',
      nivelAcesso: 'MEDICO',
    });
    rerender(
      <MemoryRouter>
        <AppLayout />
      </MemoryRouter>
    );
    expect(screen.queryByRole('link', { name: /Visão Geral/i })).not.toBeInTheDocument();

    configurarUsuario({
      id: 3,
      nome: 'Lucas Paciente',
      email: 'lucas@amde.example.org',
      nivelAcesso: 'PACIENTE',
    });
    rerender(
      <MemoryRouter>
        <AppLayout />
      </MemoryRouter>
    );
    expect(screen.queryByRole('link', { name: /Visão Geral/i })).not.toBeInTheDocument();
  });
});
