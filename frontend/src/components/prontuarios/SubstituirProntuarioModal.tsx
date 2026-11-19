import { useEffect, useState, type FormEvent } from 'react';
import { AlertCircle, RefreshCw, X } from 'lucide-react';
import { validarArquivoPdfCliente } from '@/components/prontuarios/UploadProntuarioModal';
import { extrairErroApi } from '@/services/api';
import { prontuarioService } from '@/services/prontuarioService';
import type { DocumentoHistoricoResumo } from '@/types/paciente';

interface SubstituirProntuarioModalProps {
  documento: DocumentoHistoricoResumo | null;
  onFechar: () => void;
  onSubstituido: () => void;
}

export function SubstituirProntuarioModal({
  documento,
  onFechar,
  onSubstituido,
}: SubstituirProntuarioModalProps) {
  const [tituloDocumento, setTituloDocumento] = useState('');
  const [arquivo, setArquivo] = useState<File | null>(null);
  const [erros, setErros] = useState<{ tituloDocumento?: string; arquivo?: string }>({});
  const [erroGeral, setErroGeral] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [progresso, setProgresso] = useState(0);

  useEffect(() => {
    if (!documento) {
      return;
    }
    setTituloDocumento(documento.tituloDocumento);
    setArquivo(null);
    setErros({});
    setErroGeral(null);
    setProgresso(0);
  }, [documento]);

  useEffect(() => {
    if (!documento) {
      return;
    }
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !enviando) {
        onFechar();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [documento, enviando, onFechar]);

  if (!documento) {
    return null;
  }

  const validarTituloOpcional = (valor: string): string | undefined => {
    const trimmed = valor.trim();
    if (!trimmed) {
      return undefined;
    }
    if (trimmed.length < 2 || trimmed.length > 150) {
      return 'Título deve ter entre 2 e 150 caracteres';
    }
    return undefined;
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setErroGeral(null);

    const erroTitulo = validarTituloOpcional(tituloDocumento);
    const erroArquivo = validarArquivoPdfCliente(arquivo);
    setErros({ tituloDocumento: erroTitulo, arquivo: erroArquivo });
    if (erroTitulo || erroArquivo || !arquivo) {
      return;
    }

    setEnviando(true);
    setProgresso(0);
    try {
      await prontuarioService.substituirDocumento({
        documentoId: documento.id,
        tituloDocumento: tituloDocumento.trim() || null,
        arquivo,
        onProgress: setProgresso,
      });
      onSubstituido();
    } catch (error) {
      const { mensagem, errosCampo } = extrairErroApi(error);
      if (Object.keys(errosCampo).length > 0) {
        setErros((prev) => ({
          ...prev,
          tituloDocumento: errosCampo.tituloDocumento ?? prev.tituloDocumento,
          arquivo: errosCampo.arquivo ?? prev.arquivo,
        }));
      }
      setErroGeral(mensagem);
    } finally {
      setEnviando(false);
    }
  };

  const possuiErroPendente = Boolean(erros.tituloDocumento || erros.arquivo);

  return (
    <div
      className="fixed inset-0 z-[60] flex items-center justify-center p-md bg-neutral-text-primary/50"
      role="dialog"
      aria-modal="true"
      aria-labelledby="substituir-prontuario-titulo"
      onClick={(e) => {
        if (e.target === e.currentTarget && !enviando) {
          onFechar();
        }
      }}
    >
      <div className="w-full max-w-lg rounded-lg border border-neutral-border bg-neutral-surface shadow-lg">
        <div className="flex items-center justify-between px-lg py-md border-b border-neutral-border">
          <div>
            <h2 id="substituir-prontuario-titulo" className="text-lg font-semibold text-neutral-text-primary">
              Substituir Versão do Documento
            </h2>
            <p className="text-xs text-neutral-text-secondary">
              Versão anterior #{documento.id} permanecerá no histórico como Substituído.
            </p>
          </div>
          <button
            type="button"
            onClick={onFechar}
            disabled={enviando}
            className="rounded-md p-1 text-neutral-text-muted hover:text-neutral-text-primary"
            aria-label="Fechar substituição"
          >
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </div>

        <form onSubmit={handleSubmit} noValidate className="p-lg space-y-md">
          {erroGeral && (
            <div
              role="alert"
              className="flex items-start gap-2 rounded-md border border-status-danger bg-status-danger-bg px-3 py-2 text-sm text-status-danger"
            >
              <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" aria-hidden="true" />
              <span>{erroGeral}</span>
            </div>
          )}

          <div>
            <label htmlFor="substituir-titulo" className="block text-sm font-medium text-neutral-text-primary mb-1">
              Novo título (opcional — padrão mantém o atual)
            </label>
            <input
              id="substituir-titulo"
              type="text"
              maxLength={150}
              value={tituloDocumento}
              onChange={(e) => {
                setTituloDocumento(e.target.value);
                if (erros.tituloDocumento) {
                  setErros((prev) => ({ ...prev, tituloDocumento: validarTituloOpcional(e.target.value) }));
                }
              }}
              onBlur={() => setErros((prev) => ({ ...prev, tituloDocumento: validarTituloOpcional(tituloDocumento) }))}
              aria-invalid={Boolean(erros.tituloDocumento)}
              className={`w-full rounded-md border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary ${
                erros.tituloDocumento ? 'border-status-danger' : 'border-neutral-border'
              }`}
            />
            {erros.tituloDocumento && (
              <p className="mt-1 text-xs text-status-danger">{erros.tituloDocumento}</p>
            )}
          </div>

          <div>
            <label htmlFor="substituir-arquivo" className="block text-sm font-medium text-neutral-text-primary mb-1">
              Novo arquivo PDF (menor que 10 MB) <span className="text-status-danger">*</span>
            </label>
            <input
              id="substituir-arquivo"
              type="file"
              accept=".pdf,application/pdf"
              onChange={(e) => {
                const selecionado = e.target.files?.[0] ?? null;
                setArquivo(selecionado);
                setErros((prev) => ({ ...prev, arquivo: validarArquivoPdfCliente(selecionado) }));
              }}
              aria-invalid={Boolean(erros.arquivo)}
              className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary"
            />
            {erros.arquivo && <p className="mt-1 text-xs text-status-danger">{erros.arquivo}</p>}
          </div>

          {enviando && (
            <div className="space-y-xs">
              <div className="flex justify-between text-xs text-neutral-text-secondary">
                <span>Enviando nova versão...</span>
                <span>{progresso}%</span>
              </div>
              <div
                role="progressbar"
                aria-valuenow={progresso}
                aria-valuemin={0}
                aria-valuemax={100}
                className="h-2 w-full overflow-hidden rounded-full bg-neutral-background"
              >
                <div
                  className="h-full bg-brand-primary transition-all"
                  style={{ width: `${progresso}%` }}
                />
              </div>
            </div>
          )}

          <div className="flex justify-end gap-sm pt-md border-t border-neutral-border">
            <button
              type="button"
              onClick={onFechar}
              disabled={enviando}
              className="rounded-md border border-neutral-border bg-neutral-surface px-4 py-2 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={enviando || possuiErroPendente}
              className="inline-flex items-center gap-2 rounded-md bg-brand-primary px-4 py-2 text-sm font-semibold text-neutral-surface hover:bg-brand-primary-hover disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              <RefreshCw className="h-4 w-4" aria-hidden="true" />
              {enviando ? 'Substituindo...' : 'Substituir PDF'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

