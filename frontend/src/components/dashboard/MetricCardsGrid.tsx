import { CalendarCheck2, Clock, Stethoscope, UserCheck } from 'lucide-react';
import type { DashboardContadores } from '@/types/dashboard';

interface MetricCardsGridProps {
  contadores: DashboardContadores;
}

export function MetricCardsGrid({ contadores }: MetricCardsGridProps) {
  const cards = [
    {
      titulo: 'Total de Consultas',
      valor: contadores.totalConsultas,
      subtitulo: 'Consultas válidas no recorte',
      icon: CalendarCheck2,
      iconeClasse: 'bg-status-info-bg text-status-info',
    },
    {
      titulo: 'Consultas Hoje',
      valor: contadores.consultasHoje,
      subtitulo: 'Fuso America/Sao_Paulo',
      icon: Clock,
      iconeClasse: 'bg-status-warning-bg text-status-warning',
    },
    {
      titulo: 'Total de Pacientes',
      valor: contadores.totalPacientes,
      subtitulo: 'Pacientes ativos',
      icon: UserCheck,
      iconeClasse: 'bg-status-success-bg text-status-success',
    },
    {
      titulo: 'Profissionais Ativos',
      valor: contadores.profissionaisAtivos,
      subtitulo: 'Equipe ativa',
      icon: Stethoscope,
      iconeClasse: 'bg-neutral-background text-brand-primary',
    },
  ];

  return (
    <section aria-label="Indicadores principais" className="grid grid-cols-1 gap-md md:grid-cols-2 lg:grid-cols-4">
      {cards.map((card) => {
        const Icon = card.icon;
        return (
          <article
            key={card.titulo}
            className="rounded-lg border border-neutral-border bg-neutral-surface p-lg shadow-sm"
          >
            <div className="flex items-start justify-between gap-sm">
              <div>
                <h2 className="text-sm font-medium text-neutral-text-secondary">{card.titulo}</h2>
                <p className="mt-sm text-3xl font-bold text-neutral-text-primary">{card.valor}</p>
                <p className="mt-xs text-xs text-neutral-text-secondary">{card.subtitulo}</p>
              </div>
              <span className={`inline-flex rounded-md p-sm ${card.iconeClasse}`} aria-hidden="true">
                <Icon className="h-5 w-5" />
              </span>
            </div>
          </article>
        );
      })}
    </section>
  );
}

