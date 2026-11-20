import { useEffect, useState } from 'react';
import { AlertCircle, Ban, CalendarClock, CheckCircle2, CheckSquare, Edit3, RefreshCw, X } from 'lucide-react';
import {
  CLASSE_BADGE_STATUS,
  formatarDataHoraSp,
  formatarIntervaloHorarioSp,
  ROTULO_STATUS_AGENDAMENTO,
  ROTULO_TIPO_AGENDAMENTO,
  sessaoJaIniciou,
} from '@/components/agendamentos/agendamentoUtils';
import { extrairErroApi } from '@/services/api';
import { agendamentoService } from '@/services/agendamentoService';
import type { AgendamentoItem } from '@/types/agendamento';
import type { NivelAcesso } from '@/types/usuario';

interface AgendamentoDetalheModalProps {
  agendamentoId: number | null;
  nivelAcesso: NivelAcesso;
  recarregarToken?: number;
  onFechar: () => void;
  onEditar: (agendamento: AgendamentoItem) => void;
  onRegistrarPresenca: (agendamento: AgendamentoItem) => void;
  onConcluir: (agendamento: AgendamentoItem) => Promise<void>;
  onSolicitarCancelamento: (agendamento: AgendamentoItem) => void;
}

export function AgendamentoDetalheModal({
  agendamentoId,
  nivelAcesso,
  recarregarToken = 0,
  onFechar,
  onEditar,
  onRegistrarPresenca,
  onConcluir,
  onSolicitarCancelamento,
}: AgendamentoDetalheModalProps) {
  const [detalhe, setDetalhe] = useState<AgendamentoItem | null>(null);
  const [carregando, setCarregando] = useState(false);
  const [concluindo, setConcluindo] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const carregar = async (id: number) => {
    setCarregando(true);
    setErro(null);
    try {
      const dados = await agendamentoService.consultarAgendamento(id);
      setDetalhe(dados);
    } catch (error) {
      setErro(extrairErroApi(error).mensagem);
      setDetalhe(null);
    } finally {
      setCarregando(false);
    }
  };

  useEffect(() => {
    if (agendamentoId === null) {
      setDetalhe(null);
      setErro(null);
      return;
    }
    void carregar(agendamentoId);
  }, [agendamentoId, recarregarToken]);

  useEffect(() => {
    if (agendamentoId === null) {
      return;
    }
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !concluindo) {
        onFechar();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [agendamentoId, concluindo, onFechar]);

  if (agendamentoId === null) {
    return null;
  }

  const podeGerenciar = nivelAcesso === 'ADMINISTRADOR' || nivelAcesso === 'MEDICO';
  const iniciada = detalhe ? sessaoJaIniciou(detalhe.dataInicio) : false;

  const handleConcluirClick = async () => {
    if (!detalhe) {
      return;
    }
    setConcluindo(true);
    setErro(null);
    try {
      await onConcluir(detalhe);
      await carregar(detalhe.id);
    } catch (error) {
      setErro(extrairErroApi(error).mensagem);
    } finally {
      setConcluindo(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-md bg-neutral-text-primary/50"
      role="dialog"
      aria-modal="true"
      aria-labelledby="detalhe-agendamento-titulo"
      onClick={(e) => {
        if (e.target === e.currentTarget && !concluindo) {
          onFechar();
        }
      }}
    >
      <div className="w-full max-w-2xl rounded-lg border border-neutral-border bg-neutral-surface shadow-lg max-h-[90vh] flex flex-col">
        <div className="flex items-center justify-between px-lg py-md border-b border-neutral-border">
          <div className="flex items-center gap-2">
            <CalendarClock className="h-5 w-5 text-brand-primary" aria-hidden="true" />
            <h2 id="detalhe-agendamento-titulo" className="text-lg font-semibold text-neutral-text-primary">
              Detalhes do Agendamento
            </h2>
          </div>
          <button
            type="button"
            onClick={onFechar}
            disabled={concluindo}
            className="rounded-md p-1 text-neutral-text-muted hover:text-neutral-text-primary"
            aria-label="Fechar detalhes do agendamento"
          >
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </div>

        <div className="p-lg overflow-y-auto space-y-md flex-1">
          {carregando && (
            <div className="space-y-3" aria-label="Carregando detalhes do agendamento">
              <div className="h-6 w-1/3 animate-pulse rounded bg-neutral-border" />
              <div className="h-20 w-full animate-pulse rounded bg-neutral-background" />
              <div className="h-24 w-full animate-pulse rounded bg-neutral-background" />
            </div>
          )}

          {!carregando && erro && (
            <div
              role="alert"
              className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 rounded-md border border-status-danger bg-status-danger-bg px-md py-sm text-sm text-status-danger"
            >
              <div className="flex items-center gap-2">
                <AlertCircle className="h-4 w-4 shrink-0" aria-hidden="true" />
                <span>{erro}</span>
              </div>
              <button
                type="button"
                onClick={() => agendamentoId !== null && void carregar(agendamentoId)}
                className="inline-flex items-center gap-1.5 rounded-md border border-status-danger bg-neutral-surface px-3 py-1.5 text-xs font-semibold text-status-danger hover:bg-neutral-background"
              >
                <RefreshCw className="h-3.5 w-3.5" aria-hidden="true" />
                Tentar novamente
              </button>
            </div>
          )}

          {!carregando && detalhe && (
            <div className="space-y-lg">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div className="space-y-1">
                  <p className="text-base font-semibold text-neutral-text-primary">
                    {ROTULO_TIPO_AGENDAMENTO[detalhe.tipoAgendamento]}
                  </p>
                  <p className="text-sm text-neutral-text-secondary">
                    {formatarIntervaloHorarioSp(detalhe.dataInicio, detalhe.dataFim)} (America/Sao_Paulo)
                  </p>
                </div>
                <span
                  className={`inline-flex items-center rounded-full border px-3 py-1 text-xs font-semibold ${
                    CLASSE_BADGE_STATUS[detalhe.status]
                  }`}
                >
                  {ROTULO_STATUS_AGENDAMENTO[detalhe.status]}
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-md rounded-md border border-neutral-border bg-neutral-background p-md text-sm">
                <div>
                  <p className="text-xs font-medium text-neutral-text-secondary">Profissional Responsável</p>
                  <p className="font-semibold text-neutral-text-primary">{detalhe.nomeProfissional}</p>
                  <p className="text-xs text-neutral-text-secondary">{detalhe.cargoProfissional}</p>
                </div>
                <div>
                  <p className="text-xs font-medium text-neutral-text-secondary">Presença do Profissional</p>
                  <span
                    className={`mt-1 inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
                      detalhe.possuiPresencaFuncionario
                        ? 'bg-status-success-bg text-status-success'
                        : 'bg-neutral-surface border border-neutral-border text-neutral-text-secondary'
                    }`}
                  >
                    {detalhe.possuiPresencaFuncionario ? 'Presente' : 'Não confirmada'}
                  </span>
                </div>
                <div>
                  <p className="text-xs font-medium text-neutral-text-secondary">Início</p>
                  <p className="text-neutral-text-primary">{formatarDataHoraSp(detalhe.dataInicio)}</p>
                </div>
                <div>
                  <p className="text-xs font-medium text-neutral-text-secondary">Término</p>
                  <p className="text-neutral-text-primary">{formatarDataHoraSp(detalhe.dataFim)}</p>
                </div>
              </div>

              <div className="space-y-2">
                <h3 className="text-sm font-semibold text-neutral-text-primary">
                  {nivelAcesso === 'PACIENTE'
                    ? 'Minha Presença'
                    : `Pacientes Participantes (${detalhe.pacientes.length})`}
                </h3>
                <ul className="divide-y divide-neutral-border rounded-md border border-neutral-border bg-neutral-surface">
                  {detalhe.pacientes.map((pac) => (
                    <li key={pac.pacienteId} className="flex items-center justify-between px-md py-sm text-sm">
                      <span className="font-medium text-neutral-text-primary">{pac.nomePaciente}</span>
                      <span
                        className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
                          pac.possuiPresenca
                            ? 'bg-status-success-bg text-status-success'
                            : 'bg-neutral-background text-neutral-text-secondary'
                        }`}
                      >
                        {pac.possuiPresenca ? 'Presente' : 'Não confirmada'}
                      </span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          )}
        </div>

        <div className="flex flex-wrap items-center justify-between gap-sm px-lg py-md border-t border-neutral-border">
          <div className="flex flex-wrap items-center gap-sm">
            {podeGerenciar && detalhe && detalhe.status === 'AGENDADO' && (
              <button
                type="button"
                onClick={() => onEditar(detalhe)}
                className="inline-flex items-center gap-1.5 rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background"
              >
                <Edit3 className="h-4 w-4" aria-hidden="true" />
                Editar
              </button>
            )}
            {podeGerenciar && detalhe && detalhe.status !== 'CANCELADO' && (
              <button
                type="button"
                disabled={!iniciada}
                title={!iniciada ? 'Disponível apenas a partir do início da sessão' : undefined}
                onClick={() => onRegistrarPresenca(detalhe)}
                className="inline-flex items-center gap-1.5 rounded-md border border-brand-secondary bg-neutral-surface px-3 py-2 text-sm font-medium text-brand-secondary hover:bg-status-info-bg disabled:opacity-45 disabled:cursor-not-allowed"
              >
                <CheckSquare className="h-4 w-4" aria-hidden="true" />
                Registrar Presença
              </button>
            )}
            {podeGerenciar && detalhe && detalhe.status === 'AGENDADO' && (
              <button
                type="button"
                disabled={!iniciada || concluindo}
                title={!iniciada ? 'Disponível apenas a partir do início da sessão' : undefined}
                onClick={handleConcluirClick}
                className="inline-flex items-center gap-1.5 rounded-md bg-status-success px-3 py-2 text-sm font-semibold text-neutral-surface hover:opacity-90 disabled:opacity-45 disabled:cursor-not-allowed"
              >
                <CheckCircle2 className="h-4 w-4" aria-hidden="true" />
                {concluindo ? 'Concluindo...' : 'Concluir'}
              </button>
            )}
            {podeGerenciar && detalhe && detalhe.status === 'AGENDADO' && (
              <button
                type="button"
                onClick={() => onSolicitarCancelamento(detalhe)}
                className="inline-flex items-center gap-1.5 rounded-md border border-status-danger bg-neutral-surface px-3 py-2 text-sm font-medium text-status-danger hover:bg-status-danger-bg"
              >
                <Ban className="h-4 w-4" aria-hidden="true" />
                Cancelar Agendamento
              </button>
            )}
          </div>

          <button
            type="button"
            onClick={onFechar}
            disabled={concluindo}
            className="rounded-md border border-neutral-border bg-neutral-surface px-4 py-2 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background"
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  );
}

