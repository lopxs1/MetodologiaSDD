import { api } from '@/services/api';
import { usuarioService } from '@/services/usuarioService';
import type {
  AgendamentoItem,
  CriarAgendamentoRequest,
  EditarAgendamentoRequest,
  ListaAgendamentosResponse,
  ListarAgendamentosParams,
  ProfissionalAgendaOpcao,
  RegistrarPresencaRequest,
} from '@/types/agendamento';

export const agendamentoService = {
  async listarAgendamentos(params: ListarAgendamentosParams = {}): Promise<ListaAgendamentosResponse> {
    const query: Record<string, string | number> = {
      page: params.page ?? 0,
      size: params.size ?? 10,
      sort: params.sort ?? 'dataInicio',
      direction: params.direction ?? 'ASC',
    };
    if (params.termo && params.termo.trim().length > 0) {
      query.termo = params.termo.trim();
    }
    if (params.dataInicio) {
      query.dataInicio = params.dataInicio;
    }
    if (params.dataFim) {
      query.dataFim = params.dataFim;
    }
    if (params.status) {
      query.status = params.status;
    }
    if (params.tipoAgendamento) {
      query.tipoAgendamento = params.tipoAgendamento;
    }
    if (params.funcionarioId !== undefined && params.funcionarioId !== '') {
      query.funcionarioId = params.funcionarioId;
    }

    const { data } = await api.get<ListaAgendamentosResponse>('/api/agendamentos', { params: query });
    return data;
  },

  async consultarAgendamento(id: number): Promise<AgendamentoItem> {
    const { data } = await api.get<AgendamentoItem>(`/api/agendamentos/${id}`);
    return data;
  },

  async criarAgendamento(payload: CriarAgendamentoRequest): Promise<AgendamentoItem> {
    const { data } = await api.post<AgendamentoItem>('/api/agendamentos', payload);
    return data;
  },

  async editarAgendamento(id: number, payload: EditarAgendamentoRequest): Promise<AgendamentoItem> {
    const { data } = await api.put<AgendamentoItem>(`/api/agendamentos/${id}`, payload);
    return data;
  },

  async cancelarAgendamento(id: number): Promise<void> {
    await api.delete(`/api/agendamentos/${id}`);
  },

  async registrarPresenca(id: number, payload: RegistrarPresencaRequest): Promise<AgendamentoItem> {
    const { data } = await api.patch<AgendamentoItem>(`/api/agendamentos/${id}/presenca`, payload);
    return data;
  },

  async concluirAgendamento(id: number): Promise<AgendamentoItem> {
    const { data } = await api.patch<AgendamentoItem>(`/api/agendamentos/${id}/concluir`);
    return data;
  },

  async listarProfissionaisClinicos(): Promise<ProfissionalAgendaOpcao[]> {
    const lista = await usuarioService.listarUsuarios({
      nivelAcesso: 'MEDICO',
      page: 0,
      size: 50,
      sort: 'nome',
      direction: 'ASC',
    });

    const detalhes = await Promise.all(
      lista.itens.map(async (u) => {
        try {
          return await usuarioService.consultarUsuarioPorId(u.id, false);
        } catch {
          return null;
        }
      })
    );

    const opcoes: ProfissionalAgendaOpcao[] = [];
    for (const det of detalhes) {
      if (det?.funcionario) {
        opcoes.push({
          funcionarioId: det.funcionario.id,
          usuarioId: det.id,
          nome: det.nome,
          cargo: det.funcionario.cargo,
          crm: det.funcionario.crm,
        });
      }
    }
    return opcoes;
  },
};

