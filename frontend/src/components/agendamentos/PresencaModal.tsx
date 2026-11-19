import { useEffect, useState, type FormEvent } from 'react';
import { AlertCircle, CheckSquare, X } from 'lucide-react';
import {
  formatarIntervaloHorarioSp,
  ROTULO_STATUS_AGENDAMENTO,
  ROTULO_TIPO_AGENDAMENTO,
} from '@/components/agendamentos/agendamentoUtils';
import { extrairErroApi } from '@/services/api';
import { agendamentoService } from '@/services/agendamentoService';
import type { AgendamentoItem } from '@/types/agendamento';

interface PresencaModalProps {
  agendamento: AgendamentoItem | null;
  onFechar: () => void;
  onAtualizado: () => void;
}

export function PresencaModal({ agendamento, onFechar, onAtualizado }: PresencaModalProps) {
  const [presencaProfissional, setPresencaProfissional] = useState(false);
  const [presencaPacientes, setPresencaPacientes] = useState<Record<number, boolean>>({});
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (!agendamento) {
      return;
    }
    setErro(null);
    setPresencaProfissional(agendamento.possuiPresencaFuncionario);
    const mapa: Record<number, boolean> = {};
    for (const pac of agendamento.pacientes) {
      mapa[pac.pacienteId] = pac.possuiPresenca;
    }
    setPresencaPacientes(mapa);
  }, [agendamento]);

  useEffect(() => {
    if (!agendamento) {
      return;
    }
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !salvando) {
        onFechar();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [agendamento, salvando, onFechar]);

  if (!agendamento) {
    return null;
  }

  const handleTogglePaciente = (pacienteId: number) => {
    setPresencaPacientes((prev) => ({
      ...prev,
      [pacienteId]: !prev[pacienteId],
    }));
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setSalvando(true);
    setErro(null);
    try {
      await agendamentoService.registrarPresenca(agendamento.id, {
        possuiPresencaFuncionario: presencaProfissional,
        pacientes: agendamento.pacientes.map((pac) => ({
          pacienteId: pac.pacienteId,
          possuiPresenca: Boolean(presencaPacientes[pac.pacienteId]),
        })),
      });
      onAtualizado();
    } catch (error) {
      setErro(extrairErroApi(error).mensagem);
    } finally {
      setSalvando(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-md bg-neutral-text-primary/50"
      role="dialog"
      aria-modal="true"
      aria-labelledby="presenca-modal-titulo"
      onClick={(e) => {
        if (e.target === e.currentTarget && !salvando) {
          onFechar();
        }
      }}
    >
      <div className="w-full max-w-lg rounded-lg border border-neutral-border bg-neutral-surface shadow-lg">
        <div className="flex items-center justify-between px-lg py-md border-b border-neutral-border">
          <div className="flex items-center gap-2">
            <CheckSquare className="h-5 w-5 text-brand-primary" aria-hidden="true" />
            <h2 id="presenca-modal-titulo" className="text-base font-semibold text-neutral-text-primary">
              Registrar Presenças
            </h2>
          </div>
          <button
            type="button"
            onClick={onFechar}
            disabled={salvando}
            className="rounded-md p-1 text-neutral-text-muted hover:text-neutral-text-primary"
            aria-label="Fechar registro de presenças"
          >
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-lg space-y-md">
          {erro && (
            <div
              role="alert"
              className="flex items-start gap-2 rounded-md border border-status-danger bg-status-danger-bg px-3 py-2 text-sm text-status-danger"
            >
              <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" aria-hidden="true" />
              <span>{erro}</span>
            </div>
          )}

          <div className="rounded-md border border-neutral-border bg-neutral-background px-md py-sm text-sm space-y-1">
            <p className="font-medium text-neutral-text-primary">
              {ROTULO_TIPO_AGENDAMENTO[agendamento.tipoAgendamento]} · {ROTULO_STATUS_AGENDAMENTO[agendamento.status]}
            </p>
            <p className="text-xs text-neutral-text-secondary">
              {formatarIntervaloHorarioSp(agendamento.dataInicio, agendamento.dataFim)} (horário de Brasília)
            </p>
          </div>

          <div className="space-y-2">
            <p className="text-xs font-semibold uppercase tracking-wide text-neutral-text-secondary">
              Profissional Responsável
            </p>
            <label className="flex items-center gap-3 rounded-md border border-neutral-border px-md py-sm cursor-pointer hover:bg-neutral-background">
              <input
                type="checkbox"
                checked={presencaProfissional}
                onChange={(e) => setPresencaProfissional(e.target.checked)}
                className="h-4 w-4 rounded border-neutral-border text-brand-primary focus:ring-brand-secondary"
              />
              <div>
                <p className="text-sm font-medium text-neutral-text-primary">{agendamento.nomeProfissional}</p>
                <p className="text-xs text-neutral-text-secondary">{agendamento.cargoProfissional}</p>
              </div>
            </label>
          </div>

          <div className="space-y-2">
            <p className="text-xs font-semibold uppercase tracking-wide text-neutral-text-secondary">
              Pacientes Participantes ({agendamento.pacientes.length})
            </p>
            <div className="max-h-56 overflow-y-auto space-y-2 pr-1">
              {agendamento.pacientes.map((pac) => (
                <label
                  key={pac.pacienteId}
                  className="flex items-center justify-between gap-3 rounded-md border border-neutral-border px-md py-sm cursor-pointer hover:bg-neutral-background"
                >
                  <div className="flex items-center gap-3">
                    <input
                      type="checkbox"
                      checked={Boolean(presencaPacientes[pac.pacienteId])}
                      onChange={() => handleTogglePaciente(pac.pacienteId)}
                      className="h-4 w-4 rounded border-neutral-border text-brand-primary focus:ring-brand-secondary"
                    />
                    <span className="text-sm font-medium text-neutral-text-primary">{pac.nomePaciente}</span>
                  </div>
                  <span
                    className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                      presencaPacientes[pac.pacienteId]
                        ? 'bg-status-success-bg text-status-success'
                        : 'bg-neutral-background text-neutral-text-secondary'
                    }`}
                  >
                    {presencaPacientes[pac.pacienteId] ? 'Presente' : 'Não confirmada'}
                  </span>
                </label>
              ))}
            </div>
          </div>

          <div className="flex justify-end gap-sm pt-sm border-t border-neutral-border">
            <button
              type="button"
              onClick={onFechar}
              disabled={salvando}
              className="rounded-md border border-neutral-border bg-neutral-surface px-4 py-2 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={salvando}
              className="rounded-md bg-brand-primary px-4 py-2 text-sm font-semibold text-neutral-surface hover:bg-brand-primary-hover disabled:opacity-50"
            >
              {salvando ? 'Salvando...' : 'Salvar Presenças'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

