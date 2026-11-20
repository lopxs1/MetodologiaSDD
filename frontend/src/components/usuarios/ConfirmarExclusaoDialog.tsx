import { useEffect, useRef, useState } from 'react';
import { AlertCircle, AlertTriangle } from 'lucide-react';
import { extrairErroApi } from '@/services/api';
import { usuarioService } from '@/services/usuarioService';
import type { UsuarioResumo } from '@/types/usuario';

interface ConfirmarExclusaoDialogProps {
  usuario: UsuarioResumo | null;
  onFechar: () => void;
  onExcluido: () => void;
}

export function ConfirmarExclusaoDialog({
  usuario,
  onFechar,
  onExcluido,
}: ConfirmarExclusaoDialogProps) {
  const [excluindo, setExcluindo] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const cancelarRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!usuario) {
      setErro(null);
      return;
    }

    cancelarRef.current?.focus();

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !excluindo) {
        onFechar();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [usuario, excluindo, onFechar]);

  if (!usuario) {
    return null;
  }

  const handleConfirmar = async () => {
    setExcluindo(true);
    setErro(null);
    try {
      await usuarioService.excluirUsuario(usuario.id);
      onExcluido();
    } catch (error) {
      setErro(extrairErroApi(error).mensagem);
    } finally {
      setExcluindo(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-md bg-neutral-text-primary/60"
      role="alertdialog"
      aria-modal="true"
      aria-labelledby="exclusao-titulo"
      aria-describedby="exclusao-descricao"
      onClick={(e) => {
        if (e.target === e.currentTarget && !excluindo) {
          onFechar();
        }
      }}
    >
      <div className="w-full max-w-md rounded-lg bg-neutral-surface border border-neutral-border p-lg shadow-xl">
        <div className="flex items-start gap-3">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-status-danger-bg text-status-danger">
            <AlertTriangle className="h-5 w-5" aria-hidden="true" />
          </div>
          <div>
            <h2 id="exclusao-titulo" className="text-lg font-semibold text-neutral-text-primary">
              Inativar usuário
            </h2>
            <p id="exclusao-descricao" className="mt-1 text-sm text-neutral-text-secondary">
              Deseja realmente inativar a conta de <strong>{usuario.nome}</strong> ({usuario.email})? O acesso será
              revogado imediatamente e o histórico clínico será preservado.
            </p>
          </div>
        </div>

        {erro && (
          <div
            role="alert"
            className="mt-md flex items-start gap-2 rounded-md border border-status-danger bg-status-danger-bg px-3 py-2 text-sm text-status-danger"
          >
            <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" aria-hidden="true" />
            <span>{erro}</span>
          </div>
        )}

        <div className="mt-lg flex justify-end gap-sm">
          <button
            ref={cancelarRef}
            type="button"
            onClick={onFechar}
            disabled={excluindo}
            className="rounded-md border border-neutral-border bg-neutral-surface px-4 py-2 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background disabled:opacity-50"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={handleConfirmar}
            disabled={excluindo}
            className="rounded-md bg-status-danger px-4 py-2 text-sm font-semibold text-neutral-surface hover:opacity-90 disabled:opacity-50 transition-opacity"
          >
            {excluindo ? 'Inativando...' : 'Confirmar inativação'}
          </button>
        </div>
      </div>
    </div>
  );
}
