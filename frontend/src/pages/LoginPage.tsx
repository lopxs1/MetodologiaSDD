import { useState, type FormEvent } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { AlertCircle, Eye, EyeOff, Lock, Mail } from 'lucide-react';
import { rotaInicialPorPerfil, useAuth } from '@/contexts/AuthContext';
import { extrairErroApi } from '@/services/api';

const EMAIL_REGEX = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

export function LoginPage() {
  const { autenticado, usuario, login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [erros, setErros] = useState<{ email?: string; senha?: string }>({});
  const [erroGeral, setErroGeral] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  if (autenticado && usuario) {
    return <Navigate to={rotaInicialPorPerfil(usuario.nivelAcesso)} replace />;
  }

  const validarEmail = (valor: string): string | undefined => {
    const trimmed = valor.trim();
    if (!trimmed) {
      return 'E-mail é obrigatório';
    }
    if (trimmed.length < 5 || trimmed.length > 254 || !EMAIL_REGEX.test(trimmed)) {
      return 'Formato de e-mail inválido';
    }
    return undefined;
  };

  const validarSenha = (valor: string): string | undefined => {
    if (!valor) {
      return 'Senha é obrigatória';
    }
    return undefined;
  };

  const handleBlurEmail = () => {
    setErros((prev) => ({ ...prev, email: validarEmail(email) }));
  };

  const handleBlurSenha = () => {
    setErros((prev) => ({ ...prev, senha: validarSenha(senha) }));
  };

  const possuiErroPendente = Boolean(erros.email || erros.senha);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setErroGeral(null);

    const erroEmail = validarEmail(email);
    const erroSenha = validarSenha(senha);
    setErros({ email: erroEmail, senha: erroSenha });
    if (erroEmail || erroSenha) {
      return;
    }

    setCarregando(true);
    try {
      const sessao = await login({ email: email.trim(), senha });
      navigate(rotaInicialPorPerfil(sessao.nivelAcesso), { replace: true });
    } catch (error) {
      const { status, mensagem, errosCampo } = extrairErroApi(error);
      if (status === 400 && Object.keys(errosCampo).length > 0) {
        setErros({ email: errosCampo.email, senha: errosCampo.senha });
      } else {
        setErroGeral(mensagem);
      }
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-neutral-background px-md py-xl">
      <div className="w-full max-w-md bg-neutral-surface border border-neutral-border rounded-lg p-xl shadow-sm">
        <div className="mb-lg text-center">
          <div className="mx-auto mb-sm inline-flex h-12 w-12 items-center justify-center rounded-lg bg-brand-primary text-neutral-surface font-bold text-lg">
            AM
          </div>
          <h1 className="text-2xl font-bold text-neutral-text-primary">AMDE Digital</h1>
          <p className="mt-xs text-sm text-neutral-text-secondary">
            Acesse sua conta para gerenciar atendimentos e prontuários
          </p>
        </div>

        {erroGeral && (
          <div
            role="alert"
            className="mb-md flex items-start gap-2 rounded-md border border-status-danger bg-status-danger-bg px-3 py-2.5 text-sm text-status-danger"
          >
            <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" aria-hidden="true" />
            <span>{erroGeral}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate className="space-y-md">
          <div>
            <label htmlFor="login-email" className="block text-sm font-medium text-neutral-text-primary mb-1">
              E-mail <span className="text-status-danger">*</span>
            </label>
            <div className="relative">
              <Mail
                className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-neutral-text-muted"
                aria-hidden="true"
              />
              <input
                id="login-email"
                name="email"
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  if (erros.email) {
                    setErros((prev) => ({ ...prev, email: validarEmail(e.target.value) }));
                  }
                }}
                onBlur={handleBlurEmail}
                aria-invalid={Boolean(erros.email)}
                aria-describedby={erros.email ? 'login-email-erro' : undefined}
                placeholder="nome@amde.org.br"
                className={`w-full rounded-md border pl-9 pr-3 py-2 text-sm text-neutral-text-primary placeholder:text-neutral-text-muted bg-neutral-surface ${
                  erros.email ? 'border-status-danger' : 'border-neutral-border'
                }`}
              />
            </div>
            {erros.email && (
              <p id="login-email-erro" className="mt-1 text-xs text-status-danger">
                {erros.email}
              </p>
            )}
          </div>

          <div>
            <label htmlFor="login-senha" className="block text-sm font-medium text-neutral-text-primary mb-1">
              Senha <span className="text-status-danger">*</span>
            </label>
            <div className="relative">
              <Lock
                className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-neutral-text-muted"
                aria-hidden="true"
              />
              <input
                id="login-senha"
                name="senha"
                type={mostrarSenha ? 'text' : 'password'}
                autoComplete="current-password"
                required
                value={senha}
                onChange={(e) => {
                  setSenha(e.target.value);
                  if (erros.senha) {
                    setErros((prev) => ({ ...prev, senha: validarSenha(e.target.value) }));
                  }
                }}
                onBlur={handleBlurSenha}
                aria-invalid={Boolean(erros.senha)}
                aria-describedby={erros.senha ? 'login-senha-erro' : undefined}
                placeholder="Digite sua senha"
                className={`w-full rounded-md border pl-9 pr-10 py-2 text-sm text-neutral-text-primary placeholder:text-neutral-text-muted bg-neutral-surface ${
                  erros.senha ? 'border-status-danger' : 'border-neutral-border'
                }`}
              />
              <button
                type="button"
                onClick={() => setMostrarSenha((prev) => !prev)}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 rounded p-1 text-neutral-text-muted hover:text-neutral-text-primary"
                aria-label={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
              >
                {mostrarSenha ? (
                  <EyeOff className="h-4 w-4" aria-hidden="true" />
                ) : (
                  <Eye className="h-4 w-4" aria-hidden="true" />
                )}
              </button>
            </div>
            {erros.senha && (
              <p id="login-senha-erro" className="mt-1 text-xs text-status-danger">
                {erros.senha}
              </p>
            )}
          </div>

          <button
            type="submit"
            disabled={carregando || possuiErroPendente}
            className="w-full rounded-md bg-brand-primary px-4 py-2.5 text-sm font-semibold text-neutral-surface hover:bg-brand-primary-hover disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {carregando ? 'Entrando...' : 'Entrar'}
          </button>
        </form>
      </div>
    </div>
  );
}
