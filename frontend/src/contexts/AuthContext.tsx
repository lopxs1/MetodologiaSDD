import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import {
  limparSessaoArmazenada,
  obterSessaoArmazenada,
  registrarHandlerNaoAutorizado,
  salvarSessaoArmazenada,
} from '@/services/api';
import { usuarioService } from '@/services/usuarioService';
import type { LoginRequest, LoginResponse, NivelAcesso, UsuarioSessao } from '@/types/usuario';

interface AuthContextValue {
  usuario: UsuarioSessao | null;
  token: string | null;
  autenticado: boolean;
  login: (credenciais: LoginRequest) => Promise<UsuarioSessao>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function rotaInicialPorPerfil(nivelAcesso: NivelAcesso): string {
  return nivelAcesso === 'ADMINISTRADOR' ? '/dashboard' : '/agendamentos';
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<LoginResponse | null>(() => obterSessaoArmazenada());

  const logout = useCallback(() => {
    limparSessaoArmazenada();
    setSessao(null);
  }, []);

  useEffect(() => {
    registrarHandlerNaoAutorizado(() => setSessao(null));
    return () => registrarHandlerNaoAutorizado(null);
  }, []);

  useEffect(() => {
    if (!sessao?.expiraEm) {
      return;
    }
    const restanteMs = new Date(sessao.expiraEm).getTime() - Date.now();
    if (restanteMs <= 0) {
      logout();
      return;
    }
    const timer = window.setTimeout(() => {
      logout();
    }, restanteMs);
    return () => window.clearTimeout(timer);
  }, [sessao, logout]);

  const login = useCallback(async (credenciais: LoginRequest): Promise<UsuarioSessao> => {
    const resposta = await usuarioService.login(credenciais);
    salvarSessaoArmazenada(resposta);
    setSessao(resposta);
    return resposta.usuario;
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      usuario: sessao?.usuario ?? null,
      token: sessao?.token ?? null,
      autenticado: Boolean(sessao?.token && sessao?.usuario),
      login,
      logout,
    }),
    [sessao, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth deve ser utilizado dentro de AuthProvider');
  }
  return context;
}
