import { Link, Navigate, Outlet, useLocation } from 'react-router-dom';
import { ShieldAlert } from 'lucide-react';
import { rotaInicialPorPerfil, useAuth } from '@/contexts/AuthContext';
import type { NivelAcesso } from '@/types/usuario';

interface ProtectedRouteProps {
  perfisPermitidos?: NivelAcesso[];
}

export function ProtectedRoute({ perfisPermitidos }: ProtectedRouteProps) {
  const { autenticado, usuario } = useAuth();
  const location = useLocation();

  if (!autenticado || !usuario) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (perfisPermitidos && !perfisPermitidos.includes(usuario.nivelAcesso)) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-neutral-background p-lg">
        <div
          role="alert"
          className="max-w-md w-full bg-neutral-surface border border-neutral-border rounded-lg p-xl text-center shadow-sm"
        >
          <div className="mx-auto mb-md flex h-12 w-12 items-center justify-center rounded-full bg-status-danger-bg text-status-danger">
            <ShieldAlert className="h-6 w-6" aria-hidden="true" />
          </div>
          <h1 className="text-xl font-semibold text-neutral-text-primary">Acesso negado</h1>
          <p className="mt-sm text-sm text-neutral-text-secondary">
            Seu perfil de acesso não possui permissão para visualizar esta área.
          </p>
          <Link
            to={rotaInicialPorPerfil(usuario.nivelAcesso)}
            className="mt-lg inline-flex items-center justify-center rounded-md bg-brand-primary px-4 py-2 text-sm font-medium text-neutral-surface hover:bg-brand-primary-hover transition-colors"
          >
            Voltar ao início
          </Link>
        </div>
      </div>
    );
  }

  return <Outlet />;
}
