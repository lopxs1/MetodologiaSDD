import { api } from '@/services/api';
import type { CategoriaProntuario } from '@/types/paciente';
import type {
  ProntuarioResponse,
  SubstituirProntuarioParams,
  UploadProntuarioParams,
} from '@/types/prontuario';

export const prontuarioService = {
  async uploadDocumento(params: UploadProntuarioParams): Promise<ProntuarioResponse> {
    const formData = new FormData();
    formData.append('pacienteId', String(params.pacienteId));
    formData.append('tituloDocumento', params.tituloDocumento.trim());
    formData.append('categoria', params.categoria);
    if (params.registroSubstituidoId != null) {
      formData.append('registroSubstituidoId', String(params.registroSubstituidoId));
    }
    formData.append('arquivo', params.arquivo);

    const { data } = await api.post<ProntuarioResponse>('/api/prontuarios/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (event) => {
        if (params.onProgress && event.total && event.total > 0) {
          params.onProgress(Math.min(100, Math.round((event.loaded * 100) / event.total)));
        }
      },
    });
    return data;
  },

  async substituirDocumento(params: SubstituirProntuarioParams): Promise<ProntuarioResponse> {
    const formData = new FormData();
    if (params.tituloDocumento && params.tituloDocumento.trim().length > 0) {
      formData.append('tituloDocumento', params.tituloDocumento.trim());
    }
    formData.append('arquivo', params.arquivo);

    const { data } = await api.post<ProntuarioResponse>(
      `/api/prontuarios/${params.documentoId}/substituir`,
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (event) => {
          if (params.onProgress && event.total && event.total > 0) {
            params.onProgress(Math.min(100, Math.round((event.loaded * 100) / event.total)));
          }
        },
      }
    );
    return data;
  },

  async reclassificarCategoria(documentoId: number, categoria: CategoriaProntuario): Promise<ProntuarioResponse> {
    const { data } = await api.patch<ProntuarioResponse>(`/api/prontuarios/${documentoId}/categoria`, {
      categoria,
    });
    return data;
  },

  async baixarDocumento(documentoId: number, nomeArquivoOriginal: string): Promise<void> {
    const response = await api.get<Blob>(`/api/prontuarios/${documentoId}/download`, {
      responseType: 'blob',
    });

    const blob = response.data instanceof Blob
      ? response.data
      : new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    try {
      const link = document.createElement('a');
      link.href = url;
      link.download = nomeArquivoOriginal;
      document.body.appendChild(link);
      link.click();
      link.remove();
    } finally {
      window.URL.revokeObjectURL(url);
    }
  },

  async excluirDocumento(documentoId: number): Promise<void> {
    await api.delete(`/api/prontuarios/${documentoId}`);
  },
};

