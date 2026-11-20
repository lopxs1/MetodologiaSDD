import { agendamentoService } from '@/services/agendamentoService';
import { api } from '@/services/api';
import type { ProfissionalAgendaOpcao } from '@/types/agendamento';
import type { DashboardFiltroParams, DashboardResponse } from '@/types/dashboard';

export const dashboardService = {
  async obterMetricas(params: DashboardFiltroParams = {}): Promise<DashboardResponse> {
    const query: Record<string, string | number> = {};
    if (params.dataInicio) {
      query.dataInicio = params.dataInicio;
    }
    if (params.dataFim) {
      query.dataFim = params.dataFim;
    }
    if (params.funcionarioId !== undefined && params.funcionarioId !== '') {
      query.funcionarioId = params.funcionarioId;
    }

    const { data } = await api.get<DashboardResponse>('/api/dashboard', { params: query });
    return data;
  },

  async listarProfissionais(): Promise<ProfissionalAgendaOpcao[]> {
    return agendamentoService.listarProfissionaisClinicos();
  },
};

