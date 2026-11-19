import { useRef, useState, type KeyboardEvent } from 'react';
import {
  AlertCircle,
  Archive,
  Download,
  FileText,
  FolderOpen,
  Plus,
  RefreshCw,
  Trash2,
} from 'lucide-react';
import { ConfirmarExclusaoProntuarioDialog } from '@/components/prontuarios/ConfirmarExclusaoProntuarioDialog';
import { SubstituirProntuarioModal } from '@/components/prontuarios/SubstituirProntuarioModal';
import { UploadProntuarioModal } from '@/components/prontuarios/UploadProntuarioModal';
import { extrairErroApi } from '@/services/api';
import { prontuarioService } from '@/services/prontuarioService';
import type {
  CategoriaHistorico,
  CategoriaProntuario,
  DocumentoHistoricoResumo,
  SituacaoDocumento,
} from '@/types/paciente';

interface HistoricoDocumentosTabsProps {
  historicoPorCategoria: CategoriaHistorico[];
  pacienteId?: number;
  permitirGerenciarDocumentos?: boolean;
  carregandoPagina?: boolean;
  onMudarPagina: (novaPagina: number) => void;
  onDocumentosAlterados?: () => void;
}

const ROTULO_CATEGORIA: Record<CategoriaProntuario, string> = {
  Exames: 'Exames',
  Laudos: 'Laudos',
  Receituario: 'Receituário',
  Arquivado: 'Arquivado',
};

const ORDEM_CATEGORIAS: CategoriaProntuario[] = ['Exames', 'Laudos', 'Receituario', 'Arquivado'];

const BADGE_SITUACAO: Record<SituacaoDocumento, { rotulo: string; classe: string }> = {
  VIGENTE: {
    rotulo: 'Vigente',
    classe: 'border-status-success bg-status-success-bg text-status-success',
  },
  SUBSTITUIDO: {
    rotulo: 'Substituído',
    classe: 'border-status-warning bg-status-warning-bg text-status-warning',
  },
  ARQUIVADO: {
    rotulo: 'Arquivado',
    classe: 'border-neutral-border bg-neutral-background text-neutral-text-secondary',
  },
};

export function HistoricoDocumentosTabs({
  historicoPorCategoria,
  pacienteId,
  permitirGerenciarDocumentos = false,
  carregandoPagina = false,
  onMudarPagina,
  onDocumentosAlterados,
}: HistoricoDocumentosTabsProps) {
  const [abaAtiva, setAbaAtiva] = useState<CategoriaProntuario>('Exames');
  const [uploadAberto, setUploadAberto] = useState(false);
  const [documentoSubstituir, setDocumentoSubstituir] = useState<DocumentoHistoricoResumo | null>(null);
  const [documentoExcluir, setDocumentoExcluir] = useState<DocumentoHistoricoResumo | null>(null);
  const [baixandoId, setBaixandoId] = useState<number | null>(null);
  const [reclassificandoId, setReclassificandoId] = useState<number | null>(null);
  const [erroAcao, setErroAcao] = useState<string | null>(null);

  const tabRefs = useRef<Partial<Record<CategoriaProntuario, HTMLButtonElement | null>>>({});

  const mapaCategorias = new Map(historicoPorCategoria.map((item) => [item.categoria, item]));
  const categoriaAtual: CategoriaHistorico = mapaCategorias.get(abaAtiva) ?? {
    categoria: abaAtiva,
    totalDocumentos: 0,
    pagina: 0,
    tamanho: 10,
    totalPaginas: 0,
    documentos: [],
  };

  const handleTabKeyDown = (event: KeyboardEvent<HTMLButtonElement>, indiceAtual: number) => {
    let proximoIndice: number | null = null;
    if (event.key === 'ArrowRight') {
      proximoIndice = (indiceAtual + 1) % ORDEM_CATEGORIAS.length;
    } else if (event.key === 'ArrowLeft') {
      proximoIndice = (indiceAtual - 1 + ORDEM_CATEGORIAS.length) % ORDEM_CATEGORIAS.length;
    } else if (event.key === 'Home') {
      proximoIndice = 0;
    } else if (event.key === 'End') {
      proximoIndice = ORDEM_CATEGORIAS.length - 1;
    }

    if (proximoIndice !== null) {
      event.preventDefault();
      const proxima = ORDEM_CATEGORIAS[proximoIndice];
      setAbaAtiva(proxima);
      tabRefs.current[proxima]?.focus();
    }
  };

  const handleDownload = async (doc: DocumentoHistoricoResumo) => {
    setBaixandoId(doc.id);
    setErroAcao(null);
    try {
      await prontuarioService.baixarDocumento(doc.id, doc.nomeArquivoOriginal);
    } catch (error) {
      setErroAcao(extrairErroApi(error).mensagem);
    } finally {
      setBaixandoId(null);
    }
  };

  const handleReclassificar = async (docId: number, novaCategoria: CategoriaProntuario) => {
    setReclassificandoId(docId);
    setErroAcao(null);
    try {
      await prontuarioService.reclassificarCategoria(docId, novaCategoria);
      onDocumentosAlterados?.();
    } catch (error) {
      setErroAcao(extrairErroApi(error).mensagem);
    } finally {
      setReclassificandoId(null);
    }
  };

  return (
    <section aria-labelledby="historico-prontuario-titulo" className="space-y-md">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h3 id="historico-prontuario-titulo" className="text-sm font-semibold text-neutral-text-primary">
          Histórico Documental do Prontuário
        </h3>
        {permitirGerenciarDocumentos && pacienteId != null && (
          <button
            type="button"
            onClick={() => setUploadAberto(true)}
            className="inline-flex items-center gap-1.5 rounded-md bg-brand-primary px-3 py-1.5 text-xs font-semibold text-neutral-surface hover:bg-brand-primary-hover transition-colors"
          >
            <Plus className="h-3.5 w-3.5" aria-hidden="true" />
            Enviar Documento
          </button>
        )}
      </div>

      {erroAcao && (
        <div
          role="alert"
          className="flex items-start gap-2 rounded-md border border-status-danger bg-status-danger-bg px-3 py-2 text-sm text-status-danger"
        >
          <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" aria-hidden="true" />
          <span>{erroAcao}</span>
        </div>
      )}

      <div
        role="tablist"
        aria-label="Categorias de documentos do prontuário"
        className="flex flex-wrap gap-1 border-b border-neutral-border pb-xs"
      >
        {ORDEM_CATEGORIAS.map((categoria, indice) => {
          const dadosCategoria = mapaCategorias.get(categoria);
          const total = dadosCategoria?.totalDocumentos ?? 0;
          const selecionada = abaAtiva === categoria;

          return (
            <button
              key={categoria}
              ref={(el) => {
                tabRefs.current[categoria] = el;
              }}
              id={`tab-${categoria}`}
              type="button"
              role="tab"
              aria-selected={selecionada}
              aria-controls={`panel-${categoria}`}
              tabIndex={selecionada ? 0 : -1}
              onClick={() => setAbaAtiva(categoria)}
              onKeyDown={(e) => handleTabKeyDown(e, indice)}
              className={`inline-flex items-center gap-2 rounded-md px-3 py-1.5 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary ${
                selecionada
                  ? 'bg-brand-primary text-neutral-surface'
                  : 'text-neutral-text-secondary hover:bg-neutral-background hover:text-neutral-text-primary'
              }`}
            >
              <span>{ROTULO_CATEGORIA[categoria]}</span>
              <span
                className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
                  selecionada
                    ? 'bg-brand-primary-hover text-neutral-surface'
                    : 'bg-neutral-background text-neutral-text-primary'
                }`}
              >
                {total}
              </span>
            </button>
          );
        })}
      </div>

      <div
        id={`panel-${abaAtiva}`}
        role="tabpanel"
        aria-labelledby={`tab-${abaAtiva}`}
        className="space-y-sm"
      >
        {carregandoPagina ? (
          <div className="space-y-sm" aria-busy="true" aria-label="Carregando documentos">
            {[1, 2, 3].map((item) => (
              <div key={item} className="h-16 rounded-md bg-neutral-background animate-pulse" />
            ))}
          </div>
        ) : categoriaAtual.documentos.length === 0 ? (
          <div className="rounded-md border border-dashed border-neutral-border bg-neutral-background/50 p-lg text-center space-y-xs">
            <div className="mx-auto flex h-10 w-10 items-center justify-center rounded-full bg-neutral-surface text-neutral-text-muted">
              <FolderOpen className="h-5 w-5" aria-hidden="true" />
            </div>
            <p className="text-sm font-medium text-neutral-text-primary">Nenhum documento nesta categoria</p>
            <p className="text-xs text-neutral-text-secondary">
              Não existem documentos ativos registrados em {ROTULO_CATEGORIA[abaAtiva]}.
            </p>
          </div>
        ) : (
          <>
            <ul className="divide-y divide-neutral-border rounded-md border border-neutral-border bg-neutral-surface">
              {categoriaAtual.documentos.map((doc) => {
                const badge = BADGE_SITUACAO[doc.situacao];
                const ocupado = baixandoId === doc.id || reclassificandoId === doc.id;
                return (
                  <li key={doc.id} className="p-md space-y-sm">
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-sm">
                      <div className="flex items-start gap-3 min-w-0">
                        <div className="mt-0.5 rounded-md bg-status-info-bg p-2 text-brand-secondary shrink-0">
                          <FileText className="h-4 w-4" aria-hidden="true" />
                        </div>
                        <div className="min-w-0 space-y-0.5">
                          <div className="flex flex-wrap items-center gap-2">
                            <span className="font-mono text-xs text-neutral-text-secondary">#{doc.id}</span>
                            <p className="text-sm font-semibold text-neutral-text-primary truncate">
                              {doc.tituloDocumento}
                            </p>
                          </div>
                          <p className="text-xs text-neutral-text-secondary font-mono truncate">
                            {doc.nomeArquivoOriginal}
                          </p>
                          <div className="flex flex-wrap items-center gap-3 text-xs text-neutral-text-secondary">
                            <span>Categoria: {ROTULO_CATEGORIA[doc.categoria] ?? doc.categoria}</span>
                            <span>
                              Enviado em {new Date(doc.criadoEm).toLocaleString('pt-BR')}
                            </span>
                            {doc.registroSubstituidoId != null && (
                              <span>Substitui documento #{doc.registroSubstituidoId}</span>
                            )}
                          </div>
                        </div>
                      </div>

                      <div className="shrink-0">
                        <span
                          className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium ${badge.classe}`}
                        >
                          {badge.rotulo}
                        </span>
                      </div>
                    </div>

                    <div className="flex flex-wrap items-center justify-end gap-2 pt-xs border-t border-neutral-border/60">
                      <button
                        type="button"
                        disabled={ocupado}
                        onClick={() => void handleDownload(doc)}
                        aria-label={`Baixar PDF ${doc.tituloDocumento}`}
                        className="inline-flex items-center gap-1 rounded border border-neutral-border bg-neutral-surface px-2.5 py-1 text-xs font-medium text-brand-secondary hover:bg-status-info-bg disabled:opacity-50"
                      >
                        <Download className="h-3.5 w-3.5" aria-hidden="true" />
                        {baixandoId === doc.id ? 'Baixando...' : 'Baixar PDF'}
                      </button>

                      {permitirGerenciarDocumentos && (
                        <>
                          {doc.situacao === 'VIGENTE' && (
                            <button
                              type="button"
                              disabled={ocupado}
                              onClick={() => setDocumentoSubstituir(doc)}
                              aria-label={`Substituir ${doc.tituloDocumento}`}
                              className="inline-flex items-center gap-1 rounded border border-neutral-border bg-neutral-surface px-2.5 py-1 text-xs font-medium text-brand-primary hover:bg-neutral-background disabled:opacity-50"
                            >
                              <RefreshCw className="h-3.5 w-3.5" aria-hidden="true" />
                              Substituir
                            </button>
                          )}

                          <select
                            aria-label={`Reclassificar categoria de ${doc.tituloDocumento}`}
                            value={doc.categoria}
                            disabled={ocupado}
                            onChange={(e) => void handleReclassificar(doc.id, e.target.value as CategoriaProntuario)}
                            className="rounded border border-neutral-border bg-neutral-surface px-2 py-1 text-xs text-neutral-text-primary disabled:opacity-50"
                          >
                            <option value="Exames">Exames</option>
                            <option value="Laudos">Laudos</option>
                            <option value="Receituario">Receituário</option>
                            <option value="Arquivado">Arquivado</option>
                          </select>

                          {doc.categoria !== 'Arquivado' && (
                            <button
                              type="button"
                              disabled={ocupado}
                              onClick={() => void handleReclassificar(doc.id, 'Arquivado')}
                              aria-label={`Arquivar ${doc.tituloDocumento}`}
                              className="inline-flex items-center gap-1 rounded border border-neutral-border bg-neutral-surface px-2.5 py-1 text-xs font-medium text-neutral-text-secondary hover:bg-neutral-background disabled:opacity-50"
                            >
                              <Archive className="h-3.5 w-3.5" aria-hidden="true" />
                              Arquivar
                            </button>
                          )}

                          <button
                            type="button"
                            disabled={ocupado}
                            onClick={() => setDocumentoExcluir(doc)}
                            aria-label={`Excluir documento ${doc.tituloDocumento}`}
                            className="inline-flex items-center gap-1 rounded border border-status-danger bg-neutral-surface px-2.5 py-1 text-xs font-medium text-status-danger hover:bg-status-danger-bg disabled:opacity-50"
                          >
                            <Trash2 className="h-3.5 w-3.5" aria-hidden="true" />
                            Excluir
                          </button>
                        </>
                      )}
                    </div>
                  </li>
                );
              })}
            </ul>

            {categoriaAtual.totalPaginas > 1 && (
              <div className="flex items-center justify-between pt-xs text-xs text-neutral-text-secondary">
                <span>
                  Página {categoriaAtual.pagina + 1} de {categoriaAtual.totalPaginas} ({categoriaAtual.totalDocumentos}{' '}
                  documentos)
                </span>
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    disabled={categoriaAtual.pagina <= 0}
                    onClick={() => onMudarPagina(Math.max(0, categoriaAtual.pagina - 1))}
                    className="rounded-md border border-neutral-border bg-neutral-surface px-2.5 py-1 font-medium text-neutral-text-primary hover:bg-neutral-background disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Anterior
                  </button>
                  <button
                    type="button"
                    disabled={categoriaAtual.pagina + 1 >= categoriaAtual.totalPaginas}
                    onClick={() => onMudarPagina(categoriaAtual.pagina + 1)}
                    className="rounded-md border border-neutral-border bg-neutral-surface px-2.5 py-1 font-medium text-neutral-text-primary hover:bg-neutral-background disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Próxima
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {pacienteId != null && (
        <UploadProntuarioModal
          aberto={uploadAberto}
          pacienteId={pacienteId}
          categoriaInicial={abaAtiva}
          onFechar={() => setUploadAberto(false)}
          onEnviado={() => {
            setUploadAberto(false);
            onDocumentosAlterados?.();
          }}
        />
      )}

      <SubstituirProntuarioModal
        documento={documentoSubstituir}
        onFechar={() => setDocumentoSubstituir(null)}
        onSubstituido={() => {
          setDocumentoSubstituir(null);
          onDocumentosAlterados?.();
        }}
      />

      <ConfirmarExclusaoProntuarioDialog
        documento={documentoExcluir}
        onFechar={() => setDocumentoExcluir(null)}
        onExcluido={() => {
          setDocumentoExcluir(null);
          onDocumentosAlterados?.();
        }}
      />
    </section>
  );
}
