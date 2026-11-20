import { useEffect, useState, type FormEvent } from 'react';
import { AlertCircle, Upload, X } from 'lucide-react';
import { extrairErroApi } from '@/services/api';
import { prontuarioService } from '@/services/prontuarioService';
import type { CategoriaProntuario } from '@/types/paciente';

interface UploadProntuarioModalProps {
  aberto: boolean;
  pacienteId: number;
  categoriaInicial?: CategoriaProntuario;
  onFechar: () => void;
  onEnviado: () => void;
}

const TAMANHO_MAXIMO_BYTES = 10 * 1024 * 1024;

export function validarArquivoPdfCliente(arquivo: File | null): string | undefined {
  if (!arquivo) {
    return 'Selecione um arquivo PDF';
  }
  if (arquivo.size <= 0) {
    return 'O arquivo PDF não pode estar vazio';
  }
  if (arquivo.size >= TAMANHO_MAXIMO_BYTES) {
    return 'O arquivo deve ter tamanho inferior a 10 MB';
  }
  const nome = arquivo.name.trim();
  if (nome.length < 5 || nome.length > 255 || !nome.toLowerCase().endsWith('.pdf')) {
    return 'Apenas arquivos com extensão .pdf (5 a 255 caracteres) são aceitos';
  }
  if (arquivo.type && arquivo.type !== 'application/pdf') {
    return 'Tipo de arquivo inválido: selecione um PDF válido';
  }
  return undefined;
}

export function UploadProntuarioModal({
  aberto,
  pacienteId,
  categoriaInicial = 'Exames',
  onFechar,
  onEnviado,
}: UploadProntuarioModalProps) {
  const [tituloDocumento, setTituloDocumento] = useState('');
  const [categoria, setCategoria] = useState<CategoriaProntuario>(categoriaInicial);
  const [arquivo, setArquivo] = useState<File | null>(null);
  const [erros, setErros] = useState<{ tituloDocumento?: string; arquivo?: string }>({});
  const [erroGeral, setErroGeral] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [progresso, setProgresso] = useState(0);

  useEffect(() => {
    if (!aberto) {
      return;
    }
    setTituloDocumento('');
    setCategoria(categoriaInicial);
    setArquivo(null);
    setErros({});
    setErroGeral(null);
    setProgresso(0);
  }, [aberto, categoriaInicial]);

  useEffect(() => {
    if (!aberto) {
      return;
    }
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !enviando) {
        onFechar();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [aberto, enviando, onFechar]);

  if (!aberto) {
    return null;
  }

  const validarTitulo = (valor: string): string | undefined => {
    const trimmed = valor.trim();
    if (!trimmed) {
      return 'Título do documento é obrigatório';
    }
    if (trimmed.length < 2 || trimmed.length > 150) {
      return 'Título deve ter entre 2 e 150 caracteres';
    }
    return undefined;
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setErroGeral(null);

    const erroTitulo = validarTitulo(tituloDocumento);
    const erroArquivo = validarArquivoPdfCliente(arquivo);
    setErros({ tituloDocumento: erroTitulo, arquivo: erroArquivo });
    if (erroTitulo || erroArquivo || !arquivo) {
      return;
    }

    setEnviando(true);
    setProgresso(0);
    try {
      await prontuarioService.uploadDocumento({
        pacienteId,
        tituloDocumento: tituloDocumento.trim(),
        categoria,
        arquivo,
        onProgress: setProgresso,
      });
      onEnviado();
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
      aria-labelledby="upload-prontuario-titulo"
      onClick={(e) => {
        if (e.target === e.currentTarget && !enviando) {
          onFechar();
        }
      }}
    >
      <div className="w-full max-w-lg rounded-lg border border-neutral-border bg-neutral-surface shadow-lg">
        <div className="flex items-center justify-between px-lg py-md border-b border-neutral-border">
          <h2 id="upload-prontuario-titulo" className="text-lg font-semibold text-neutral-text-primary">
            Enviar Documento PDF
          </h2>
          <button
            type="button"
            onClick={onFechar}
            disabled={enviando}
            className="rounded-md p-1 text-neutral-text-muted hover:text-neutral-text-primary"
            aria-label="Fechar envio de documento"
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
            <label htmlFor="upload-titulo" className="block text-sm font-medium text-neutral-text-primary mb-1">
              Título do documento <span className="text-status-danger">*</span>
            </label>
            <input
              id="upload-titulo"
              type="text"
              maxLength={150}
              value={tituloDocumento}
              onChange={(e) => {
                setTituloDocumento(e.target.value);
                if (erros.tituloDocumento) {
                  setErros((prev) => ({ ...prev, tituloDocumento: validarTitulo(e.target.value) }));
                }
              }}
              onBlur={() => setErros((prev) => ({ ...prev, tituloDocumento: validarTitulo(tituloDocumento) }))}
              aria-invalid={Boolean(erros.tituloDocumento)}
              placeholder="Ex.: Laudo neuropsicológico"
              className={`w-full rounded-md border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary ${
                erros.tituloDocumento ? 'border-status-danger' : 'border-neutral-border'
              }`}
            />
            {erros.tituloDocumento && (
              <p className="mt-1 text-xs text-status-danger">{erros.tituloDocumento}</p>
            )}
          </div>

          <div>
            <label htmlFor="upload-categoria" className="block text-sm font-medium text-neutral-text-primary mb-1">
              Categoria <span className="text-status-danger">*</span>
            </label>
            <select
              id="upload-categoria"
              value={categoria}
              onChange={(e) => setCategoria(e.target.value as CategoriaProntuario)}
              className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary"
            >
              <option value="Exames">Exames</option>
              <option value="Laudos">Laudos</option>
              <option value="Receituario">Receituário</option>
              <option value="Arquivado">Arquivado</option>
            </select>
          </div>

          <div>
            <label htmlFor="upload-arquivo" className="block text-sm font-medium text-neutral-text-primary mb-1">
              Arquivo PDF (menor que 10 MB) <span className="text-status-danger">*</span>
            </label>
            <input
              id="upload-arquivo"
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
                <span>Enviando documento...</span>
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
              <Upload className="h-4 w-4" aria-hidden="true" />
              {enviando ? 'Enviando...' : 'Enviar PDF'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

