import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { AlertCircle, CalendarPlus, Clock, Search, X } from 'lucide-react';
import {
  inputDateTimeSpParaIso,
  isoParaInputDateTimeSp,
  ROTULO_TIPO_AGENDAMENTO,
  TIPOS_AGENDAMENTO,
} from '@/components/agendamentos/agendamentoUtils';
import { extrairErroApi } from '@/services/api';
import { agendamentoService } from '@/services/agendamentoService';
import type { AgendamentoItem, ProfissionalAgendaOpcao, TipoAgendamento } from '@/types/agendamento';
import type { PacienteResumoItem } from '@/types/paciente';
import type { NivelAcesso } from '@/types/usuario';

interface AgendamentoFormModalProps {
  aberto: boolean;
  agendamentoParaEditar: AgendamentoItem | null;
  nivelAcesso: NivelAcesso;
  nomeUsuarioAutenticado: string;
  profissionais: ProfissionalAgendaOpcao[];
  pacientesDisponiveis: PacienteResumoItem[];
  carregandoOpcoes: boolean;
  onFechar: () => void;
  onSalvo: () => void;
}

export function AgendamentoFormModal({
  aberto,
  agendamentoParaEditar,
  nivelAcesso,
  nomeUsuarioAutenticado,
  profissionais,
  pacientesDisponiveis,
  carregandoOpcoes,
  onFechar,
  onSalvo,
}: AgendamentoFormModalProps) {
  const modoEdicao = agendamentoParaEditar !== null;

  const [funcionarioId, setFuncionarioId] = useState<string>('');
  const [tipoAgendamento, setTipoAgendamento] = useState<TipoAgendamento>('CONSULTA');
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState('');
  const [pacientesSelecionados, setPacientesSelecionados] = useState<number[]>([]);
  const [buscaPaciente, setBuscaPaciente] = useState('');

  const [salvando, setSalvando] = useState(false);
  const [erroGeral, setErroGeral] = useState<string | null>(null);
  const [erroConflitoHorario, setErroConflitoHorario] = useState<string | null>(null);
  const [errosCampo, setErrosCampo] = useState<Record<string, string>>({});

  useEffect(() => {
    if (!aberto) {
      return;
    }
    setErroGeral(null);
    setErroConflitoHorario(null);
    setErrosCampo({});
    setBuscaPaciente('');

    if (agendamentoParaEditar) {
      setFuncionarioId(String(agendamentoParaEditar.funcionarioId));
      setTipoAgendamento(agendamentoParaEditar.tipoAgendamento);
      setDataInicio(isoParaInputDateTimeSp(agendamentoParaEditar.dataInicio));
      setDataFim(isoParaInputDateTimeSp(agendamentoParaEditar.dataFim));
      setPacientesSelecionados(agendamentoParaEditar.pacientes.map((p) => p.pacienteId));
    } else {
      setFuncionarioId(profissionais.length === 1 ? String(profissionais[0].funcionarioId) : '');
      setTipoAgendamento('CONSULTA');
      setDataInicio('');
      setDataFim('');
      setPacientesSelecionados([]);
    }
  }, [aberto, agendamentoParaEditar, profissionais]);

  useEffect(() => {
    if (!aberto) {
      return;
    }
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !salvando) {
        onFechar();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [aberto, salvando, onFechar]);

  const pacientesCombinados = useMemo(() => {
    const mapa = new Map<number, { id: number; nome: string; detalhe: string }>();
    for (const pac of pacientesDisponiveis) {
      mapa.set(pac.id, { id: pac.id, nome: pac.nome, detalhe: pac.email });
    }
    if (agendamentoParaEditar) {
      for (const pac of agendamentoParaEditar.pacientes) {
        if (!mapa.has(pac.pacienteId)) {
          mapa.set(pac.pacienteId, {
            id: pac.pacienteId,
            nome: pac.nomePaciente,
            detalhe: 'Participante vinculado',
          });
        }
      }
    }
    const lista = Array.from(mapa.values()).sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
    const termo = buscaPaciente.trim().toLowerCase();
    if (!termo) {
      return lista;
    }
    return lista.filter(
      (item) => item.nome.toLowerCase().includes(termo) || item.detalhe.toLowerCase().includes(termo)
    );
  }, [pacientesDisponiveis, agendamentoParaEditar, buscaPaciente]);

  if (!aberto) {
    return null;
  }

  const handleTogglePaciente = (id: number) => {
    setPacientesSelecionados((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const validarFormulario = (): boolean => {
    const novosErros: Record<string, string> = {};
    if (!modoEdicao && nivelAcesso === 'ADMINISTRADOR' && !funcionarioId) {
      novosErros.funcionarioId = 'Selecione o profissional responsável';
    }
    if (!dataInicio) {
      novosErros.dataInicio = 'Informe a data e horário de início';
    }
    if (!dataFim) {
      novosErros.dataFim = 'Informe a data e horário de término';
    } else if (dataInicio && dataFim <= dataInicio) {
      novosErros.dataFim = 'O término deve ser posterior ao início';
    }
    if (pacientesSelecionados.length === 0) {
      novosErros.pacientesIds = 'Selecione pelo menos um paciente participante';
    }
    setErrosCampo(novosErros);
    return Object.keys(novosErros).length === 0;
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setErroGeral(null);
    setErroConflitoHorario(null);
    if (!validarFormulario()) {
      return;
    }

    setSalvando(true);
    try {
      const inicioIso = inputDateTimeSpParaIso(dataInicio);
      const fimIso = inputDateTimeSpParaIso(dataFim);

      if (modoEdicao && agendamentoParaEditar) {
        await agendamentoService.editarAgendamento(agendamentoParaEditar.id, {
          funcionarioId: agendamentoParaEditar.funcionarioId,
          dataInicio: inicioIso,
          dataFim: fimIso,
          tipoAgendamento,
          pacientesIds: pacientesSelecionados,
        });
      } else {
        await agendamentoService.criarAgendamento({
          funcionarioId: nivelAcesso === 'ADMINISTRADOR' ? Number(funcionarioId) : null,
          dataInicio: inicioIso,
          dataFim: fimIso,
          tipoAgendamento,
          pacientesIds: pacientesSelecionados,
        });
      }
      onSalvo();
    } catch (error) {
      const parsed = extrairErroApi(error);
      if (parsed.status === 409) {
        setErroConflitoHorario(parsed.mensagem);
      } else {
        setErroGeral(parsed.mensagem);
      }
      if (Object.keys(parsed.errosCampo).length > 0) {
        setErrosCampo(parsed.errosCampo);
      }
    } finally {
      setSalvando(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-md bg-neutral-text-primary/50"
      role="dialog"
      aria-modal="true"
      aria-labelledby="agendamento-form-titulo"
      onClick={(e) => {
        if (e.target === e.currentTarget && !salvando) {
          onFechar();
        }
      }}
    >
      <div className="w-full max-w-2xl rounded-lg border border-neutral-border bg-neutral-surface shadow-lg max-h-[92vh] flex flex-col">
        <div className="flex items-center justify-between px-lg py-md border-b border-neutral-border">
          <div className="flex items-center gap-2">
            <CalendarPlus className="h-5 w-5 text-brand-primary" aria-hidden="true" />
            <h2 id="agendamento-form-titulo" className="text-lg font-semibold text-neutral-text-primary">
              {modoEdicao ? 'Editar Agendamento' : 'Novo Agendamento'}
            </h2>
          </div>
          <button
            type="button"
            onClick={onFechar}
            disabled={salvando}
            className="rounded-md p-1 text-neutral-text-muted hover:text-neutral-text-primary"
            aria-label="Fechar formulário de agendamento"
          >
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-lg overflow-y-auto space-y-md flex-1">
          {erroGeral && (
            <div
              role="alert"
              className="flex items-start gap-2 rounded-md border border-status-danger bg-status-danger-bg px-3 py-2 text-sm text-status-danger"
            >
              <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" aria-hidden="true" />
              <span>{erroGeral}</span>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-md">
            <div>
              <label
                htmlFor="agendamento-profissional"
                className="block text-sm font-medium text-neutral-text-primary mb-1"
              >
                Profissional Responsável *
              </label>
              {modoEdicao && agendamentoParaEditar ? (
                <input
                  id="agendamento-profissional"
                  type="text"
                  readOnly
                  disabled
                  value={`${agendamentoParaEditar.nomeProfissional} (${agendamentoParaEditar.cargoProfissional})`}
                  className="w-full rounded-md border border-neutral-border bg-neutral-background px-3 py-2 text-sm text-neutral-text-secondary"
                />
              ) : nivelAcesso === 'MEDICO' ? (
                <input
                  id="agendamento-profissional"
                  type="text"
                  readOnly
                  disabled
                  value={nomeUsuarioAutenticado}
                  className="w-full rounded-md border border-neutral-border bg-neutral-background px-3 py-2 text-sm text-neutral-text-secondary"
                />
              ) : (
                <select
                  id="agendamento-profissional"
                  value={funcionarioId}
                  onChange={(e) => setFuncionarioId(e.target.value)}
                  disabled={carregandoOpcoes || salvando}
                  className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary focus:border-brand-secondary focus:outline-none focus:ring-2 focus:ring-brand-secondary/30"
                >
                  <option value="">
                    {carregandoOpcoes ? 'Carregando profissionais...' : 'Selecione o profissional'}
                  </option>
                  {profissionais.map((prof) => (
                    <option key={prof.funcionarioId} value={prof.funcionarioId}>
                      {prof.nome} — {prof.cargo}
                    </option>
                  ))}
                </select>
              )}
              {errosCampo.funcionarioId && (
                <p className="mt-1 text-xs text-status-danger">{errosCampo.funcionarioId}</p>
              )}
            </div>

            <div>
              <label
                htmlFor="agendamento-tipo"
                className="block text-sm font-medium text-neutral-text-primary mb-1"
              >
                Tipo de Sessão *
              </label>
              <select
                id="agendamento-tipo"
                value={tipoAgendamento}
                onChange={(e) => setTipoAgendamento(e.target.value as TipoAgendamento)}
                disabled={salvando}
                className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary focus:border-brand-secondary focus:outline-none focus:ring-2 focus:ring-brand-secondary/30"
              >
                {TIPOS_AGENDAMENTO.map((tipo) => (
                  <option key={tipo} value={tipo}>
                    {ROTULO_TIPO_AGENDAMENTO[tipo]}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="rounded-md border border-neutral-border bg-neutral-background/50 p-md space-y-sm">
            <div className="flex items-center gap-1.5 text-xs font-medium text-neutral-text-secondary">
              <Clock className="h-3.5 w-3.5" aria-hidden="true" />
              <span>Horário da sessão no fuso America/Sao_Paulo (horário de Brasília)</span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-md">
              <div>
                <label
                  htmlFor="agendamento-inicio"
                  className="block text-sm font-medium text-neutral-text-primary mb-1"
                >
                  Início *
                </label>
                <input
                  id="agendamento-inicio"
                  type="datetime-local"
                  value={dataInicio}
                  onChange={(e) => {
                    setDataInicio(e.target.value);
                    setErroConflitoHorario(null);
                  }}
                  className={`w-full rounded-md border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary focus:outline-none focus:ring-2 ${
                    erroConflitoHorario || errosCampo.dataInicio
                      ? 'border-status-danger focus:ring-status-danger/30'
                      : 'border-neutral-border focus:border-brand-secondary focus:ring-brand-secondary/30'
                  }`}
                />
                {errosCampo.dataInicio && (
                  <p className="mt-1 text-xs text-status-danger">{errosCampo.dataInicio}</p>
                )}
              </div>

              <div>
                <label
                  htmlFor="agendamento-fim"
                  className="block text-sm font-medium text-neutral-text-primary mb-1"
                >
                  Término *
                </label>
                <input
                  id="agendamento-fim"
                  type="datetime-local"
                  value={dataFim}
                  onChange={(e) => {
                    setDataFim(e.target.value);
                    setErroConflitoHorario(null);
                  }}
                  className={`w-full rounded-md border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary focus:outline-none focus:ring-2 ${
                    erroConflitoHorario || errosCampo.dataFim
                      ? 'border-status-danger focus:ring-status-danger/30'
                      : 'border-neutral-border focus:border-brand-secondary focus:ring-brand-secondary/30'
                  }`}
                />
                {errosCampo.dataFim && (
                  <p className="mt-1 text-xs text-status-danger">{errosCampo.dataFim}</p>
                )}
              </div>
            </div>

            {erroConflitoHorario && (
              <div
                role="alert"
                className="flex items-start gap-2 rounded-md border border-status-danger bg-status-danger-bg px-3 py-2 text-xs font-medium text-status-danger"
              >
                <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" aria-hidden="true" />
                <span>Conflito de horário: {erroConflitoHorario}</span>
              </div>
            )}
          </div>

          <div className="space-y-2">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <label className="text-sm font-medium text-neutral-text-primary">
                Pacientes Participantes * ({pacientesSelecionados.length} selecionado
                {pacientesSelecionados.length === 1 ? '' : 's'})
              </label>
              <span className="text-xs text-neutral-text-secondary">
                Selecione um paciente (individual) ou mais (grupo)
              </span>
            </div>

            <div className="relative">
              <Search
                className="pointer-events-none absolute left-3 top-2.5 h-4 w-4 text-neutral-text-muted"
                aria-hidden="true"
              />
              <input
                type="search"
                value={buscaPaciente}
                onChange={(e) => setBuscaPaciente(e.target.value)}
                placeholder="Filtrar pacientes pelo nome ou e-mail..."
                aria-label="Filtrar pacientes disponíveis"
                className="w-full rounded-md border border-neutral-border bg-neutral-surface pl-9 pr-3 py-2 text-sm text-neutral-text-primary focus:border-brand-secondary focus:outline-none focus:ring-2 focus:ring-brand-secondary/30"
              />
            </div>

            <div className="max-h-48 overflow-y-auto rounded-md border border-neutral-border divide-y divide-neutral-border">
              {carregandoOpcoes && pacientesCombinados.length === 0 ? (
                <p className="p-md text-xs text-neutral-text-secondary">Carregando pacientes ativos...</p>
              ) : pacientesCombinados.length === 0 ? (
                <p className="p-md text-xs text-neutral-text-secondary">
                  Nenhum paciente encontrado para seleção.
                </p>
              ) : (
                pacientesCombinados.map((pac) => {
                  const marcado = pacientesSelecionados.includes(pac.id);
                  return (
                    <label
                      key={pac.id}
                      className="flex items-center justify-between gap-3 px-md py-2 cursor-pointer hover:bg-neutral-background"
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <input
                          type="checkbox"
                          checked={marcado}
                          onChange={() => handleTogglePaciente(pac.id)}
                          className="h-4 w-4 rounded border-neutral-border text-brand-primary focus:ring-brand-secondary"
                        />
                        <div className="min-w-0">
                          <p className="text-sm font-medium text-neutral-text-primary truncate">{pac.nome}</p>
                          <p className="text-xs text-neutral-text-secondary truncate">{pac.detalhe}</p>
                        </div>
                      </div>
                    </label>
                  );
                })
              )}
            </div>
            {errosCampo.pacientesIds && (
              <p className="text-xs text-status-danger">{errosCampo.pacientesIds}</p>
            )}
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
              {salvando ? 'Salvando...' : modoEdicao ? 'Salvar Alterações' : 'Criar Agendamento'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

