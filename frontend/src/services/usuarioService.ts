import { api } from '@/services/api';
import type {
  AtualizarUsuarioRequest,
  CadastrarUsuarioRequest,
  ListaUsuariosResponse,
  ListarUsuariosParams,
  LoginRequest,
  LoginResponse,
  UsuarioDetalhe,
} from '@/types/usuario';

export const usuarioService = {
  async login(credenciais: LoginRequest): Promise<LoginResponse> {
    const { data } = await api.post<LoginResponse>('/api/auth/login', credenciais);
    return data;
  },

  async listarUsuarios(params: ListarUsuariosParams = {}): Promise<ListaUsuariosResponse> {
    const query: Record<string, string | number> = {
      page: params.page ?? 0,
      size: params.size ?? 10,
      sort: params.sort ?? 'nome',
      direction: params.direction ?? 'ASC',
    };
    if (params.termo && params.termo.trim().length > 0) {
      query.termo = params.termo.trim();
    }
    if (params.nivelAcesso) {
      query.nivelAcesso = params.nivelAcesso;
    }

    const { data } = await api.get<ListaUsuariosResponse>('/api/usuarios', { params: query });
    return data;
  },

  async consultarUsuarioPorId(id: number, revelarCelular = false): Promise<UsuarioDetalhe> {
    const { data } = await api.get<UsuarioDetalhe>(`/api/usuarios/${id}`, {
      params: { revelarCelular },
    });
    return data;
  },

  async cadastrarUsuario(payload: CadastrarUsuarioRequest): Promise<UsuarioDetalhe> {
    const { data } = await api.post<UsuarioDetalhe>('/api/usuarios/registro', payload);
    return data;
  },

  async atualizarUsuario(id: number, payload: AtualizarUsuarioRequest): Promise<UsuarioDetalhe> {
    const { data } = await api.put<UsuarioDetalhe>(`/api/usuarios/${id}`, payload);
    return data;
  },

  async excluirUsuario(id: number): Promise<void> {
    await api.delete(`/api/usuarios/${id}`);
  },
};
