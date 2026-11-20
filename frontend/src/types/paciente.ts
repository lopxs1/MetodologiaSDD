import type { DirecaoOrdenacao, GrauSuporte } from '@/types/usuario';

export type SituacaoDocumento = 'VIGENTE' | 'SUBSTITUIDO' | 'ARQUIVADO';

export type CategoriaProntuario = 'Exames' | 'Laudos' | 'Receituario' | 'Arquivado';

export type OrdenacaoPacienteCampo = 'nome' | 'email' | 'grauSuporte';

export interface PacienteResumoItem {
  id: number;
  usuarioId: number;
  nome: string;
  email: string;
  grauSuporte: GrauSuporte;
  numCelular: string | null;
}

export interface ListaPacientesResponse {
  itens: PacienteResumoItem[];
  pagina: number;
  tamanho: number;
  totalItens: number;
  totalPaginas: number;
}

export interface ListarPacientesParams {
  termo?: string;
  grauSuporte?: GrauSuporte | '';
  page?: number;
  size?: number;
  sort?: OrdenacaoPacienteCampo;
  direction?: DirecaoOrdenacao;
}

export interface DocumentoHistoricoResumo {
  id: number;
  tituloDocumento: string;
  nomeArquivoOriginal: string;
  categoria: CategoriaProntuario;
  situacao: SituacaoDocumento;
  registroSubstituidoId: number | null;
  criadoEm: string;
}

export interface CategoriaHistorico {
  categoria: CategoriaProntuario;
  totalDocumentos: number;
  pagina: number;
  tamanho: number;
  totalPaginas: number;
  documentos: DocumentoHistoricoResumo[];
}

export interface PacienteFicha {
  id: number;
  usuarioId: number;
  nome: string;
  email: string;
  dataNascimento: string | null;
  numCelular: string | null;
  celularMascarado: boolean;
  pais: string | null;
  estado: string | null;
  cidade: string | null;
  rua: string | null;
  grauSuporte: GrauSuporte;
  descricao: string | null;
  historicoPorCategoria: CategoriaHistorico[];
  atualizadoEm: string;
}

export interface AtualizarDadosClinicosRequest {
  grauSuporte: GrauSuporte;
  descricao: string | null;
}

