import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from '@/contexts/AuthContext';
import { LoginPage } from '@/pages/LoginPage';
import { UsuariosPage } from '@/pages/UsuariosPage';
import { usuarioService } from '@/services/usuarioService';

vi.mock('@/services/usuarioService', () => ({
  usuarioService: {
    login: vi.fn(),
    listarUsuarios: vi.fn(),
    consultarUsuarioPorId: vi.fn(),
    cadastrarUsuario: vi.fn(),
    atualizarUsuario: vi.fn(),
    excluirUsuario: vi.fn(),
  },
}));

describe('Incremento 1 - LoginPage e UsuariosPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
  });

  it('valida formato de e-mail onBlur na LoginPage', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/login']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    const emailInput = screen.getByLabelText(/E-mail/i);
    await user.type(emailInput, 'invalido');
    fireEvent.blur(emailInput);

    expect(await screen.findByText('Formato de e-mail inválido')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Entrar' })).toBeDisabled();
  });

  it('exibe erro 401 na LoginPage e redireciona Administrador ao dashboard quando autentica', async () => {
    const user = userEvent.setup();
    const loginMock = vi.mocked(usuarioService.login);
    loginMock.mockRejectedValueOnce({
      isAxiosError: true,
      response: { status: 401, data: { mensagem: 'Credenciais inválidas' } },
    });

    render(
      <MemoryRouter initialEntries={['/login']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/dashboard" element={<div>Painel Admin</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    await user.type(screen.getByLabelText(/E-mail/i), 'admin@amde.example.org');
    await user.type(screen.getByLabelText(/^Senha/i), 'SenhaErrada123');
    await user.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(await screen.findByRole('alert')).toBeInTheDocument();

    loginMock.mockResolvedValueOnce({
      token: 'jwt-token',
      tipo: 'Bearer',
      expiraEm: new Date(Date.now() + 3600_000).toISOString(),
      usuario: {
        id: 1,
        nome: 'Admin Teste',
        email: 'admin@amde.example.org',
        nivelAcesso: 'ADMINISTRADOR',
      },
    });

    await user.click(screen.getByRole('button', { name: 'Entrar' }));
    expect(await screen.findByText('Painel Admin')).toBeInTheDocument();
  });

  it('renderiza estados Loading, Erro com recarregar, Vazio e Sucesso na UsuariosPage', async () => {
    const listarMock = vi.mocked(usuarioService.listarUsuarios);

    listarMock.mockImplementationOnce(() => new Promise(() => {}));
    const { unmount } = render(
      <MemoryRouter>
        <UsuariosPage />
      </MemoryRouter>
    );
    expect(screen.getByLabelText('Carregando usuários')).toBeInTheDocument();
    unmount();

    listarMock.mockRejectedValueOnce(new Error('Falha de rede'));
    listarMock.mockResolvedValueOnce({
      itens: [],
      pagina: 0,
      tamanho: 10,
      totalItens: 0,
      totalPaginas: 0,
      totalizadores: { total: 0, administradores: 0, medicos: 0, pacientes: 0 },
    });

    render(
      <MemoryRouter>
        <UsuariosPage />
      </MemoryRouter>
    );

    expect(await screen.findByRole('alert')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /Tentar novamente/i }));
    expect(await screen.findByText('Nenhum usuário encontrado')).toBeInTheDocument();

    listarMock.mockResolvedValueOnce({
      itens: [
        {
          id: 5,
          nome: 'José Lucas Teste',
          email: 'jose@amde.example.org',
          nivelAcesso: 'PACIENTE',
          numCelular: '(15) *****-0011',
          criadoEm: '2026-11-18T10:00:00-03:00',
        },
      ],
      pagina: 0,
      tamanho: 10,
      totalItens: 1,
      totalPaginas: 1,
      totalizadores: { total: 1, administradores: 0, medicos: 0, pacientes: 1 },
    });

    fireEvent.change(screen.getByLabelText('Filtrar por perfil'), { target: { value: 'PACIENTE' } });

    await waitFor(() => {
      expect(screen.getByText('José Lucas Teste')).toBeInTheDocument();
    });
    expect(screen.getByText('(15) *****-0011')).toBeInTheDocument();
  });
});

