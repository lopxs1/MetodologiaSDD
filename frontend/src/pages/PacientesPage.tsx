import { useCallback, useEffect, useState } from 'react';
import {
  AlertCircle,
  ArrowDown,
  ArrowUp,
  ArrowUpDown,
  Eye,
  RefreshCw,
  Search,
  UserCheck,
} from 'lucide-react';
import { PacienteFichaModal } from '@/components/pacientes/PacienteFichaModal';
import { extrairErroApi } from '@/services/api';
import { pacienteService } from '@/services/pacienteService';
import type { ListaPacientesResponse, OrdenacaoPacienteCampo } from '@/types/paciente';
import type { DirecaoOrdenacao, GrauSuporte } from '@/types/usuario';

const BADGE_GRAU_SUPORTE: Record<GrauSuporte, { rotulo: string; classe: string }> = {
  NIVEL_1: {
    rotulo: 'Nível 1',
    classe: 'border-status-success bg-status-success-bg text-status-success',
  },
  NIVEL_2: {
    rotulo: 'Nível 2',
    classe: 'border-status-warning bg-status-warning-bg text-status-warning',
  },
  NIVEL_3: {
    rotulo: 'Nível 3',
    classe: 'border-status-danger bg-status-danger-bg text-status-danger',
  },
};

export function PacientesPage() {
  const [termoInput, setTermoInput] = useState('');
  const [termoAplicado, setTermoAplicado] = useState('');
  const [filtroGrau, setFiltroGrau] = useState<GrauSuporte | ''>('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState<OrdenacaoPacienteCampo>('nome');
  const [direction, setDirection] = useState<DirecaoOrdenacao>('ASC');

  const [dados, setDados] = useState<ListaPacientesResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [pacienteSelecionadoId, setPacienteSelecionadoId] = useState<number | null>(null);

  const carregarPacientes = useCallback(async () => {
    setCarregando(true);
    setErro(null);
    try {
      const resposta = await pacienteService.listarPacientes({
        termo: termoAplicado,
        grauSuporte: filtroGrau,
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
  }, [termoAplicado, filtroGrau, page, size, sort, direction]);

  useEffect(() => {
    void carregarPacientes();
  }, [carregarPacientes]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      const normalizado = termoInput.trim().slice(0, 100);
      setTermoAplicado(normalizado);
      setPage(0);
    }, 300);
    return () => window.clearTimeout(timer);
  }, [termoInput]);

  const alternarOrdenacao = (campo: OrdenacaoPacienteCampo) => {
    if (sort === campo) {
      setDirection((prev) => (prev === 'ASC' ? 'DESC' : 'ASC'));
    } else {
      setSort(campo);
      setDirection('ASC');
    }
    setPage(0);
  };

  const renderIconeOrdenacao = (campo: OrdenacaoPacienteCampo) => {
    if (sort !== campo) {
      return <ArrowUpDown className="h-3.5 w-3.5 text-neutral-text-muted" aria-hidden="true" />;
    }
    return direction === 'ASC' ? (
      <ArrowUp className="h-3.5 w-3.5 text-brand-primary" aria-hidden="true" />
    ) : (
      <ArrowDown className="h-3.5 w-3.5 text-brand-primary" aria-hidden="true" />
    );
  };

  const ariaSort = (campo: OrdenacaoPacienteCampo) => {
    if (sort !== campo) {
      return 'none';
    }
    return direction === 'ASC' ? 'ascending' : 'descending';
  };

  return (
    <div className="space-y-lg">
      <div>
        <h1 className="text-2xl font-bold text-neutral-text-primary">Pacientes e Prontuários</h1>
        <p className="text-sm text-neutral-text-secondary">
          Consulte pacientes ativos, atualize o grau de suporte TEA e acompanhe o histórico documental
        </p>
      </div>

      <div className="rounded-lg border border-neutral-border bg-neutral-surface p-md shadow-sm space-y-md">
        <div className="flex flex-col md:flex-row gap-md">
          <div className="relative flex-1">
            <label htmlFor="busca-pacientes" className="sr-only">
              Buscar por nome ou e-mail
            </label>
            <Search
              className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-neutral-text-muted"
              aria-hidden="true"
            />
            <input
              id="busca-pacientes"
              type="search"
              maxLength={100}
              value={termoInput}
              onChange={(e) => setTermoInput(e.target.value)}
              placeholder="Buscar por nome ou e-mail..."
              className="w-full rounded-md border border-neutral-border bg-neutral-surface pl-9 pr-3 py-2 text-sm text-neutral-text-primary placeholder:text-neutral-text-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary"
            />
          </div>

          <div className="w-full md:w-64">
            <label htmlFor="filtro-grau-suporte" className="sr-only">
              Filtrar por grau de suporte
            </label>
            <select
              id="filtro-grau-suporte"
              value={filtroGrau}
              onChange={(e) => {
                setFiltroGrau(e.target.value as GrauSuporte | '');
                setPage(0);
              }}
              className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary"
            >
              <option value="">Todos os graus de suporte</option>
              <option value="NIVEL_1">Nível 1</option>
              <option value="NIVEL_2">Nível 2</option>
              <option value="NIVEL_3">Nível 3</option>
            </select>
          </div>
        </div>

        {carregando ? (
          <div className="space-y-sm py-sm" aria-busy="true" aria-label="Carregando pacientes">
            {[1, 2, 3, 4, 5].map((linha) => (
              <div key={linha} className="h-12 rounded-md bg-neutral-background animate-pulse" />
            ))}
          </div>
        ) : erro ? (
          <div
            role="alert"
            className="rounded-md border border-status-danger bg-status-danger-bg p-lg text-center space-y-sm"
          >
            <div className="mx-auto flex h-10 w-10 items-center justify-center rounded-full bg-neutral-surface text-status-danger">
              <AlertCircle className="h-5 w-5" aria-hidden="true" />
            </div>
            <p className="text-sm font-medium text-status-danger">{erro}</p>
            <button
              type="button"
              onClick={() => void carregarPacientes()}
              className="inline-flex items-center gap-2 rounded-md bg-brand-primary px-4 py-2 text-sm font-medium text-neutral-surface hover:bg-brand-primary-hover transition-colors"
            >
              <RefreshCw className="h-4 w-4" aria-hidden="true" />
              Recarregar
            </button>
          </div>
        ) : !dados || dados.itens.length === 0 ? (
          <div className="py-xl text-center space-y-sm">
            <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-neutral-background text-neutral-text-muted">
              <UserCheck className="h-6 w-6" aria-hidden="true" />
            </div>
            <h2 className="text-base font-semibold text-neutral-text-primary">Nenhum paciente encontrado</h2>
            <p className="text-sm text-neutral-text-secondary">
              Não há pacientes ativos correspondentes aos critérios de busca e filtro informados.
            </p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-sm">
                <thead>
                  <tr className="border-b border-neutral-border text-xs font-semibold uppercase tracking-wide text-neutral-text-secondary">
                    <th className="py-3 px-3" aria-sort={ariaSort('nome')}>
                      <button
                        type="button"
                        onClick={() => alternarOrdenacao('nome')}
                        className="inline-flex items-center gap-1 hover:text-neutral-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary rounded"
                      >
                        Nome {renderIconeOrdenacao('nome')}
                      </button>
                    </th>
                    <th className="py-3 px-3" aria-sort={ariaSort('email')}>
                      <button
                        type="button"
                        onClick={() => alternarOrdenacao('email')}
                        className="inline-flex items-center gap-1 hover:text-neutral-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary rounded"
                      >
                        E-mail {renderIconeOrdenacao('email')}
                      </button>
                    </th>
                    <th className="py-3 px-3" aria-sort={ariaSort('grauSuporte')}>
                      <button
                        type="button"
                        onClick={() => alternarOrdenacao('grauSuporte')}
                        className="inline-flex items-center gap-1 hover:text-neutral-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary rounded"
                      >
                        Grau de Suporte {renderIconeOrdenacao('grauSuporte')}
                      </button>
                    </th>
                    <th className="py-3 px-3">Celular</th>
                    <th className="py-3 px-3 text-right">Ações</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-neutral-border">
                  {dados.itens.map((item) => {
                    const badge = BADGE_GRAU_SUPORTE[item.grauSuporte];
                    return (
                      <tr key={item.id} className="hover:bg-neutral-background/70">
                        <td className="py-3 px-3 font-medium text-neutral-text-primary">{item.nome}</td>
                        <td className="py-3 px-3 text-neutral-text-secondary">{item.email}</td>
                        <td className="py-3 px-3">
                          <span
                            className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium ${badge.classe}`}
                          >
                            {badge.rotulo}
                          </span>
                        </td>
                        <td className="py-3 px-3 font-mono text-xs text-neutral-text-secondary">
                          {item.numCelular || 'Não informado'}
                        </td>
                        <td className="py-3 px-3 text-right">
                          <button
                            type="button"
                            onClick={() => setPacienteSelecionadoId(item.id)}
                            className="inline-flex items-center gap-1.5 rounded-md border border-neutral-border bg-neutral-surface px-2.5 py-1 text-xs font-medium text-brand-primary hover:bg-status-info-bg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary"
                            aria-label={`Abrir prontuário de ${item.nome}`}
                          >
                            <Eye className="h-3.5 w-3.5" aria-hidden="true" />
                            Abrir Prontuário
                          </button>
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
                  Exibindo {dados.itens.length} de {dados.totalItens} pacientes ativos
                </p>
                <div className="flex items-center gap-1.5">
                  <label htmlFor="tamanho-pagina-pacientes" className="text-xs text-neutral-text-secondary">
                    Por página:
                  </label>
                  <select
                    id="tamanho-pagina-pacientes"
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

      <PacienteFichaModal
        pacienteId={pacienteSelecionadoId}
        onFechar={() => setPacienteSelecionadoId(null)}
        onAtualizado={() => void carregarPacientes()}
      />
    </div>
  );
}

