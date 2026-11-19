import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MeuProntuarioPage } from '@/pages/MeuProntuarioPage';
import { PacientesPage } from '@/pages/PacientesPage';
import { pacienteService } from '@/services/pacienteService';
import type { PacienteFicha } from '@/types/paciente';

vi.mock('@/services/pacienteService', () => ({
  pacienteService: {
    listarPacientes: vi.fn(),
    consultarFichaPaciente: vi.fn(),
    consultarMeuProntuario: vi.fn(),
    atualizarDadosClinicos: vi.fn(),
  },
}));

const FICHA_EXEMPLO: PacienteFicha = {
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
  descricao: 'Acompanhamento fonoaudiológico.',
  atualizadoEm: '2026-11-18T10:30:00-03:00',
  historicoPorCategoria: [
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
      totalDocumentos: 2,
      pagina: 0,
      tamanho: 10,
      totalPaginas: 1,
      documentos: [
        {
          id: 101,
          tituloDocumento: 'Laudo Atualizado',
          nomeArquivoOriginal: 'laudo-v2.pdf',
          categoria: 'Laudos',
          situacao: 'VIGENTE',
          registroSubstituidoId: 98,
          criadoEm: '2026-11-18T10:00:00-03:00',
        },
        {
          id: 98,
          tituloDocumento: 'Laudo Inicial',
          nomeArquivoOriginal: 'laudo-v1.pdf',
          categoria: 'Laudos',
          situacao: 'SUBSTITUIDO',
          registroSubstituidoId: null,
          criadoEm: '2026-11-10T10:00:00-03:00',
        },
      ],
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
      totalDocumentos: 1,
      pagina: 0,
      tamanho: 10,
      totalPaginas: 1,
      documentos: [
        {
          id: 50,
          tituloDocumento: 'Encaminhamento Antigo',
          nomeArquivoOriginal: 'antigo.pdf',
          categoria: 'Arquivado',
          situacao: 'ARQUIVADO',
          registroSubstituidoId: null,
          criadoEm: '2025-05-01T10:00:00-03:00',
        },
      ],
    },
  ],
};

describe('Incremento 2 - PacientesPage e MeuProntuarioPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renderiza os 4 estados da tela PacientesPage (Loading, Erro, Vazio e Sucesso) e filtra/ordena', async () => {
    const listarMock = vi.mocked(pacienteService.listarPacientes);

    listarMock.mockImplementationOnce(() => new Promise(() => {}));
    const { unmount } = render(
      <MemoryRouter>
        <PacientesPage />
      </MemoryRouter>
    );
    expect(screen.getByLabelText('Carregando pacientes')).toBeInTheDocument();
    unmount();

    listarMock.mockRejectedValueOnce(new Error('Falha de rede'));
    listarMock.mockResolvedValueOnce({
      itens: [],
      pagina: 0,
      tamanho: 10,
      totalItens: 0,
      totalPaginas: 0,
    });

    render(
      <MemoryRouter>
        <PacientesPage />
      </MemoryRouter>
    );

    expect(await screen.findByRole('alert')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /Recarregar/i }));
    expect(await screen.findByText('Nenhum paciente encontrado')).toBeInTheDocument();

    listarMock.mockResolvedValue({
      itens: [
        {
          id: 12,
          usuarioId: 25,
          nome: 'José Lucas Teste',
          email: 'jose.lucas@amde.example.org',
          grauSuporte: 'NIVEL_2',
          numCelular: '(15) *****-0011',
        },
      ],
      pagina: 0,
      tamanho: 10,
      totalItens: 1,
      totalPaginas: 1,
    });

    fireEvent.change(screen.getByLabelText('Filtrar por grau de suporte'), { target: { value: 'NIVEL_2' } });
    expect(await screen.findByText('José Lucas Teste')).toBeInTheDocument();
    expect(screen.getByText('(15) *****-0011')).toBeInTheDocument();

    await userEvent.type(screen.getByLabelText('Buscar por nome ou e-mail'), 'jose');
    await waitFor(() => {
      expect(listarMock).toHaveBeenLastCalledWith(
        expect.objectContaining({
          termo: 'jose',
          grauSuporte: 'NIVEL_2',
        })
      );
    });

    await userEvent.click(screen.getByRole('button', { name: /E-mail/i }));
    await waitFor(() => {
      expect(listarMock).toHaveBeenLastCalledWith(
        expect.objectContaining({
          sort: 'email',
          direction: 'ASC',
        })
      );
    });
  });

  it('abre modal da ficha, revela celular, navega nas abas de histórico e valida/salva notas clínicas', async () => {
    const user = userEvent.setup();
    vi.mocked(pacienteService.listarPacientes).mockResolvedValue({
      itens: [
        {
          id: 12,
          usuarioId: 25,
          nome: 'José Lucas Teste',
          email: 'jose.lucas@amde.example.org',
          grauSuporte: 'NIVEL_2',
          numCelular: '(15) *****-0011',
        },
      ],
      pagina: 0,
      tamanho: 10,
      totalItens: 1,
      totalPaginas: 1,
    });

    const fichaMock = vi.mocked(pacienteService.consultarFichaPaciente);
    fichaMock.mockResolvedValueOnce(FICHA_EXEMPLO);
    fichaMock.mockResolvedValueOnce({
      ...FICHA_EXEMPLO,
      numCelular: '(15) 99999-0011',
      celularMascarado: false,
    });

    const atualizarMock = vi.mocked(pacienteService.atualizarDadosClinicos);
    atualizarMock.mockResolvedValueOnce({
      ...FICHA_EXEMPLO,
      grauSuporte: 'NIVEL_3',
      descricao: 'Notas atualizadas.',
    });

    render(
      <MemoryRouter>
        <PacientesPage />
      </MemoryRouter>
    );

    const abrirBtn = await screen.findByRole('button', { name: /Abrir prontuário de José Lucas Teste/i });
    await user.click(abrirBtn);

    expect(await screen.findByText('Prontuário Digital do Paciente')).toBeInTheDocument();
    expect(screen.getByText('Exame Audiológico')).toBeInTheDocument();

    await user.click(screen.getByRole('tab', { name: /Laudos/i }));
    expect(screen.getByText('Laudo Atualizado')).toBeInTheDocument();
    expect(screen.getByText('Substituído')).toBeInTheDocument();

    await user.click(screen.getByRole('tab', { name: /Receituário/i }));
    expect(screen.getByText('Nenhum documento nesta categoria')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /Revelar celular/i }));
    expect(await screen.findByText('(15) 99999-0011')).toBeInTheDocument();

    const textarea = screen.getByLabelText('Descrição clínica');
    fireEvent.change(textarea, { target: { value: 'a'.repeat(5001) } });
    fireEvent.blur(textarea);
    expect(await screen.findByText('Descrição clínica deve ter no máximo 5000 caracteres')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Salvar Notas/i })).toBeDisabled();

    const dialog = screen.getByRole('dialog');
    fireEvent.change(textarea, { target: { value: 'Notas atualizadas.' } });
    await user.selectOptions(within(dialog).getByLabelText(/Grau de suporte/i), 'NIVEL_3');
    await user.click(within(dialog).getByRole('button', { name: /Salvar Notas/i }));

    await waitFor(() => {
      expect(atualizarMock).toHaveBeenCalledWith(12, {
        grauSuporte: 'NIVEL_3',
        descricao: 'Notas atualizadas.',
      });
    });
    expect(await screen.findByText('Notas clínicas atualizadas com sucesso.')).toBeInTheDocument();
  });

  it('exibe MeuProntuarioPage em modo somente leitura com celular mascarado sem opção de revelar ou editar', async () => {
    vi.mocked(pacienteService.consultarMeuProntuario).mockResolvedValueOnce(FICHA_EXEMPLO);

    render(
      <MemoryRouter>
        <MeuProntuarioPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('José Lucas Teste')).toBeInTheDocument();
    expect(screen.getByText('(15) *****-0011')).toBeInTheDocument();
    expect(screen.getByText('Acompanhamento fonoaudiológico.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Revelar celular/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Salvar Notas/i })).not.toBeInTheDocument();
  });
});
