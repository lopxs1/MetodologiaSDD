import { api } from '@/services/api';
import type {
  AtualizarDadosClinicosRequest,
  ListaPacientesResponse,
  ListarPacientesParams,
  PacienteFicha,
} from '@/types/paciente';

export interface ConsultarFichaParams {
  revelarCelular?: boolean;
  docPage?: number;
  docSize?: number;
}

export const pacienteService = {
  async listarPacientes(params: ListarPacientesParams = {}): Promise<ListaPacientesResponse> {
    const query: Record<string, string | number> = {
      page: params.page ?? 0,
      size: params.size ?? 10,
      sort: params.sort ?? 'nome',
      direction: params.direction ?? 'ASC',
    };
    if (params.termo && params.termo.trim().length > 0) {
      query.termo = params.termo.trim();
    }
    if (params.grauSuporte) {
      query.grauSuporte = params.grauSuporte;
    }

    const { data } = await api.get<ListaPacientesResponse>('/api/pacientes', { params: query });
    return data;
  },

  async consultarFichaPaciente(id: number, params: ConsultarFichaParams = {}): Promise<PacienteFicha> {
    const { data } = await api.get<PacienteFicha>(`/api/pacientes/${id}`, {
      params: {
        revelarCelular: params.revelarCelular ?? false,
        docPage: params.docPage ?? 0,
        docSize: params.docSize ?? 10,
      },
    });
    return data;
  },

  async consultarMeuProntuario(params: Omit<ConsultarFichaParams, 'revelarCelular'> = {}): Promise<PacienteFicha> {
    const { data } = await api.get<PacienteFicha>('/api/pacientes/meu-prontuario', {
      params: {
        docPage: params.docPage ?? 0,
        docSize: params.docSize ?? 10,
      },
    });
    return data;
  },

  async atualizarDadosClinicos(id: number, payload: AtualizarDadosClinicosRequest): Promise<PacienteFicha> {
    const { data } = await api.put<PacienteFicha>(`/api/pacientes/${id}`, payload);
    return data;
  },
};

