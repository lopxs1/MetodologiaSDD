import type { FormEvent } from 'react';
import type { ProfissionalAgendaOpcao } from '@/types/agendamento';

interface DashboardFiltersProps {
  dataInicio: string;
  dataFim: string;
  funcionarioId: number | '';
  profissionais: ProfissionalAgendaOpcao[];
  carregando: boolean;
  onChangeDataInicio: (valor: string) => void;
  onChangeDataFim: (valor: string) => void;
  onChangeFuncionarioId: (valor: number | '') => void;
  onAplicar: () => void;
  onUltimos30Dias: () => void;
  onLimpar: () => void;
}

export function DashboardFilters({
  dataInicio,
  dataFim,
  funcionarioId,
  profissionais,
  carregando,
  onChangeDataInicio,
  onChangeDataFim,
  onChangeFuncionarioId,
  onAplicar,
  onUltimos30Dias,
  onLimpar,
}: DashboardFiltersProps) {
  const periodoInvertido = Boolean(dataInicio && dataFim && dataInicio > dataFim);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!periodoInvertido) {
      onAplicar();
    }
  };

  return (
    <section
      aria-label="Filtros do dashboard"
      className="rounded-lg border border-neutral-border bg-neutral-surface p-md shadow-sm"
    >
      <form onSubmit={handleSubmit} className="space-y-md" noValidate>
        <div className="grid grid-cols-1 gap-md md:grid-cols-3">
          <div className="space-y-xs">
            <label
              htmlFor="dashboard-data-inicio"
              className="block text-sm font-medium text-neutral-text-primary"
            >
              Data inicial
            </label>
            <input
              id="dashboard-data-inicio"
              type="date"
              value={dataInicio}
              onChange={(e) => onChangeDataInicio(e.target.value)}
              aria-invalid={periodoInvertido}
              className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary focus:outline-none focus:ring-2 focus:ring-brand-secondary"
            />
          </div>

          <div className="space-y-xs">
            <label
              htmlFor="dashboard-data-fim"
              className="block text-sm font-medium text-neutral-text-primary"
            >
              Data final
            </label>
            <input
              id="dashboard-data-fim"
              type="date"
              value={dataFim}
              onChange={(e) => onChangeDataFim(e.target.value)}
              aria-invalid={periodoInvertido}
              className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary focus:outline-none focus:ring-2 focus:ring-brand-secondary"
            />
          </div>

          <div className="space-y-xs">
            <label
              htmlFor="dashboard-funcionario"
              className="block text-sm font-medium text-neutral-text-primary"
            >
              Profissional
            </label>
            <select
              id="dashboard-funcionario"
              value={funcionarioId === '' ? '' : String(funcionarioId)}
              onChange={(e) => {
                const valor = e.target.value;
                onChangeFuncionarioId(valor === '' ? '' : Number(valor));
              }}
              className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary focus:outline-none focus:ring-2 focus:ring-brand-secondary"
            >
              <option value="">Todos os profissionais</option>
              {profissionais.map((prof) => (
                <option key={prof.funcionarioId} value={prof.funcionarioId}>
                  {prof.nome} ({prof.cargo})
                </option>
              ))}
            </select>
          </div>
        </div>

        {periodoInvertido && (
          <p role="alert" className="text-sm font-medium text-status-danger">
            A data inicial não pode ser posterior à data final
          </p>
        )}

        <div className="flex flex-wrap items-center gap-sm">
          <button
            type="submit"
            disabled={periodoInvertido || carregando}
            className="inline-flex items-center justify-center rounded-md bg-brand-primary px-4 py-2 text-sm font-medium text-neutral-surface transition-colors hover:bg-brand-primary-hover focus:outline-none focus:ring-2 focus:ring-brand-secondary disabled:cursor-not-allowed disabled:opacity-50"
          >
            Aplicar filtros
          </button>

          <button
            type="button"
            onClick={onUltimos30Dias}
            disabled={carregando}
            className="inline-flex items-center justify-center rounded-md border border-neutral-border bg-neutral-surface px-4 py-2 text-sm font-medium text-neutral-text-primary transition-colors hover:bg-neutral-background focus:outline-none focus:ring-2 focus:ring-brand-secondary disabled:cursor-not-allowed disabled:opacity-50"
          >
            Últimos 30 dias
          </button>

          <button
            type="button"
            onClick={onLimpar}
            disabled={carregando}
            className="inline-flex items-center justify-center rounded-md border border-neutral-border bg-neutral-surface px-4 py-2 text-sm font-medium text-neutral-text-secondary transition-colors hover:bg-neutral-background hover:text-neutral-text-primary focus:outline-none focus:ring-2 focus:ring-brand-secondary disabled:cursor-not-allowed disabled:opacity-50"
          >
            Limpar filtros
          </button>
        </div>
      </form>
    </section>
  );
}

