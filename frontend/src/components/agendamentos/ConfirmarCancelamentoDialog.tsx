import { useEffect, useState } from 'react';
import { AlertCircle, AlertTriangle, Ban, X } from 'lucide-react';
import { formatarIntervaloHorarioSp, ROTULO_TIPO_AGENDAMENTO } from '@/components/agendamentos/agendamentoUtils';
import { extrairErroApi } from '@/services/api';
import { agendamentoService } from '@/services/agendamentoService';
import type { AgendamentoItem } from '@/types/agendamento';

interface ConfirmarCancelamentoDialogProps {
  agendamento: AgendamentoItem | null;
  onFechar: () => void;
  onCancelado: () => void;
}

export function ConfirmarCancelamentoDialog({
  agendamento,
  onFechar,
  onCancelado,
}: ConfirmarCancelamentoDialogProps) {
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    setErro(null);
  }, [agendamento]);

  useEffect(() => {
    if (!agendamento) {
      return;
    }
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !enviando) {
        onFechar();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [agendamento, enviando, onFechar]);

  if (!agendamento) {
    return null;
  }

  const handleConfirmar = async () => {
    setEnviando(true);
    setErro(null);
    try {
      await agendamentoService.cancelarAgendamento(agendamento.id);
      onCancelado();
    } catch (error) {
      setErro(extrairErroApi(error).mensagem);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-[60] flex items-center justify-center p-md bg-neutral-text-primary/50"
      role="dialog"
      aria-modal="true"
      aria-labelledby="cancelar-agendamento-titulo"
      onClick={(e) => {
        if (e.target === e.currentTarget && !enviando) {
          onFechar();
        }
      }}
    >
      <div className="w-full max-w-md rounded-lg border border-neutral-border bg-neutral-surface shadow-lg">
        <div className="flex items-center justify-between px-lg py-md border-b border-neutral-border">
          <div className="flex items-center gap-2 text-status-danger">
            <AlertTriangle className="h-5 w-5" aria-hidden="true" />
            <h2 id="cancelar-agendamento-titulo" className="text-base font-semibold text-neutral-text-primary">
              Confirmar Cancelamento
            </h2>
          </div>
          <button
            type="button"
            onClick={onFechar}
            disabled={enviando}
            className="rounded-md p-1 text-neutral-text-muted hover:text-neutral-text-primary focus:outline-none focus:ring-2 focus:ring-brand-secondary"
            aria-label="Fechar confirmação de cancelamento"
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
            Deseja realmente cancelar o agendamento de{' '}
            <strong>{ROTULO_TIPO_AGENDAMENTO[agendamento.tipoAgendamento]}</strong> com{' '}
            <strong>{agendamento.nomeProfissional}</strong> em{' '}
            <span className="font-medium">
              {formatarIntervaloHorarioSp(agendamento.dataInicio, agendamento.dataFim)}
            </span>{' '}
            (horário de Brasília)?
          </p>

          <p className="text-xs text-neutral-text-secondary">
            O registro histórico será mantido com situação Cancelado e o horário voltará a ficar disponível na agenda do profissional.
          </p>
        </div>

        <div className="flex justify-end gap-sm px-lg py-md border-t border-neutral-border">
          <button
            type="button"
            onClick={onFechar}
            disabled={enviando}
            className="rounded-md border border-neutral-border bg-neutral-surface px-4 py-2 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background focus:outline-none focus:ring-2 focus:ring-brand-secondary"
          >
            Voltar
          </button>
          <button
            type="button"
            onClick={handleConfirmar}
            disabled={enviando}
            className="inline-flex items-center gap-2 rounded-md bg-status-danger px-4 py-2 text-sm font-semibold text-neutral-surface hover:opacity-90 disabled:opacity-50 focus:outline-none focus:ring-2 focus:ring-status-danger"
          >
            <Ban className="h-4 w-4" aria-hidden="true" />
            {enviando ? 'Cancelando...' : 'Confirmar Cancelamento'}
          </button>
        </div>
      </div>
    </div>
  );
}

