export type NivelAcesso = 'ADMINISTRADOR' | 'MEDICO' | 'PACIENTE';

export type GrauSuporte = 'NIVEL_1' | 'NIVEL_2' | 'NIVEL_3';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface UsuarioSessao {
  id: number;
  nome: string;
  email: string;
  nivelAcesso: NivelAcesso;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEm: string;
  usuario: UsuarioSessao;
}

export interface FuncionarioInput {
  cargo: string;
  crm?: string | null;
}

export interface PacienteInput {
  grauSuporte: GrauSuporte;
  descricao?: string | null;
}

export interface CadastrarUsuarioRequest {
  nome: string;
  email: string;
  senha: string;
  nivelAcesso: NivelAcesso;
  dataNascimento?: string | null;
  numCelular?: string | null;
  pais?: string | null;
  estado?: string | null;
  cidade?: string | null;
  rua?: string | null;
  funcionario?: FuncionarioInput | null;
  paciente?: PacienteInput | null;
}

export interface AtualizarUsuarioRequest {
  nome: string;
  email: string;
  senha?: string | null;
  nivelAcesso?: NivelAcesso | null;
  dataNascimento?: string | null;
  numCelular?: string | null;
  pais?: string | null;
  estado?: string | null;
  cidade?: string | null;
  rua?: string | null;
  funcionario?: FuncionarioInput | null;
  paciente?: PacienteInput | null;
}

export interface FuncionarioResumo {
  id: number;
  cargo: string;
  crm: string | null;
}

export interface PacienteResumo {
  id: number;
  grauSuporte: GrauSuporte;
  descricao: string | null;
}

export interface UsuarioResumo {
  id: number;
  nome: string;
  email: string;
  nivelAcesso: NivelAcesso;
  numCelular: string | null;
  criadoEm: string;
}

export interface UsuarioDetalhe {
  id: number;
  nome: string;
  email: string;
  nivelAcesso: NivelAcesso;
  dataNascimento: string | null;
  numCelular: string | null;
  celularMascarado: boolean;
  pais: string | null;
  estado: string | null;
  cidade: string | null;
  rua: string | null;
  funcionario: FuncionarioResumo | null;
  paciente: PacienteResumo | null;
  criadoEm: string;
  atualizadoEm: string;
}

export interface TotalizadoresPerfil {
  total: number;
  administradores: number;
  medicos: number;
  pacientes: number;
}

export interface ListaUsuariosResponse {
  itens: UsuarioResumo[];
  pagina: number;
  tamanho: number;
  totalItens: number;
  totalPaginas: number;
  totalizadores: TotalizadoresPerfil;
}

export type OrdenacaoUsuarioCampo = 'nome' | 'email' | 'nivelAcesso' | 'criadoEm';
export type DirecaoOrdenacao = 'ASC' | 'DESC';

export interface ListarUsuariosParams {
  termo?: string;
  nivelAcesso?: NivelAcesso | '';
  page?: number;
  size?: number;
  sort?: OrdenacaoUsuarioCampo;
  direction?: DirecaoOrdenacao;
}

export interface CampoErro {
  campo: string;
  mensagem: string;
}

export interface ErroResponse {
  status: number;
  mensagem: string;
  timestamp: string;
}

export interface ErroValidacaoResponse {
  status: number;
  mensagem: string;
  erros: CampoErro[];
  timestamp: string;
}
