import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HistoricoDocumentosTabs } from '@/components/pacientes/HistoricoDocumentosTabs';
import { MeuProntuarioPage } from '@/pages/MeuProntuarioPage';
import { pacienteService } from '@/services/pacienteService';
import { prontuarioService } from '@/services/prontuarioService';
import type { CategoriaHistorico, PacienteFicha } from '@/types/paciente';

vi.mock('@/services/prontuarioService', () => ({
  prontuarioService: {
    uploadDocumento: vi.fn(),
    substituirDocumento: vi.fn(),
    reclassificarCategoria: vi.fn(),
    baixarDocumento: vi.fn(),
    excluirDocumento: vi.fn(),
  },
}));

vi.mock('@/services/pacienteService', () => ({
  pacienteService: {
    listarPacientes: vi.fn(),
    consultarFichaPaciente: vi.fn(),
    consultarMeuProntuario: vi.fn(),
    atualizarDadosClinicos: vi.fn(),
  },
}));

const HISTORICO_EXEMPLO: CategoriaHistorico[] = [
  {
    categoria: 'Exames',
    totalDocumentos: 1,
    pagina: 0,
    tamanho: 10,
    totalPaginas: 1,
    documentos: [
      {
        id: 102,
        tituloDocumento: 'Exame Audiológico',
        nomeArquivoOriginal: 'audiometria.pdf',
        categoria: 'Exames',
        situacao: 'VIGENTE',
        registroSubstituidoId: null,
        criadoEm: '2026-11-18T09:00:00-03:00',
      },
    ],
  },
  {
    categoria: 'Laudos',
    totalDocumentos: 0,
    pagina: 0,
    tamanho: 10,
    totalPaginas: 0,
    documentos: [],
  },
  {
    categoria: 'Receituario',
    totalDocumentos: 0,
    pagina: 0,
    tamanho: 10,
    totalPaginas: 0,
    documentos: [],
  },
  {
    categoria: 'Arquivado',
    totalDocumentos: 0,
    pagina: 0,
    tamanho: 10,
    totalPaginas: 0,
    documentos: [],
  },
];

const FICHA_PACIENTE: PacienteFicha = {
  id: 12,
  usuarioId: 25,
  nome: 'José Lucas Teste',
  email: 'jose.lucas@amde.example.org',
  dataNascimento: '2016-03-15',
  numCelular: '(15) *****-0011',
  celularMascarado: true,
  pais: 'Brasil',
  estado: 'SP',
  cidade: 'Sorocaba',
  rua: 'Rua Modelo, 300',
  grauSuporte: 'NIVEL_2',
  descricao: 'Acompanhamento terapêutico.',
  atualizadoEm: '2026-11-18T10:30:00-03:00',
  historicoPorCategoria: HISTORICO_EXEMPLO,
};

describe('Incremento 3 - Prontuários Digitais e Anexos PDF', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('valida arquivo PDF e tamanho < 10 MB no UploadProntuarioModal e envia com sucesso', async () => {
    const user = userEvent.setup();
    const onAlterados = vi.fn();
    const uploadMock = vi.mocked(prontuarioService.uploadDocumento).mockResolvedValueOnce({
      id: 200,
      pacienteId: 12,
      categoria: 'Exames',
      registroSubstituidoId: null,
      tituloDocumento: 'Novo Exame TEA',
      nomeArquivoOriginal: 'novo-exame.pdf',
      situacao: 'VIGENTE',
      criadoEm: '2026-11-18T11:00:00-03:00',
      atualizadoEm: '2026-11-18T11:00:00-03:00',
    });

    render(
      <HistoricoDocumentosTabs
        historicoPorCategoria={HISTORICO_EXEMPLO}
        pacienteId={12}
        permitirGerenciarDocumentos={true}
        onMudarPagina={vi.fn()}
        onDocumentosAlterados={onAlterados}
      />
    );

    await user.click(screen.getByRole('button', { name: /Enviar Documento/i }));
    const dialog = screen.getByRole('dialog');

    await user.type(within(dialog).getByLabelText(/Título do documento/i), 'Novo Exame TEA');

    const inputArquivo = within(dialog).getByLabelText(/Arquivo PDF/i);
    const imagemFake = new File(['png-bytes'], 'foto.png', { type: 'image/png' });
    fireEvent.change(inputArquivo, { target: { files: [imagemFake] } });
    expect(await within(dialog).findByText(/Apenas arquivos com extensão \.pdf/i)).toBeInTheDocument();
    expect(within(dialog).getByRole('button', { name: /Enviar PDF/i })).toBeDisabled();

    const arquivo10Mb = new File(['x'], 'grande.pdf', { type: 'application/pdf' });
    Object.defineProperty(arquivo10Mb, 'size', { value: 10 * 1024 * 1024 });
    fireEvent.change(inputArquivo, { target: { files: [arquivo10Mb] } });
    expect(await within(dialog).findByText(/tamanho inferior a 10 MB/i)).toBeInTheDocument();

    const pdfValido = new File(['%PDF-1.7\n'], 'novo-exame.pdf', { type: 'application/pdf' });
    fireEvent.change(inputArquivo, { target: { files: [pdfValido] } });
    await user.click(within(dialog).getByRole('button', { name: /Enviar PDF/i }));

    await waitFor(() => {
      expect(uploadMock).toHaveBeenCalledWith(
        expect.objectContaining({
          pacienteId: 12,
          tituloDocumento: 'Novo Exame TEA',
          categoria: 'Exames',
          arquivo: pdfValido,
        })
      );
    });
    expect(onAlterados).toHaveBeenCalled();
  });

  it('permite substituir, reclassificar/arquivar, baixar e excluir com confirmação e tecla Esc', async () => {
    const user = userEvent.setup();
    const onAlterados = vi.fn();
    const substituirMock = vi.mocked(prontuarioService.substituirDocumento).mockResolvedValueOnce({
      id: 201,
      pacienteId: 12,
      categoria: 'Exames',
      registroSubstituidoId: 102,
      tituloDocumento: 'Exame Revisado',
      nomeArquivoOriginal: 'rev.pdf',
      situacao: 'VIGENTE',
      criadoEm: '2026-11-18T11:30:00-03:00',
      atualizadoEm: '2026-11-18T11:30:00-03:00',
    });
    const reclassificarMock = vi.mocked(prontuarioService.reclassificarCategoria).mockResolvedValueOnce({
      id: 102,
      pacienteId: 12,
      categoria: 'Arquivado',
      registroSubstituidoId: null,
      tituloDocumento: 'Exame Audiológico',
      nomeArquivoOriginal: 'audiometria.pdf',
      situacao: 'ARQUIVADO',
      criadoEm: '2026-11-18T09:00:00-03:00',
      atualizadoEm: '2026-11-18T11:35:00-03:00',
    });
    const baixarMock = vi.mocked(prontuarioService.baixarDocumento).mockResolvedValue();
    const excluirMock = vi.mocked(prontuarioService.excluirDocumento).mockResolvedValue();

    render(
      <HistoricoDocumentosTabs
        historicoPorCategoria={HISTORICO_EXEMPLO}
        pacienteId={12}
        permitirGerenciarDocumentos={true}
        onMudarPagina={vi.fn()}
        onDocumentosAlterados={onAlterados}
      />
    );

    await user.click(screen.getByRole('button', { name: /Baixar PDF Exame Audiológico/i }));
    await waitFor(() => {
      expect(baixarMock).toHaveBeenCalledWith(102, 'audiometria.pdf');
    });

    await user.click(screen.getByRole('button', { name: /Substituir Exame Audiológico/i }));
    const dialogSub = screen.getByRole('dialog');
    const pdfSub = new File(['%PDF-1.7\n'], 'rev.pdf', { type: 'application/pdf' });
    fireEvent.change(within(dialogSub).getByLabelText(/Novo arquivo PDF/i), { target: { files: [pdfSub] } });
    await user.click(within(dialogSub).getByRole('button', { name: /Substituir PDF/i }));
    await waitFor(() => {
      expect(substituirMock).toHaveBeenCalledWith(
        expect.objectContaining({ documentoId: 102, arquivo: pdfSub })
      );
    });

    await user.click(screen.getByRole('button', { name: /Arquivar Exame Audiológico/i }));
    await waitFor(() => {
      expect(reclassificarMock).toHaveBeenCalledWith(102, 'Arquivado');
    });

    await user.click(screen.getByRole('button', { name: /Excluir documento Exame Audiológico/i }));
    expect(screen.getByText('Confirmar Exclusão de Documento')).toBeInTheDocument();
    fireEvent.keyDown(window, { key: 'Escape' });
    expect(screen.queryByText('Confirmar Exclusão de Documento')).not.toBeInTheDocument();
    expect(excluirMock).not.toHaveBeenCalled();

    await user.click(screen.getByRole('button', { name: /Excluir documento Exame Audiológico/i }));
    await user.click(screen.getByRole('button', { name: /Confirmar Exclusão/i }));
    await waitFor(() => {
      expect(excluirMock).toHaveBeenCalledWith(102);
    });
  });

  it('exibe apenas Baixar PDF para o perfil Paciente em MeuProntuarioPage', async () => {
    vi.mocked(pacienteService.consultarMeuProntuario).mockResolvedValueOnce(FICHA_PACIENTE);
    const baixarMock = vi.mocked(prontuarioService.baixarDocumento).mockResolvedValue();

    render(
      <MemoryRouter>
        <MeuProntuarioPage />
      </MemoryRouter>
    );

    const botaoBaixar = await screen.findByRole('button', { name: /Baixar PDF Exame Audiológico/i });
    expect(screen.queryByRole('button', { name: /Enviar Documento/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Substituir/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Excluir/i })).not.toBeInTheDocument();

    await userEvent.click(botaoBaixar);
    expect(baixarMock).toHaveBeenCalledWith(102, 'audiometria.pdf');
  });
});

