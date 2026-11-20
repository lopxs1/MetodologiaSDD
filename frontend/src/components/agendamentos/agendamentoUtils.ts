import type { StatusAgendamento, TipoAgendamento } from '@/types/agendamento';

export const FUSO_CLINICA = 'America/Sao_Paulo';

export const ROTULO_TIPO_AGENDAMENTO: Record<TipoAgendamento, string> = {
  CONSULTA: 'Consulta',
  EXAME: 'Exame',
  RETORNO: 'Retorno',
  AVALIACAO: 'Avaliação',
  TERAPIA: 'Terapia',
};

export const ROTULO_STATUS_AGENDAMENTO: Record<StatusAgendamento, string> = {
  AGENDADO: 'Agendado',
  CONCLUIDO: 'Concluído',
  CANCELADO: 'Cancelado',
};

export const CLASSE_BADGE_STATUS: Record<StatusAgendamento, string> = {
  AGENDADO: 'bg-status-info-bg text-status-info border-status-info/30',
  CONCLUIDO: 'bg-status-success-bg text-status-success border-status-success/30',
  CANCELADO: 'bg-status-danger-bg text-status-danger border-status-danger/30',
};

export const TIPOS_AGENDAMENTO: TipoAgendamento[] = [
  'CONSULTA',
  'EXAME',
  'RETORNO',
  'AVALIACAO',
  'TERAPIA',
];

export const STATUS_AGENDAMENTO_OPCOES: StatusAgendamento[] = [
  'AGENDADO',
  'CONCLUIDO',
  'CANCELADO',
];

export function formatarDataHoraSp(iso: string): string {
  const data = new Date(iso);
  if (Number.isNaN(data.getTime())) {
    return iso;
  }
  return new Intl.DateTimeFormat('pt-BR', {
    timeZone: FUSO_CLINICA,
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(data);
}

export function formatarIntervaloHorarioSp(inicioIso: string, fimIso: string): string {
  const inicio = new Date(inicioIso);
  const fim = new Date(fimIso);
  if (Number.isNaN(inicio.getTime()) || Number.isNaN(fim.getTime())) {
    return `${inicioIso} - ${fimIso}`;
  }
  const diaStr = new Intl.DateTimeFormat('pt-BR', {
    timeZone: FUSO_CLINICA,
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(inicio);
  const horaFmt = new Intl.DateTimeFormat('pt-BR', {
    timeZone: FUSO_CLINICA,
    hour: '2-digit',
    minute: '2-digit',
  });
  return `${diaStr}, ${horaFmt.format(inicio)} – ${horaFmt.format(fim)}`;
}

export function isoParaInputDateTimeSp(iso: string): string {
  const data = new Date(iso);
  if (Number.isNaN(data.getTime())) {
    return '';
  }
  const partes = new Intl.DateTimeFormat('en-CA', {
    timeZone: FUSO_CLINICA,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).formatToParts(data);

  const obter = (tipo: Intl.DateTimeFormatPartTypes) =>
    partes.find((p) => p.type === tipo)?.value ?? '00';
  const hora = obter('hour') === '24' ? '00' : obter('hour');
  return `${obter('year')}-${obter('month')}-${obter('day')}T${hora}:${obter('minute')}`;
}

export function inputDateTimeSpParaIso(valorLocal: string): string {
  if (!valorLocal) {
    return '';
  }
  return valorLocal.length === 16 ? `${valorLocal}:00-03:00` : `${valorLocal}-03:00`;
}

export function sessaoJaIniciou(dataInicioIso: string): boolean {
  const inicio = new Date(dataInicioIso);
  if (Number.isNaN(inicio.getTime())) {
    return false;
  }
  return Date.now() >= inicio.getTime();
}

