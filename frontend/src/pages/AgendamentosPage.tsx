import { useCallback, useEffect, useState } from 'react';
import {
  AlertCircle,
  ArrowDown,
  ArrowUp,
  ArrowUpDown,
  Ban,
  CalendarDays,
  CalendarPlus,
  CheckCircle2,
  CheckSquare,
  Edit3,
  Eye,
  RefreshCw,
  Search,
} from 'lucide-react';
import { AgendamentoDetalheModal } from '@/components/agendamentos/AgendamentoDetalheModal';
import { AgendamentoFormModal } from '@/components/agendamentos/AgendamentoFormModal';
import { ConfirmarCancelamentoDialog } from '@/components/agendamentos/ConfirmarCancelamentoDialog';
import { PresencaModal } from '@/components/agendamentos/PresencaModal';
import {
  CLASSE_BADGE_STATUS,
  formatarIntervaloHorarioSp,
  inputDateTimeSpParaIso,
  ROTULO_STATUS_AGENDAMENTO,
  ROTULO_TIPO_AGENDAMENTO,
  sessaoJaIniciou,
  STATUS_AGENDAMENTO_OPCOES,
  TIPOS_AGENDAMENTO,
} from '@/components/agendamentos/agendamentoUtils';
import { useAuth } from '@/contexts/AuthContext';
import { extrairErroApi } from '@/services/api';
import { agendamentoService } from '@/services/agendamentoService';
import { pacienteService } from '@/services/pacienteService';
import type {
  AgendamentoItem,
  ListaAgendamentosResponse,
  OrdenacaoAgendamentoCampo,
  ProfissionalAgendaOpcao,
  StatusAgendamento,
  TipoAgendamento,
} from '@/types/agendamento';
import type { PacienteResumoItem } from '@/types/paciente';
import type { DirecaoOrdenacao } from '@/types/usuario';

export function AgendamentosPage() {
  const { usuario } = useAuth();
  const podeGerenciar = usuario?.nivelAcesso === 'ADMINISTRADOR' || usuario?.nivelAcesso === 'MEDICO';
  const ehAdmin = usuario?.nivelAcesso === 'ADMINISTRADOR';
  const ehPaciente = usuario?.nivelAcesso === 'PACIENTE';

  const [termoInput, setTermoInput] = useState('');
  const [termoAplicado, setTermoAplicado] = useState('');
  const [filtroInicio, setFiltroInicio] = useState('');
  const [filtroFim, setFiltroFim] = useState('');
  const [filtroStatus, setFiltroStatus] = useState<StatusAgendamento | ''>('');
  const [filtroTipo, setFiltroTipo] = useState<TipoAgendamento | ''>('');
  const [filtroFuncionarioId, setFiltroFuncionarioId] = useState<number | ''>('');

  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState<OrdenacaoAgendamentoCampo>('dataInicio');
  const [direction, setDirection] = useState<DirecaoOrdenacao>('ASC');

  const [dados, setDados] = useState<ListaAgendamentosResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [erroAcao, setErroAcao] = useState<string | null>(null);

  const [profissionais, setProfissionais] = useState<ProfissionalAgendaOpcao[]>([]);
  const [pacientesDisponiveis, setPacientesDisponiveis] = useState<PacienteResumoItem[]>([]);
  const [carregandoCatalogos, setCarregandoCatalogos] = useState(false);

  const [modalFormAberto, setModalFormAberto] = useState(false);
  const [agendamentoEditando, setAgendamentoEditando] = useState<AgendamentoItem | null>(null);
  const [detalheId, setDetalheId] = useState<number | null>(null);
  const [detalheRefreshToken, setDetalheRefreshToken] = useState(0);
  const [agendamentoPresenca, setAgendamentoPresenca] = useState<AgendamentoItem | null>(null);
  const [agendamentoCancelamento, setAgendamentoCancelamento] = useState<AgendamentoItem | null>(null);

  const periodoInvertido = Boolean(filtroInicio && filtroFim && filtroFim < filtroInicio);

  const carregarAgendamentos = useCallback(async () => {
    if (periodoInvertido) {
      setErro('A data final do período deve ser posterior ou igual à inicial.');
      setCarregando(false);
      return;
    }

    setCarregando(true);
    setErro(null);
    try {
      const resposta = await agendamentoService.listarAgendamentos({
        termo: termoAplicado,
        dataInicio: filtroInicio ? inputDateTimeSpParaIso(filtroInicio) : undefined,
        dataFim: filtroFim ? inputDateTimeSpParaIso(filtroFim) : undefined,
        status: filtroStatus,
        tipoAgendamento: filtroTipo,
        funcionarioId: ehAdmin ? filtroFuncionarioId : undefined,
        page,
        size,
        sort,
        direction,
      });
      setDados(resposta);
    } catch (error) {
      setErro(extrairErroApi(error).mensagem);
    } finally {
      setCarregando(false);
    }
  }, [
    periodoInvertido,
    termoAplicado,
    filtroInicio,
    filtroFim,
    filtroStatus,
    filtroTipo,
    ehAdmin,
    filtroFuncionarioId,
    page,
    size,
    sort,
    direction,
  ]);

  useEffect(() => {
    void carregarAgendamentos();
  }, [carregarAgendamentos]);

  useEffect(() => {
    if (!podeGerenciar) {
      return;
    }
    let ativo = true;
    const carregarCatalogos = async () => {
      setCarregandoCatalogos(true);
      try {
        const [listaPacientes, listaProfs] = await Promise.all([
          pacienteService.listarPacientes({ page: 0, size: 50, sort: 'nome', direction: 'ASC' }),
          ehAdmin ? agendamentoService.listarProfissionaisClinicos() : Promise.resolve([]),
        ]);
        if (!ativo) {
          return;
        }
        setPacientesDisponiveis(listaPacientes.itens);
        setProfissionais(listaProfs);
      } catch {
        // Falhas pontuais nos catálogos não devem bloquear a listagem principal
      } finally {
        if (ativo) {
          setCarregandoCatalogos(false);
        }
      }
    };
    void carregarCatalogos();
    return () => {
      ativo = false;
    };
  }, [podeGerenciar, ehAdmin]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      const normalizado = termoInput.trim().slice(0, 100);
      setTermoAplicado(normalizado);
      setPage(0);
    }, 300);
    return () => window.clearTimeout(timer);
  }, [termoInput]);

  const alternarOrdenacao = (campo: OrdenacaoAgendamentoCampo) => {
    if (sort === campo) {
      setDirection((prev) => (prev === 'ASC' ? 'DESC' : 'ASC'));
    } else {
      setSort(campo);
      setDirection('ASC');
    }
    setPage(0);
  };

  const renderIconeOrdenacao = (campo: OrdenacaoAgendamentoCampo) => {
    if (sort !== campo) {
      return <ArrowUpDown className="h-3.5 w-3.5 text-neutral-text-muted" aria-hidden="true" />;
    }
    return direction === 'ASC' ? (
      <ArrowUp className="h-3.5 w-3.5 text-brand-primary" aria-hidden="true" />
    ) : (
      <ArrowDown className="h-3.5 w-3.5 text-brand-primary" aria-hidden="true" />
    );
  };

  const ariaSort = (campo: OrdenacaoAgendamentoCampo) => {
    if (sort !== campo) {
      return 'none';
    }
    return direction === 'ASC' ? 'ascending' : 'descending';
  };

  const limparFiltros = () => {
    setTermoInput('');
    setTermoAplicado('');
    setFiltroInicio('');
    setFiltroFim('');
    setFiltroStatus('');
    setFiltroTipo('');
    setFiltroFuncionarioId('');
    setPage(0);
  };

  const handleConcluir = async (item: AgendamentoItem) => {
    setErroAcao(null);
    await agendamentoService.concluirAgendamento(item.id);
    setDetalheRefreshToken((n) => n + 1);
    await carregarAgendamentos();
  };

  const handleConcluirDiretoTabela = async (item: AgendamentoItem) => {
    try {
      await handleConcluir(item);
    } catch (error) {
      setErroAcao(extrairErroApi(error).mensagem);
    }
  };

  const abrirCriacao = () => {
    setErroAcao(null);
    setAgendamentoEditando(null);
    setModalFormAberto(true);
  };

  const abrirEdicao = (item: AgendamentoItem) => {
    setErroAcao(null);
    setDetalheId(null);
    setAgendamentoEditando(item);
    setModalFormAberto(true);
  };

  const renderResumoParticipantes = (item: AgendamentoItem) => {
    if (ehPaciente) {
      const propria = item.pacientes[0];
      if (!propria) {
        return <span className="text-xs text-neutral-text-secondary">Presença não registrada</span>;
      }
      return (
        <span
          className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
            propria.possuiPresenca
              ? 'bg-status-success-bg text-status-success'
              : 'bg-neutral-background text-neutral-text-secondary'
          }`}
        >
          {propria.possuiPresenca ? 'Presente' : 'Presença não confirmada'}
        </span>
      );
    }

    if (item.pacientes.length === 1) {
      return <span className="text-sm text-neutral-text-primary">{item.pacientes[0].nomePaciente}</span>;
    }
    return (
      <div>
        <p className="text-sm font-medium text-neutral-text-primary">
          Grupo ({item.pacientes.length} pacientes)
        </p>
        <p className="text-xs text-neutral-text-secondary truncate max-w-xs">
          {item.pacientes.map((p) => p.nomePaciente).join(', ')}
        </p>
      </div>
    );
  };

  const possuiFiltrosAtivos = Boolean(
    termoAplicado || filtroInicio || filtroFim || filtroStatus || filtroTipo || filtroFuncionarioId !== ''
  );

  return (
    <div className="space-y-lg">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-md">
        <div>
          <h1 className="text-2xl font-bold text-neutral-text-primary">Agendamentos e Consultas</h1>
          <p className="text-sm text-neutral-text-secondary">
            {ehPaciente
              ? 'Acompanhe suas sessões agendadas e presenças confirmadas (horário de Brasília)'
              : 'Gerencie atendimentos individuais e em grupo com controle de horário e presença (America/Sao_Paulo)'}
          </p>
        </div>

        {podeGerenciar && (
          <button
            type="button"
            onClick={abrirCriacao}
            className="inline-flex items-center justify-center gap-2 rounded-md bg-brand-primary px-4 py-2 text-sm font-semibold text-neutral-surface hover:bg-brand-primary-hover focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary"
          >
            <CalendarPlus className="h-4 w-4" aria-hidden="true" />
            Novo Agendamento
          </button>
        )}
      </div>

      {erroAcao && (
        <div
          role="alert"
          className="flex items-center justify-between gap-3 rounded-md border border-status-danger bg-status-danger-bg px-md py-sm text-sm text-status-danger"
        >
          <div className="flex items-center gap-2">
            <AlertCircle className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span>{erroAcao}</span>
          </div>
          <button
            type="button"
            onClick={() => setErroAcao(null)}
            className="text-xs font-semibold underline"
          >
            Fechar
          </button>
        </div>
      )}

      <div className="rounded-lg border border-neutral-border bg-neutral-surface p-md shadow-sm space-y-md">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-6 gap-sm">
          <div className="relative lg:col-span-2">
            <label htmlFor="busca-agendamentos" className="sr-only">
              Buscar por profissional ou paciente
            </label>
            <Search
              className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-neutral-text-muted"
              aria-hidden="true"
            />
            <input
              id="busca-agendamentos"
              type="search"
              maxLength={100}
              value={termoInput}
              onChange={(e) => setTermoInput(e.target.value)}
              placeholder="Buscar profissional ou paciente..."
              className="w-full rounded-md border border-neutral-border bg-neutral-surface pl-9 pr-3 py-2 text-sm text-neutral-text-primary placeholder:text-neutral-text-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary"
            />
          </div>

          <div>
            <label htmlFor="filtro-agendamento-inicio" className="sr-only">
              Data inicial do período
            </label>
            <input
              id="filtro-agendamento-inicio"
              type="datetime-local"
              aria-label="Data inicial do período"
              value={filtroInicio}
              onChange={(e) => {
                setFiltroInicio(e.target.value);
                setPage(0);
              }}
              className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary"
            />
          </div>

          <div>
            <label htmlFor="filtro-agendamento-fim" className="sr-only">
              Data final do período
            </label>
            <input
              id="filtro-agendamento-fim"
              type="datetime-local"
              aria-label="Data final do período"
              value={filtroFim}
              onChange={(e) => {
                setFiltroFim(e.target.value);
                setPage(0);
              }}
              className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary"
            />
          </div>

          <div>
            <label htmlFor="filtro-agendamento-status" className="sr-only">
              Filtrar por situação
            </label>
            <select
              id="filtro-agendamento-status"
              aria-label="Filtrar por situação"
              value={filtroStatus}
              onChange={(e) => {
                setFiltroStatus(e.target.value as StatusAgendamento | '');
                setPage(0);
              }}
              className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary"
            >
              <option value="">Todas as situações</option>
              {STATUS_AGENDAMENTO_OPCOES.map((st) => (
                <option key={st} value={st}>
                  {ROTULO_STATUS_AGENDAMENTO[st]}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="filtro-agendamento-tipo" className="sr-only">
              Filtrar por tipo de sessão
            </label>
            <select
              id="filtro-agendamento-tipo"
              aria-label="Filtrar por tipo de sessão"
              value={filtroTipo}
              onChange={(e) => {
                setFiltroTipo(e.target.value as TipoAgendamento | '');
                setPage(0);
              }}
              className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary"
            >
              <option value="">Todos os tipos</option>
              {TIPOS_AGENDAMENTO.map((tp) => (
                <option key={tp} value={tp}>
                  {ROTULO_TIPO_AGENDAMENTO[tp]}
                </option>
              ))}
            </select>
          </div>
        </div>

        {(ehAdmin || possuiFiltrosAtivos) && (
          <div className="flex flex-wrap items-center justify-between gap-sm">
            {ehAdmin ? (
              <div className="w-full sm:w-72">
                <label htmlFor="filtro-agendamento-profissional" className="sr-only">
                  Filtrar por profissional
                </label>
                <select
                  id="filtro-agendamento-profissional"
                  aria-label="Filtrar por profissional"
                  value={filtroFuncionarioId}
                  onChange={(e) => {
                    const valor = e.target.value;
                    setFiltroFuncionarioId(valor ? Number(valor) : '');
                    setPage(0);
                  }}
                  className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-1.5 text-sm text-neutral-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary"
                >
                  <option value="">Todos os profissionais</option>
                  {profissionais.map((prof) => (
                    <option key={prof.funcionarioId} value={prof.funcionarioId}>
                      {prof.nome} — {prof.cargo}
                    </option>
                  ))}
                </select>
              </div>
            ) : (
              <div />
            )}

            {possuiFiltrosAtivos && (
              <button
                type="button"
                onClick={limparFiltros}
                className="text-xs font-medium text-brand-secondary hover:underline"
              >
                Limpar filtros
              </button>
            )}
          </div>
        )}

        {carregando ? (
          <div className="space-y-2 py-md" aria-label="Carregando agendamentos">
            {[1, 2, 3, 4].map((linha) => (
              <div key={linha} className="h-12 w-full animate-pulse rounded bg-neutral-background" />
            ))}
          </div>
        ) : erro ? (
          <div
            role="alert"
            className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 rounded-md border border-status-danger bg-status-danger-bg p-md text-sm text-status-danger"
          >
            <div className="flex items-center gap-2">
              <AlertCircle className="h-4 w-4 shrink-0" aria-hidden="true" />
              <span>{erro}</span>
            </div>
            <button
              type="button"
              onClick={() => void carregarAgendamentos()}
              className="inline-flex items-center gap-1.5 rounded-md border border-status-danger bg-neutral-surface px-3 py-1.5 text-xs font-semibold text-status-danger hover:bg-neutral-background"
            >
              <RefreshCw className="h-3.5 w-3.5" aria-hidden="true" />
              Tentar novamente
            </button>
          </div>
        ) : !dados || dados.itens.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-xl text-center space-y-2">
            <CalendarDays className="h-10 w-10 text-neutral-text-muted" aria-hidden="true" />
            <p className="text-sm font-semibold text-neutral-text-primary">
              Nenhum agendamento encontrado
            </p>
            <p className="text-xs text-neutral-text-secondary max-w-md">
              {possuiFiltrosAtivos
                ? 'Ajuste ou limpe os filtros aplicados para visualizar os atendimentos.'
                : podeGerenciar
                  ? 'Clique em "Novo Agendamento" para programar a primeira sessão clínica.'
                  : 'Você ainda não possui atendimentos agendados.'}
            </p>
            {possuiFiltrosAtivos && (
              <button
                type="button"
                onClick={limparFiltros}
                className="mt-2 rounded-md border border-neutral-border bg-neutral-surface px-3 py-1.5 text-xs font-medium text-neutral-text-primary hover:bg-neutral-background"
              >
                Limpar filtros
              </button>
            )}
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-neutral-border text-xs font-semibold uppercase tracking-wide text-neutral-text-secondary">
                    <th className="py-3 px-3" aria-sort={ariaSort('dataInicio')}>
                      <button
                        type="button"
                        onClick={() => alternarOrdenacao('dataInicio')}
                        className="inline-flex items-center gap-1 hover:text-neutral-text-primary"
                      >
                        Horário (SP)
                        {renderIconeOrdenacao('dataInicio')}
                      </button>
                    </th>
                    <th className="py-3 px-3" aria-sort={ariaSort('tipoAgendamento')}>
                      <button
                        type="button"
                        onClick={() => alternarOrdenacao('tipoAgendamento')}
                        className="inline-flex items-center gap-1 hover:text-neutral-text-primary"
                      >
                        Tipo
                        {renderIconeOrdenacao('tipoAgendamento')}
                      </button>
                    </th>
                    <th className="py-3 px-3">Profissional</th>
                    <th className="py-3 px-3">{ehPaciente ? 'Minha Presença' : 'Pacientes'}</th>
                    <th className="py-3 px-3" aria-sort={ariaSort('status')}>
                      <button
                        type="button"
                        onClick={() => alternarOrdenacao('status')}
                        className="inline-flex items-center gap-1 hover:text-neutral-text-primary"
                      >
                        Situação
                        {renderIconeOrdenacao('status')}
                      </button>
                    </th>
                    <th className="py-3 px-3 text-right">Ações</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-neutral-border">
                  {dados.itens.map((item) => {
                    const iniciada = sessaoJaIniciou(item.dataInicio);
                    return (
                      <tr key={item.id} className="hover:bg-neutral-background/70">
                        <td className="py-3 px-3 text-sm font-medium text-neutral-text-primary whitespace-nowrap">
                          {formatarIntervaloHorarioSp(item.dataInicio, item.dataFim)}
                        </td>
                        <td className="py-3 px-3 text-sm text-neutral-text-primary">
                          {ROTULO_TIPO_AGENDAMENTO[item.tipoAgendamento]}
                        </td>
                        <td className="py-3 px-3">
                          <p className="text-sm font-medium text-neutral-text-primary">{item.nomeProfissional}</p>
                          <p className="text-xs text-neutral-text-secondary">{item.cargoProfissional}</p>
                        </td>
                        <td className="py-3 px-3">{renderResumoParticipantes(item)}</td>
                        <td className="py-3 px-3">
                          <span
                            className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium ${
                              CLASSE_BADGE_STATUS[item.status]
                            }`}
                          >
                            {ROTULO_STATUS_AGENDAMENTO[item.status]}
                          </span>
                        </td>
                        <td className="py-3 px-3">
                          <div className="flex flex-wrap items-center justify-end gap-1.5">
                            <button
                              type="button"
                              onClick={() => setDetalheId(item.id)}
                              className="inline-flex items-center gap-1 rounded-md border border-neutral-border bg-neutral-surface px-2.5 py-1 text-xs font-medium text-brand-primary hover:bg-status-info-bg"
                            >
                              <Eye className="h-3.5 w-3.5" aria-hidden="true" />
                              Detalhes
                            </button>

                            {podeGerenciar && item.status === 'AGENDADO' && (
                              <button
                                type="button"
                                onClick={() => abrirEdicao(item)}
                                className="inline-flex items-center gap-1 rounded-md border border-neutral-border bg-neutral-surface px-2.5 py-1 text-xs font-medium text-neutral-text-primary hover:bg-neutral-background"
                              >
                                <Edit3 className="h-3.5 w-3.5" aria-hidden="true" />
                                Editar
                              </button>
                            )}

                            {podeGerenciar && item.status !== 'CANCELADO' && (
                              <button
                                type="button"
                                disabled={!iniciada}
                                title={!iniciada ? 'Disponível após o horário de início' : undefined}
                                onClick={() => {
                                  setErroAcao(null);
                                  setAgendamentoPresenca(item);
                                }}
                                className="inline-flex items-center gap-1 rounded-md border border-brand-secondary bg-neutral-surface px-2.5 py-1 text-xs font-medium text-brand-secondary hover:bg-status-info-bg disabled:opacity-40 disabled:cursor-not-allowed"
                              >
                                <CheckSquare className="h-3.5 w-3.5" aria-hidden="true" />
                                Presença
                              </button>
                            )}

                            {podeGerenciar && item.status === 'AGENDADO' && (
                              <button
                                type="button"
                                disabled={!iniciada}
                                title={!iniciada ? 'Disponível após o horário de início' : undefined}
                                onClick={() => void handleConcluirDiretoTabela(item)}
                                className="inline-flex items-center gap-1 rounded-md bg-status-success px-2.5 py-1 text-xs font-semibold text-neutral-surface hover:opacity-90 disabled:opacity-40 disabled:cursor-not-allowed"
                              >
                                <CheckCircle2 className="h-3.5 w-3.5" aria-hidden="true" />
                                Concluir
                              </button>
                            )}

                            {podeGerenciar && item.status === 'AGENDADO' && (
                              <button
                                type="button"
                                onClick={() => {
                                  setErroAcao(null);
                                  setAgendamentoCancelamento(item);
                                }}
                                className="inline-flex items-center gap-1 rounded-md border border-status-danger bg-neutral-surface px-2.5 py-1 text-xs font-medium text-status-danger hover:bg-status-danger-bg"
                              >
                                <Ban className="h-3.5 w-3.5" aria-hidden="true" />
                                Cancelar
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            <div className="flex flex-col sm:flex-row items-center justify-between gap-sm pt-sm border-t border-neutral-border text-sm">
              <div className="flex items-center gap-3">
                <p className="text-neutral-text-secondary">
                  Exibindo {dados.itens.length} de {dados.totalItens} agendamento
                  {dados.totalItens === 1 ? '' : 's'}
                </p>
                <div className="flex items-center gap-1.5">
                  <label htmlFor="tamanho-pagina-agendamentos" className="text-xs text-neutral-text-secondary">
                    Por página:
                  </label>
                  <select
                    id="tamanho-pagina-agendamentos"
                    value={size}
                    onChange={(e) => {
                      setSize(Number(e.target.value));
                      setPage(0);
                    }}
                    className="rounded border border-neutral-border bg-neutral-surface px-2 py-1 text-xs text-neutral-text-primary"
                  >
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                    <option value={50}>50</option>
                  </select>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  disabled={dados.pagina <= 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  className="rounded-md border border-neutral-border bg-neutral-surface px-3 py-1.5 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Anterior
                </button>
                <span className="text-xs font-medium text-neutral-text-secondary px-2">
                  Página {dados.pagina + 1} de {Math.max(1, dados.totalPaginas)}
                </span>
                <button
                  type="button"
                  disabled={dados.pagina + 1 >= dados.totalPaginas}
                  onClick={() => setPage((p) => p + 1)}
                  className="rounded-md border border-neutral-border bg-neutral-surface px-3 py-1.5 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Próxima
                </button>
              </div>
            </div>
          </>
        )}
      </div>

      {usuario && podeGerenciar && (
        <AgendamentoFormModal
          aberto={modalFormAberto}
          agendamentoParaEditar={agendamentoEditando}
          nivelAcesso={usuario.nivelAcesso}
          nomeUsuarioAutenticado={usuario.nome}
          profissionais={profissionais}
          pacientesDisponiveis={pacientesDisponiveis}
          carregandoOpcoes={carregandoCatalogos}
          onFechar={() => {
            setModalFormAberto(false);
            setAgendamentoEditando(null);
          }}
          onSalvo={() => {
            setModalFormAberto(false);
            setAgendamentoEditando(null);
            void carregarAgendamentos();
          }}
        />
      )}

      {usuario && (
        <AgendamentoDetalheModal
          agendamentoId={detalheId}
          nivelAcesso={usuario.nivelAcesso}
          recarregarToken={detalheRefreshToken}
          onFechar={() => setDetalheId(null)}
          onEditar={abrirEdicao}
          onRegistrarPresenca={(item) => setAgendamentoPresenca(item)}
          onConcluir={handleConcluir}
          onSolicitarCancelamento={(item) => setAgendamentoCancelamento(item)}
        />
      )}

      <PresencaModal
        agendamento={agendamentoPresenca}
        onFechar={() => setAgendamentoPresenca(null)}
        onAtualizado={() => {
          setAgendamentoPresenca(null);
          setDetalheRefreshToken((n) => n + 1);
          void carregarAgendamentos();
        }}
      />

      <ConfirmarCancelamentoDialog
        agendamento={agendamentoCancelamento}
        onFechar={() => setAgendamentoCancelamento(null)}
        onCancelado={() => {
          setAgendamentoCancelamento(null);
          setDetalheRefreshToken((n) => n + 1);
          void carregarAgendamentos();
        }}
      />
    </div>
  );
}

