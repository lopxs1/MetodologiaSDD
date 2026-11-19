import type { CategoriaProntuario, SituacaoDocumento } from '@/types/paciente';

export interface ProntuarioResponse {
  id: number;
  pacienteId: number;
  categoria: CategoriaProntuario;
  registroSubstituidoId: number | null;
  tituloDocumento: string;
  nomeArquivoOriginal: string;
  situacao: SituacaoDocumento;
  criadoEm: string;
  atualizadoEm: string;
}

export interface UploadProntuarioParams {
  pacienteId: number;
  tituloDocumento: string;
  categoria: CategoriaProntuario;
  arquivo: File;
  registroSubstituidoId?: number | null;
  onProgress?: (percentual: number) => void;
}

export interface SubstituirProntuarioParams {
  documentoId: number;
  tituloDocumento?: string | null;
  arquivo: File;
  onProgress?: (percentual: number) => void;
}

