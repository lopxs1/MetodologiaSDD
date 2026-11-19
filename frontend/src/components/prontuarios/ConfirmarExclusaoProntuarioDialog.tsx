import { useEffect, useState } from 'react';
import { AlertCircle, AlertTriangle, Trash2, X } from 'lucide-react';
import { extrairErroApi } from '@/services/api';
import { prontuarioService } from '@/services/prontuarioService';
import type { DocumentoHistoricoResumo } from '@/types/paciente';

interface ConfirmarExclusaoProntuarioDialogProps {
  documento: DocumentoHistoricoResumo | null;
  onFechar: () => void;
  onExcluido: () => void;
}

export function ConfirmarExclusaoProntuarioDialog({
  documento,
  onFechar,
  onExcluido,
}: ConfirmarExclusaoProntuarioDialogProps) {
  const [excluindo, setExcluindo] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    setErro(null);
  }, [documento]);

  useEffect(() => {
    if (!documento) {
      return;
    }
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !excluindo) {
        onFechar();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [documento, excluindo, onFechar]);

  if (!documento) {
    return null;
  }

  const handleConfirmar = async () => {
    setExcluindo(true);
    setErro(null);
    try {
      await prontuarioService.excluirDocumento(documento.id);
      onExcluido();
    } catch (error) {
      setErro(extrairErroApi(error).mensagem);
    } finally {
      setExcluindo(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-[60] flex items-center justify-center p-md bg-neutral-text-primary/50"
      role="dialog"
      aria-modal="true"
      aria-labelledby="excluir-prontuario-titulo"
      onClick={(e) => {
        if (e.target === e.currentTarget && !excluindo) {
          onFechar();
        }
      }}
    >
      <div className="w-full max-w-md rounded-lg border border-neutral-border bg-neutral-surface shadow-lg">
        <div className="flex items-center justify-between px-lg py-md border-b border-neutral-border">
          <div className="flex items-center gap-2 text-status-danger">
            <AlertTriangle className="h-5 w-5" aria-hidden="true" />
            <h2 id="excluir-prontuario-titulo" className="text-base font-semibold text-neutral-text-primary">
              Confirmar Exclusão de Documento
            </h2>
          </div>
          <button
            type="button"
            onClick={onFechar}
            disabled={excluindo}
            className="rounded-md p-1 text-neutral-text-muted hover:text-neutral-text-primary"
            aria-label="Fechar confirmação de exclusão"
          >
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </div>

        <div className="p-lg space-y-md">
          {erro && (
            <div
              role="alert"
              className="flex items-start gap-2 rounded-md border border-status-danger bg-status-danger-bg px-3 py-2 text-sm text-status-danger"
            >
              <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" aria-hidden="true" />
              <span>{erro}</span>
            </div>
          )}

          <p className="text-sm text-neutral-text-primary">
            Tem certeza que deseja excluir o documento{' '}
            <strong>{documento.tituloDocumento}</strong>{' '}
            (<span className="font-mono text-xs">{documento.nomeArquivoOriginal}</span>)?
          </p>

          <p className="text-xs text-neutral-text-secondary">
            Esta ação registra a operação na trilha de auditoria, remove o arquivo PDF físico do armazenamento e retira o documento do histórico do prontuário.
          </p>
        </div>

        <div className="flex justify-end gap-sm px-lg py-md border-t border-neutral-border">
          <button
            type="button"
            onClick={onFechar}
            disabled={excluindo}
            className="rounded-md border border-neutral-border bg-neutral-surface px-4 py-2 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={handleConfirmar}
            disabled={excluindo}
            className="inline-flex items-center gap-2 rounded-md bg-status-danger px-4 py-2 text-sm font-semibold text-neutral-surface hover:opacity-90 disabled:opacity-50"
          >
            <Trash2 className="h-4 w-4" aria-hidden="true" />
            {excluindo ? 'Excluindo...' : 'Confirmar Exclusão'}
          </button>
        </div>
      </div>
    </div>
  );
}

