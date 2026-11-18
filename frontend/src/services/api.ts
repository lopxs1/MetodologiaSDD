import axios, { AxiosError } from 'axios';
import type { ErroResponse, ErroValidacaoResponse, LoginResponse } from '@/types/usuario';

export const SESSAO_STORAGE_KEY = 'amde_digital_sessao';

let unauthorizedHandler: (() => void) | null = null;

export function registrarHandlerNaoAutorizado(handler: (() => void) | null): void {
  unauthorizedHandler = handler;
}

export function obterSessaoArmazenada(): LoginResponse | null {
  try {
    const raw = sessionStorage.getItem(SESSAO_STORAGE_KEY);
    if (!raw) {
      return null;
    }
    const parsed = JSON.parse(raw) as LoginResponse;
    if (!parsed?.token || !parsed?.expiraEm || !parsed?.usuario) {
      return null;
    }
    if (Date.now() >= new Date(parsed.expiraEm).getTime()) {
      sessionStorage.removeItem(SESSAO_STORAGE_KEY);
      return null;
    }
    return parsed;
  } catch {
    sessionStorage.removeItem(SESSAO_STORAGE_KEY);
    return null;
  }
}

export function salvarSessaoArmazenada(sessao: LoginResponse): void {
  sessionStorage.setItem(SESSAO_STORAGE_KEY, JSON.stringify(sessao));
}

export function limparSessaoArmazenada(): void {
  sessionStorage.removeItem(SESSAO_STORAGE_KEY);
}

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const sessao = obterSessaoArmazenada();
  if (sessao?.token) {
    config.headers.Authorization = `Bearer ${sessao.token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErroResponse | ErroValidacaoResponse>) => {
    const status = error.response?.status;
    const requestUrl = error.config?.url || '';
    const isLoginEndpoint = requestUrl.includes('/api/auth/login');

    if (status === 401 && !isLoginEndpoint) {
      limparSessaoArmazenada();
      unauthorizedHandler?.();
      if (window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }

    return Promise.reject(error);
  }
);

export function extrairErroApi(error: unknown): {
  status?: number;
  mensagem: string;
  errosCampo: Record<string, string>;
} {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status;
    const data = error.response?.data as (ErroResponse & Partial<ErroValidacaoResponse>) | undefined;
    const errosCampo: Record<string, string> = {};
    if (Array.isArray(data?.erros)) {
      for (const item of data.erros) {
        if (item.campo && item.mensagem) {
          errosCampo[item.campo] = item.mensagem;
        }
      }
    }
    return {
      status,
      mensagem:
        data?.mensagem ||
        (error.code === 'ERR_NETWORK'
          ? 'Não foi possível conectar ao servidor. Tente novamente.'
          : 'Ocorreu um erro ao processar a solicitação.'),
      errosCampo,
    };
  }
  return {
    mensagem: 'Ocorreu um erro inesperado.',
    errosCampo: {},
  };
}
