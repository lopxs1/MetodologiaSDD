import { AlertCircle, Eye, FileHeart, RefreshCw } from 'lucide-react';
import { EditarDadosClinicosForm } from '@/components/pacientes/EditarDadosClinicosForm';
import { HistoricoDocumentosTabs } from '@/components/pacientes/HistoricoDocumentosTabs';
import type { PacienteFicha } from '@/types/paciente';
import type { GrauSuporte } from '@/types/usuario';

interface PacienteFichaViewProps {
  ficha: PacienteFicha | null;
  carregando: boolean;
  carregandoDocumentos?: boolean;
  revelandoCelular?: boolean;
  erro: string | null;
  permitirEditarClinico: boolean;
  permitirRevelarCelular: boolean;
  onRecarregar: () => void;
  onRevelarCelular?: () => void;
  onMudarPaginaDocumentos: (novaPagina: number) => void;
  onDadosClinicosAtualizados?: (fichaAtualizada: PacienteFicha) => void;
}

const ROTULO_SUPORTE: Record<GrauSuporte, { rotulo: string; classe: string }> = {
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

export function PacienteFichaView({
  ficha,
  carregando,
  carregandoDocumentos = false,
  revelandoCelular = false,
  erro,
  permitirEditarClinico,
  permitirRevelarCelular,
  onRecarregar,
  onRevelarCelular,
  onMudarPaginaDocumentos,
  onDadosClinicosAtualizados,
}: PacienteFichaViewProps) {
  if (carregando) {
    return (
      <div className="space-y-lg" aria-busy="true" aria-label="Carregando ficha do paciente">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-md">
          {[1, 2, 3, 4].map((item) => (
            <div key={item} className="h-12 rounded-md bg-neutral-background animate-pulse" />
          ))}
        </div>
        <div className="h-36 rounded-md bg-neutral-background animate-pulse" />
        <div className="h-44 rounded-md bg-neutral-background animate-pulse" />
      </div>
    );
  }

  if (erro) {
    return (
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
          onClick={onRecarregar}
          className="inline-flex items-center gap-2 rounded-md bg-brand-primary px-4 py-2 text-sm font-medium text-neutral-surface hover:bg-brand-primary-hover transition-colors"
        >
          <RefreshCw className="h-4 w-4" aria-hidden="true" />
          Recarregar
        </button>
      </div>
    );
  }

  if (!ficha) {
    return (
      <div className="py-xl text-center space-y-sm">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-neutral-background text-neutral-text-muted">
          <FileHeart className="h-6 w-6" aria-hidden="true" />
        </div>
        <h2 className="text-base font-semibold text-neutral-text-primary">Ficha não disponível</h2>
        <p className="text-sm text-neutral-text-secondary">
          Não foi possível localizar os dados deste prontuário.
        </p>
      </div>
    );
  }

  const badgeSuporte = ROTULO_SUPORTE[ficha.grauSuporte];
  const enderecoFormatado = [ficha.rua, ficha.cidade, ficha.estado, ficha.pais].filter(Boolean).join(', ');

  return (
    <div className="space-y-lg">
      <section aria-labelledby="dados-cadastrais-titulo" className="rounded-md border border-neutral-border bg-neutral-surface p-md space-y-md">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h3 id="dados-cadastrais-titulo" className="text-sm font-semibold text-neutral-text-primary">
            Dados Cadastrais
          </h3>
          <span
            className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold ${badgeSuporte.classe}`}
          >
            {badgeSuporte.rotulo}
          </span>
        </div>

        <dl className="grid grid-cols-1 sm:grid-cols-2 gap-md text-sm">
          <div>
            <dt className="text-xs font-medium text-neutral-text-secondary">Nome completo</dt>
            <dd className="mt-0.5 font-medium text-neutral-text-primary">{ficha.nome}</dd>
          </div>
          <div>
            <dt className="text-xs font-medium text-neutral-text-secondary">E-mail</dt>
            <dd className="mt-0.5 text-neutral-text-primary">{ficha.email}</dd>
          </div>
          <div>
            <dt className="text-xs font-medium text-neutral-text-secondary">Celular</dt>
            <dd className="mt-0.5 flex flex-wrap items-center gap-2 font-mono text-neutral-text-primary">
              <span>{ficha.numCelular || 'Não informado'}</span>
              {permitirRevelarCelular && ficha.numCelular && ficha.celularMascarado && onRevelarCelular && (
                <button
                  type="button"
                  onClick={onRevelarCelular}
                  disabled={revelandoCelular}
                  className="inline-flex items-center gap-1 rounded border border-brand-secondary px-2 py-0.5 font-sans text-xs font-medium text-brand-secondary hover:bg-status-info-bg disabled:opacity-50"
                >
                  <Eye className="h-3.5 w-3.5" aria-hidden="true" />
                  {revelandoCelular ? 'Revelando...' : 'Revelar celular'}
                </button>
              )}
            </dd>
          </div>
          <div>
            <dt className="text-xs font-medium text-neutral-text-secondary">Data de nascimento</dt>
            <dd className="mt-0.5 text-neutral-text-primary">
              {ficha.dataNascimento
                ? new Date(`${ficha.dataNascimento}T00:00:00`).toLocaleDateString('pt-BR')
                : 'Não informada'}
            </dd>
          </div>
          <div className="sm:col-span-2">
            <dt className="text-xs font-medium text-neutral-text-secondary">Endereço</dt>
            <dd className="mt-0.5 text-neutral-text-primary">{enderecoFormatado || 'Não informado'}</dd>
          </div>
        </dl>
      </section>

      {permitirEditarClinico && onDadosClinicosAtualizados ? (
        <EditarDadosClinicosForm
          pacienteId={ficha.id}
          grauSuporteInicial={ficha.grauSuporte}
          descricaoInicial={ficha.descricao}
          onAtualizado={onDadosClinicosAtualizados}
        />
      ) : (
        <section
          aria-labelledby="dados-clinicos-leitura-titulo"
          className="rounded-md border border-neutral-border bg-neutral-background p-md space-y-sm"
        >
          <h3 id="dados-clinicos-leitura-titulo" className="text-sm font-semibold text-neutral-text-primary">
            Dados Clínicos
          </h3>
          <div className="text-sm space-y-xs">
            <p className="text-neutral-text-primary">
              <span className="font-medium text-neutral-text-secondary">Grau de suporte: </span>
              {badgeSuporte.rotulo}
            </p>
            <div>
              <p className="font-medium text-neutral-text-secondary">Descrição clínica:</p>
              <p className="mt-0.5 text-neutral-text-primary whitespace-pre-line">
                {ficha.descricao || 'Não informada'}
              </p>
            </div>
          </div>
        </section>
      )}

      <HistoricoDocumentosTabs
        historicoPorCategoria={ficha.historicoPorCategoria}
        pacienteId={ficha.id}
        permitirGerenciarDocumentos={permitirEditarClinico}
        carregandoPagina={carregandoDocumentos}
        onMudarPagina={onMudarPaginaDocumentos}
        onDocumentosAlterados={onRecarregar}
      />
    </div>
  );
}

