import { useCallback, useEffect, useState } from 'react';
import { PacienteFichaView } from '@/components/pacientes/PacienteFichaView';
import { extrairErroApi } from '@/services/api';
import { pacienteService } from '@/services/pacienteService';
import type { PacienteFicha } from '@/types/paciente';

export function MeuProntuarioPage() {
  const [ficha, setFicha] = useState<PacienteFicha | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [carregandoDocs, setCarregandoDocs] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [docPage, setDocPage] = useState(0);

  const carregarMeuProntuario = useCallback(async (paginaDocs: number, inicial: boolean) => {
    if (inicial) {
      setCarregando(true);
    } else {
      setCarregandoDocs(true);
    }
    setErro(null);
    try {
      const dados = await pacienteService.consultarMeuProntuario({
        docPage: paginaDocs,
        docSize: 10,
      });
      setFicha(dados);
    } catch (error) {
      setErro(extrairErroApi(error).mensagem);
    } finally {
      setCarregando(false);
      setCarregandoDocs(false);
    }
  }, []);

  useEffect(() => {
    void carregarMeuProntuario(0, true);
  }, [carregarMeuProntuario]);

  const handleMudarPaginaDocs = (novaPagina: number) => {
    setDocPage(novaPagina);
    void carregarMeuProntuario(novaPagina, false);
  };

  return (
    <div className="space-y-lg">
      <div>
        <h1 className="text-2xl font-bold text-neutral-text-primary">Meu Prontuário</h1>
        <p className="text-sm text-neutral-text-secondary">
          Consulte sua ficha clínica e o histórico de documentos vinculados ao seu atendimento
        </p>
      </div>

      <div className="rounded-lg border border-neutral-border bg-neutral-surface p-lg shadow-sm">
        <PacienteFichaView
          ficha={ficha}
          carregando={carregando}
          carregandoDocumentos={carregandoDocs}
          erro={erro}
          permitirEditarClinico={false}
          permitirRevelarCelular={false}
          onRecarregar={() => void carregarMeuProntuario(docPage, true)}
          onMudarPaginaDocumentos={handleMudarPaginaDocs}
        />
      </div>
    </div>
  );
}

