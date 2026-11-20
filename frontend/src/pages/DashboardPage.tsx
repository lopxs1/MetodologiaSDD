import { useCallback, useEffect, useState } from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';
import { ConsultasEspecialidadeChart } from '@/components/dashboard/ConsultasEspecialidadeChart';
import { ConsultasTempoChart } from '@/components/dashboard/ConsultasTempoChart';
import { DashboardFilters } from '@/components/dashboard/DashboardFilters';
import { MetricCardsGrid } from '@/components/dashboard/MetricCardsGrid';
import { dashboardService } from '@/services/dashboardService';
import type { ProfissionalAgendaOpcao } from '@/types/agendamento';
import type { DashboardFiltroParams, DashboardResponse } from '@/types/dashboard';

function dataAtualSaoPaulo(diasDeslocamento = 0): string {
  const base = new Date(Date.now() + diasDeslocamento * 24 * 60 * 60 * 1000);
  return new Intl.DateTimeFormat('en-CA', {
    timeZone: 'America/Sao_Paulo',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(base);
}

export function DashboardPage() {
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState('');
  const [funcionarioId, setFuncionarioId] = useState<number | ''>('');
  const [filtrosAplicados, setFiltrosAplicados] = useState<DashboardFiltroParams>({});

  const [profissionais, setProfissionais] = useState<ProfissionalAgendaOpcao[]>([]);
  const [dados, setDados] = useState<DashboardResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(false);

  const carregarMetricas = useCallback(async (filtros: DashboardFiltroParams) => {
    setCarregando(true);
    setErro(false);
    setDados(null);
    try {
      const resposta = await dashboardService.obterMetricas(filtros);
      setDados(resposta);
    } catch {
      setErro(true);
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    void carregarMetricas(filtrosAplicados);
  }, [carregarMetricas, filtrosAplicados]);

  useEffect(() => {
    let ativo = true;
    const carregarProfissionais = async () => {
      try {
        const lista = await dashboardService.listarProfissionais();
        if (ativo) {
          setProfissionais(lista);
        }
      } catch {
        // Falha pontual ao listar profissionais nao bloqueia os indicadores principais
      }
    };
    void carregarProfissionais();
    return () => {
      ativo = false;
    };
  }, []);

  const handleAplicarFiltros = () => {
    setFiltrosAplicados({
      dataInicio: dataInicio || undefined,
      dataFim: dataFim || undefined,
      funcionarioId,
    });
  };

  const handleUltimos30Dias = () => {
    const inicio = dataAtualSaoPaulo(-29);
    const fim = dataAtualSaoPaulo(0);
    setDataInicio(inicio);
    setDataFim(fim);
    setFiltrosAplicados({
      dataInicio: inicio,
      dataFim: fim,
      funcionarioId,
    });
  };

  const handleLimparFiltros = () => {
    setDataInicio('');
    setDataFim('');
    setFuncionarioId('');
    setFiltrosAplicados({});
  };

  const handleRecarregar = () => {
    void carregarMetricas(filtrosAplicados);
  };

  return (
    <div className="space-y-lg">
      <div className="flex flex-wrap items-center justify-between gap-md">
        <div>
          <h1 className="text-2xl font-bold text-neutral-text-primary">Dashboard Gerencial</h1>
          <p className="text-sm text-neutral-text-secondary">
            Indicadores agregados de atendimentos e capacidade clínica da AMDE
          </p>
        </div>
        <button
          type="button"
          onClick={handleRecarregar}
          disabled={carregando}
          className="inline-flex items-center gap-2 rounded-md border border-neutral-border bg-neutral-surface px-4 py-2 text-sm font-medium text-neutral-text-primary transition-colors hover:bg-neutral-background focus:outline-none focus:ring-2 focus:ring-brand-secondary disabled:cursor-not-allowed disabled:opacity-50"
        >
          <RefreshCw className={`h-4 w-4 ${carregando ? 'animate-spin' : ''}`} aria-hidden="true" />
          <span>Atualizar indicadores</span>
        </button>
      </div>

      <DashboardFilters
        dataInicio={dataInicio}
        dataFim={dataFim}
        funcionarioId={funcionarioId}
        profissionais={profissionais}
        carregando={carregando}
        onChangeDataInicio={setDataInicio}
        onChangeDataFim={setDataFim}
        onChangeFuncionarioId={setFuncionarioId}
        onAplicar={handleAplicarFiltros}
        onUltimos30Dias={handleUltimos30Dias}
        onLimpar={handleLimparFiltros}
      />

      {carregando && (
        <div
          aria-label="Carregando indicadores do dashboard"
          className="space-y-lg animate-pulse"
        >
          <div className="grid grid-cols-1 gap-md md:grid-cols-2 lg:grid-cols-4">
            {[1, 2, 3, 4].map((item) => (
              <div
                key={item}
                className="h-28 rounded-lg border border-neutral-border bg-neutral-surface p-lg"
              >
                <div className="h-4 w-28 rounded bg-neutral-border" />
                <div className="mt-md h-8 w-16 rounded bg-neutral-border" />
              </div>
            ))}
          </div>

          <div className="grid grid-cols-1 gap-lg lg:grid-cols-2">
            {[1, 2].map((grafico) => (
              <div
                key={grafico}
                className="h-72 rounded-lg border border-neutral-border bg-neutral-surface p-lg"
              >
                <div className="h-5 w-48 rounded bg-neutral-border" />
                <div className="mt-lg h-48 rounded bg-neutral-background" />
              </div>
            ))}
          </div>
        </div>
      )}

      {!carregando && erro && (
        <div
          role="alert"
          className="flex flex-wrap items-center justify-between gap-md rounded-lg border border-status-danger bg-status-danger-bg p-lg text-neutral-text-primary"
        >
          <div className="flex items-start gap-sm">
            <AlertCircle className="h-5 w-5 shrink-0 text-status-danger mt-0.5" aria-hidden="true" />
            <div>
              <h2 className="text-base font-semibold text-neutral-text-primary">
                Não foi possível carregar os indicadores do dashboard
              </h2>
              <p className="mt-xs text-sm text-neutral-text-secondary">
                Verifique sua conexão e tente atualizar os dados novamente.
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={handleRecarregar}
            className="inline-flex items-center gap-2 rounded-md bg-brand-primary px-4 py-2 text-sm font-medium text-neutral-surface transition-colors hover:bg-brand-primary-hover focus:outline-none focus:ring-2 focus:ring-brand-secondary"
          >
            <RefreshCw className="h-4 w-4" aria-hidden="true" />
            <span>Recarregar</span>
          </button>
        </div>
      )}

      {!carregando && !erro && dados && (
        <div className="space-y-lg">
          <MetricCardsGrid contadores={dados.contadores} />

          <div className="grid grid-cols-1 gap-lg lg:grid-cols-2">
            <ConsultasTempoChart itens={dados.consultasAoLongoDoTempo} />
            <ConsultasEspecialidadeChart itens={dados.consultasPorEspecialidade} />
          </div>
        </div>
      )}
    </div>
  );
}

