import { useEffect, useState, type FormEvent } from 'react';
import { AlertCircle, CheckCircle2, Save } from 'lucide-react';
import { extrairErroApi } from '@/services/api';
import { pacienteService } from '@/services/pacienteService';
import type { PacienteFicha } from '@/types/paciente';
import type { GrauSuporte } from '@/types/usuario';

interface EditarDadosClinicosFormProps {
  pacienteId: number;
  grauSuporteInicial: GrauSuporte;
  descricaoInicial: string | null;
  onAtualizado: (fichaAtualizada: PacienteFicha) => void;
}

const MAX_DESCRICAO = 5000;

export function EditarDadosClinicosForm({
  pacienteId,
  grauSuporteInicial,
  descricaoInicial,
  onAtualizado,
}: EditarDadosClinicosFormProps) {
  const [grauSuporte, setGrauSuporte] = useState<GrauSuporte>(grauSuporteInicial);
  const [descricao, setDescricao] = useState<string>(descricaoInicial ?? '');
  const [erroDescricao, setErroDescricao] = useState<string | undefined>(undefined);
  const [erroGeral, setErroGeral] = useState<string | null>(null);
  const [sucesso, setSucesso] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    setGrauSuporte(grauSuporteInicial);
    setDescricao(descricaoInicial ?? '');
    setErroDescricao(undefined);
    setErroGeral(null);
  }, [pacienteId, grauSuporteInicial, descricaoInicial]);

  const validarDescricao = (valor: string): string | undefined => {
    if (valor.trim().length > MAX_DESCRICAO) {
      return 'Descrição clínica deve ter no máximo 5000 caracteres';
    }
    return undefined;
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setErroGeral(null);
    setSucesso(null);

    const erroValidacao = validarDescricao(descricao);
    setErroDescricao(erroValidacao);
    if (erroValidacao) {
      return;
    }

    setSalvando(true);
    try {
      const descricaoLimpa = descricao.trim();
      const atualizada = await pacienteService.atualizarDadosClinicos(pacienteId, {
        grauSuporte,
        descricao: descricaoLimpa ? descricaoLimpa : null,
      });
      setSucesso('Notas clínicas atualizadas com sucesso.');
      onAtualizado(atualizada);
    } catch (error) {
      const { mensagem, errosCampo } = extrairErroApi(error);
      if (errosCampo.descricao) {
        setErroDescricao(errosCampo.descricao);
      }
      setErroGeral(mensagem);
    } finally {
      setSalvando(false);
    }
  };

  const tamanhoAtual = descricao.trim().length;

  return (
    <form onSubmit={handleSubmit} noValidate className="rounded-md border border-neutral-border bg-neutral-background p-md space-y-md">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-neutral-text-primary">Dados Clínicos e Notas de Atendimento</h3>
      </div>

      {erroGeral && (
        <div
          role="alert"
          className="flex items-start gap-2 rounded-md border border-status-danger bg-status-danger-bg px-3 py-2 text-sm text-status-danger"
        >
          <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" aria-hidden="true" />
          <span>{erroGeral}</span>
        </div>
      )}

      {sucesso && (
        <div
          role="status"
          className="flex items-start gap-2 rounded-md border border-status-success bg-status-success-bg px-3 py-2 text-sm text-status-success"
        >
          <CheckCircle2 className="h-4 w-4 mt-0.5 shrink-0" aria-hidden="true" />
          <span>{sucesso}</span>
        </div>
      )}

      <div>
        <label htmlFor="ficha-grau-suporte" className="block text-sm font-medium text-neutral-text-primary mb-1">
          Grau de suporte <span className="text-status-danger">*</span>
        </label>
        <select
          id="ficha-grau-suporte"
          value={grauSuporte}
          onChange={(e) => {
            setGrauSuporte(e.target.value as GrauSuporte);
            setSucesso(null);
          }}
          className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary"
        >
          <option value="NIVEL_1">Nível 1 (Suporte leve)</option>
          <option value="NIVEL_2">Nível 2 (Suporte substancial)</option>
          <option value="NIVEL_3">Nível 3 (Suporte muito substancial)</option>
        </select>
      </div>

      <div>
        <div className="flex items-center justify-between mb-1">
          <label htmlFor="ficha-descricao-clinica" className="block text-sm font-medium text-neutral-text-primary">
            Descrição clínica
          </label>
          <span
            className={`text-xs font-mono ${
              tamanhoAtual > MAX_DESCRICAO ? 'text-status-danger font-semibold' : 'text-neutral-text-secondary'
            }`}
          >
            {tamanhoAtual} / {MAX_DESCRICAO}
          </span>
        </div>
        <textarea
          id="ficha-descricao-clinica"
          rows={4}
          value={descricao}
          onChange={(e) => {
            const val = e.target.value;
            setDescricao(val);
            setSucesso(null);
            if (erroDescricao) {
              setErroDescricao(validarDescricao(val));
            }
          }}
          onBlur={() => setErroDescricao(validarDescricao(descricao))}
          aria-invalid={Boolean(erroDescricao)}
          aria-describedby={erroDescricao ? 'ficha-descricao-erro' : undefined}
          placeholder="Registre observações clínicas, evolução multidisciplinar e orientações terapêuticas..."
          className={`w-full rounded-md border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary placeholder:text-neutral-text-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary ${
            erroDescricao ? 'border-status-danger' : 'border-neutral-border'
          }`}
        />
        {erroDescricao && (
          <p id="ficha-descricao-erro" className="mt-1 text-xs text-status-danger">
            {erroDescricao}
          </p>
        )}
      </div>

      <div className="flex justify-end">
        <button
          type="submit"
          disabled={salvando || Boolean(erroDescricao)}
          className="inline-flex items-center gap-2 rounded-md bg-brand-primary px-4 py-2 text-sm font-semibold text-neutral-surface hover:bg-brand-primary-hover disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          <Save className="h-4 w-4" aria-hidden="true" />
          {salvando ? 'Salvando...' : 'Salvar Notas'}
        </button>
      </div>
    </form>
  );
}

