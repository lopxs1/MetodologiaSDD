import { BarChart3 } from 'lucide-react';
import type { ConsultaDiaItem } from '@/types/dashboard';

interface ConsultasTempoChartProps {
  itens: ConsultaDiaItem[];
}

function formatarDataBr(dataIso: string): string {
  const partes = dataIso.split('-');
  if (partes.length !== 3) {
    return dataIso;
  }
  return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

export function ConsultasTempoChart({ itens }: ConsultasTempoChartProps) {
  const maximo = itens.reduce((maior, item) => Math.max(maior, item.quantidade), 0);

  return (
    <section
      aria-labelledby="titulo-consultas-tempo"
      className="rounded-lg border border-neutral-border bg-neutral-surface p-lg shadow-sm space-y-md"
    >
      <div className="flex items-center justify-between gap-sm">
        <div>
          <h2 id="titulo-consultas-tempo" className="text-lg font-semibold text-neutral-text-primary">
            Consultas ao Longo do Tempo
          </h2>
          <p className="text-xs text-neutral-text-secondary">
            Evolução diária de agendamentos válidos (America/Sao_Paulo)
          </p>
        </div>
        <BarChart3 className="h-5 w-5 text-brand-primary shrink-0" aria-hidden="true" />
      </div>

      {itens.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-md border border-dashed border-neutral-border bg-neutral-background px-md py-xl text-center">
          <BarChart3 className="h-8 w-8 text-neutral-text-secondary" aria-hidden="true" />
          <p className="mt-sm text-sm font-medium text-neutral-text-primary">
            Nenhuma consulta encontrada para os filtros selecionados
          </p>
          <p className="mt-xs text-xs text-neutral-text-secondary">
            Ajuste o período ou selecione outro profissional para visualizar a série diária.
          </p>
        </div>
      ) : (
        <div className="space-y-md">
          <div className="overflow-x-auto pb-sm">
            <div className="flex min-w-max items-end gap-sm rounded-md border border-neutral-border bg-neutral-background p-md">
              {itens.map((item) => {
                const alturaPct = maximo > 0 ? Math.max(12, Math.round((item.quantidade / maximo) * 100)) : 0;
                return (
                  <div
                    key={item.data}
                    className="flex w-14 flex-col items-center gap-xs"
                    title={`${formatarDataBr(item.data)}: ${item.quantidade} consulta(s)`}
                  >
                    <span className="font-mono text-xs font-semibold text-neutral-text-primary">
                      {item.quantidade}
                    </span>
                    <div className="flex h-36 w-8 items-end rounded-sm border border-neutral-border bg-neutral-surface p-0.5">
                      <div
                        className="w-full rounded-sm bg-brand-primary transition-all"
                        style={{ height: `${alturaPct}%` }}
                      />
                    </div>
                    <span className="font-mono text-xs text-neutral-text-secondary">
                      {formatarDataBr(item.data).slice(0, 5)}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>

          <div className="overflow-x-auto border-t border-neutral-border pt-md">
            <table
              aria-label="Tabela de Consultas ao Longo do Tempo"
              className="min-w-full divide-y divide-neutral-border text-sm"
            >
              <caption className="sr-only">
                Dados tabulares da evolução diária de consultas ao longo do tempo
              </caption>
              <thead className="bg-neutral-background">
                <tr>
                  <th
                    scope="col"
                    className="px-3 py-2 text-left font-medium text-neutral-text-secondary"
                  >
                    Data
                  </th>
                  <th
                    scope="col"
                    className="px-3 py-2 text-right font-medium text-neutral-text-secondary"
                  >
                    Quantidade
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-border bg-neutral-surface">
                {itens.map((item) => (
                  <tr key={item.data}>
                    <td className="px-3 py-2 font-mono text-neutral-text-primary">
                      {formatarDataBr(item.data)}
                    </td>
                    <td className="px-3 py-2 text-right font-mono font-semibold text-neutral-text-primary">
                      {item.quantidade}
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

