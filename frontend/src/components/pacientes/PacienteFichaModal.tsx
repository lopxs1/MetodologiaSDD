import { useCallback, useEffect, useState } from 'react';
import { X } from 'lucide-react';
import { PacienteFichaView } from '@/components/pacientes/PacienteFichaView';
import { extrairErroApi } from '@/services/api';
import { pacienteService } from '@/services/pacienteService';
import type { PacienteFicha } from '@/types/paciente';

interface PacienteFichaModalProps {
  pacienteId: number | null;
  onFechar: () => void;
  onAtualizado: () => void;
}

export function PacienteFichaModal({
  pacienteId,
  onFechar,
  onAtualizado,
}: PacienteFichaModalProps) {
  const [ficha, setFicha] = useState<PacienteFicha | null>(null);
  const [carregando, setCarregando] = useState(false);
  const [carregandoDocs, setCarregandoDocs] = useState(false);
  const [revelandoCelular, setRevelandoCelular] = useState(false);
  const [celularRevelado, setCelularRevelado] = useState(false);
  const [docPage, setDocPage] = useState(0);
  const [erro, setErro] = useState<string | null>(null);

  const carregarFicha = useCallback(
    async (id: number, opcoes: { revelar: boolean; paginaDocs: number; inicial: boolean }) => {
      if (opcoes.inicial) {
        setCarregando(true);
      } else {
        setCarregandoDocs(true);
      }
      setErro(null);

      try {
        const dados = await pacienteService.consultarFichaPaciente(id, {
          revelarCelular: opcoes.revelar,
          docPage: opcoes.paginaDocs,
          docSize: 10,
        });
        setFicha(dados);
      } catch (error) {
        setErro(extrairErroApi(error).mensagem);
      } finally {
        setCarregando(false);
        setCarregandoDocs(false);
      }
    },
    []
  );

  useEffect(() => {
    if (pacienteId == null) {
      setFicha(null);
      setCelularRevelado(false);
      setDocPage(0);
      setErro(null);
      return;
    }

    setCelularRevelado(false);
    setDocPage(0);
    void carregarFicha(pacienteId, { revelar: false, paginaDocs: 0, inicial: true });
  }, [pacienteId, carregarFicha]);

  useEffect(() => {
    if (pacienteId == null) {
      return;
    }
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && document.querySelectorAll('[role="dialog"]').length <= 1) {
        onFechar();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [pacienteId, onFechar]);

  if (pacienteId == null) {
    return null;
  }

  const handleRevelarCelular = async () => {
    setRevelandoCelular(true);
    setErro(null);
    try {
      const dados = await pacienteService.consultarFichaPaciente(pacienteId, {
        revelarCelular: true,
        docPage,
        docSize: 10,
      });
      setCelularRevelado(true);
      setFicha(dados);
    } catch (error) {
      setErro(extrairErroApi(error).mensagem);
    } finally {
      setRevelandoCelular(false);
    }
  };

  const handleMudarPaginaDocs = (novaPagina: number) => {
    setDocPage(novaPagina);
    void carregarFicha(pacienteId, {
      revelar: celularRevelado,
      paginaDocs: novaPagina,
      inicial: false,
    });
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-md bg-neutral-text-primary/50"
      role="dialog"
      aria-modal="true"
      aria-labelledby="ficha-paciente-modal-titulo"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          onFechar();
        }
      }}
    >
      <div className="w-full max-w-3xl max-h-[90vh] flex flex-col rounded-lg border border-neutral-border bg-neutral-surface shadow-lg">
        <div className="flex items-center justify-between px-lg py-md border-b border-neutral-border shrink-0">
          <div>
            <h2 id="ficha-paciente-modal-titulo" className="text-lg font-semibold text-neutral-text-primary">
              Prontuário Digital do Paciente
            </h2>
            {ficha && (
              <p className="text-xs text-neutral-text-secondary">
                Ficha #{ficha.id} · Atualizado em {new Date(ficha.atualizadoEm).toLocaleString('pt-BR')}
              </p>
            )}
          </div>
          <button
            type="button"
            onClick={onFechar}
            className="rounded-md p-1 text-neutral-text-muted hover:text-neutral-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-secondary"
            aria-label="Fechar prontuário"
          >
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </div>

        <div className="p-lg overflow-y-auto flex-1">
          <PacienteFichaView
            ficha={ficha}
            carregando={carregando}
            carregandoDocumentos={carregandoDocs}
            revelandoCelular={revelandoCelular}
            erro={erro}
            permitirEditarClinico={true}
            permitirRevelarCelular={true}
            onRecarregar={() =>
              void carregarFicha(pacienteId, {
                revelar: celularRevelado,
                paginaDocs: docPage,
                inicial: true,
              })
            }
            onRevelarCelular={handleRevelarCelular}
            onMudarPaginaDocumentos={handleMudarPaginaDocs}
            onDadosClinicosAtualizados={(atualizada) => {
              setFicha((prev) =>
                prev && celularRevelado
                  ? { ...atualizada, numCelular: prev.numCelular, celularMascarado: false }
                  : atualizada
              );
              onAtualizado();
            }}
          />
        </div>

        <div className="flex justify-end px-lg py-md border-t border-neutral-border shrink-0">
          <button
            type="button"
            onClick={onFechar}
            className="rounded-md border border-neutral-border bg-neutral-surface px-4 py-2 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background transition-colors"
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  );
}

