import type { DirecaoOrdenacao } from '@/types/usuario';

export type TipoAgendamento = 'CONSULTA' | 'EXAME' | 'RETORNO' | 'AVALIACAO' | 'TERAPIA';

export type StatusAgendamento = 'AGENDADO' | 'CONCLUIDO' | 'CANCELADO';

export type OrdenacaoAgendamentoCampo = 'dataInicio' | 'dataFim' | 'status' | 'tipoAgendamento';

export interface AgendamentoPacienteItem {
  pacienteId: number;
  nomePaciente: string;
  possuiPresenca: boolean;
}

export interface AgendamentoItem {
  id: number;
  funcionarioId: number;
  nomeProfissional: string;
  cargoProfissional: string;
  dataInicio: string;
  dataFim: string;
  tipoAgendamento: TipoAgendamento;
  status: StatusAgendamento;
  possuiPresencaFuncionario: boolean;
  pacientes: AgendamentoPacienteItem[];
  criadoEm: string;
  atualizadoEm: string;
}

export interface ListaAgendamentosResponse {
  itens: AgendamentoItem[];
  pagina: number;
  tamanho: number;
  totalItens: number;
  totalPaginas: number;
}

export interface ListarAgendamentosParams {
  termo?: string;
  dataInicio?: string;
  dataFim?: string;
  status?: StatusAgendamento | '';
  tipoAgendamento?: TipoAgendamento | '';
  funcionarioId?: number | '';
  page?: number;
  size?: number;
  sort?: OrdenacaoAgendamentoCampo;
  direction?: DirecaoOrdenacao;
}

export interface CriarAgendamentoRequest {
  funcionarioId: number | null;
  dataInicio: string;
  dataFim: string;
  tipoAgendamento: TipoAgendamento;
  pacientesIds: number[];
}

export interface EditarAgendamentoRequest {
  funcionarioId: number | null;
  dataInicio: string;
  dataFim: string;
  tipoAgendamento: TipoAgendamento;
  pacientesIds: number[];
}

export interface PresencaPacienteInput {
  pacienteId: number;
  possuiPresenca: boolean;
}

export interface RegistrarPresencaRequest {
  possuiPresencaFuncionario: boolean | null;
  pacientes: PresencaPacienteInput[];
}

export interface ProfissionalAgendaOpcao {
  funcionarioId: number;
  usuarioId: number;
  nome: string;
  cargo: string;
  crm: string | null;
}

