import { PieChart } from 'lucide-react';
import type { ConsultaEspecialidadeItem } from '@/types/dashboard';

interface ConsultasEspecialidadeChartProps {
  itens: ConsultaEspecialidadeItem[];
}

const CORES_BARRAS = [
  'bg-brand-primary',
  'bg-brand-secondary',
  'bg-status-info',
  'bg-status-success',
  'bg-status-warning',
];

function formatarPercentual(valor: number): string {
  return `${new Intl.NumberFormat('pt-BR', {
    minimumFractionDigits: 1,
    maximumFractionDigits: 2,
  }).format(valor)}%`;
}

export function ConsultasEspecialidadeChart({ itens }: ConsultasEspecialidadeChartProps) {
  const maximo = itens.reduce((maior, item) => Math.max(maior, item.quantidade), 0);

  return (
    <section
      aria-labelledby="titulo-consultas-especialidade"
      className="rounded-lg border border-neutral-border bg-neutral-surface p-lg shadow-sm space-y-md"
    >
      <div className="flex items-center justify-between gap-sm">
        <div>
          <h2 id="titulo-consultas-especialidade" className="text-lg font-semibold text-neutral-text-primary">
            Consultas por Especialidade
          </h2>
          <p className="text-xs text-neutral-text-secondary">
            Distribuição por área de atuação profissional
          </p>
        </div>
        <PieChart className="h-5 w-5 text-brand-primary shrink-0" aria-hidden="true" />
      </div>

      {itens.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-md border border-dashed border-neutral-border bg-neutral-background px-md py-xl text-center">
          <PieChart className="h-8 w-8 text-neutral-text-secondary" aria-hidden="true" />
          <p className="mt-sm text-sm font-medium text-neutral-text-primary">
            Nenhuma consulta encontrada para os filtros selecionados
          </p>
          <p className="mt-xs text-xs text-neutral-text-secondary">
            Ajuste o período ou selecione outro profissional para visualizar a distribuição por especialidade.
          </p>
        </div>
      ) : (
        <div className="space-y-md">
          <ul className="max-h-64 space-y-sm overflow-y-auto rounded-md border border-neutral-border bg-neutral-background p-md">
            {itens.map((item, indice) => {
              const larguraPct = maximo > 0 ? Math.max(8, Math.round((item.quantidade / maximo) * 100)) : 0;
              const corBarra = CORES_BARRAS[indice % CORES_BARRAS.length];
              return (
                <li key={item.especialidade} className="space-y-xs">
                  <div className="flex items-center justify-between gap-sm text-sm">
                    <span
                      className="min-w-0 truncate font-medium text-neutral-text-primary"
                      title={item.especialidade}
                    >
                      {item.especialidade}
                    </span>
                    <span className="shrink-0 font-mono text-xs font-semibold text-neutral-text-primary">
                      {item.quantidade} ({formatarPercentual(item.percentual)})
                    </span>
                  </div>
                  <div className="h-3 w-full overflow-hidden rounded-full border border-neutral-border bg-neutral-surface">
                    <div
                      className={`h-full rounded-full ${corBarra} transition-all`}
                      style={{ width: `${larguraPct}%` }}
                    />
                  </div>
                </li>
              );
            })}
          </ul>

          <div className="overflow-x-auto border-t border-neutral-border pt-md">
            <table
              aria-label="Tabela de Consultas por Especialidade"
              className="min-w-full divide-y divide-neutral-border text-sm"
            >
              <caption className="sr-only">
                Dados tabulares da distribuição de consultas por especialidade
              </caption>
              <thead className="bg-neutral-background">
                <tr>
                  <th
                    scope="col"
                    className="px-3 py-2 text-left font-medium text-neutral-text-secondary"
                  >
                    Especialidade
                  </th>
                  <th
                    scope="col"
                    className="px-3 py-2 text-right font-medium text-neutral-text-secondary"
                  >
                    Quantidade
                  </th>
                  <th
                    scope="col"
                    className="px-3 py-2 text-right font-medium text-neutral-text-secondary"
                  >
                    Percentual (%)
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-border bg-neutral-surface">
                {itens.map((item) => (
                  <tr key={item.especialidade}>
                    <td className="px-3 py-2 text-neutral-text-primary">{item.especialidade}</td>
                    <td className="px-3 py-2 text-right font-mono font-semibold text-neutral-text-primary">
                      {item.quantidade}
                    </td>
                    <td className="px-3 py-2 text-right font-mono text-neutral-text-primary">
                      {formatarPercentual(item.percentual)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </section>
  );
}

