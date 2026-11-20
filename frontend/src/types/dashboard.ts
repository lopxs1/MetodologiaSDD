export interface DashboardContadores {
  totalConsultas: number;
  consultasHoje: number;
  totalPacientes: number;
  profissionaisAtivos: number;
}

export interface ConsultaDiaItem {
  data: string;
  quantidade: number;
}

export interface ConsultaEspecialidadeItem {
  especialidade: string;
  quantidade: number;
  percentual: number;
}

export interface DashboardResponse {
  contadores: DashboardContadores;
  consultasAoLongoDoTempo: ConsultaDiaItem[];
  consultasPorEspecialidade: ConsultaEspecialidadeItem[];
}

export interface DashboardFiltroParams {
  dataInicio?: string;
  dataFim?: string;
  funcionarioId?: number | '';
}

