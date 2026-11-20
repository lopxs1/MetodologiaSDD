import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { CalendarDays, FileHeart, LayoutDashboard, LogOut, Menu, UserCheck, Users, X } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import type { NivelAcesso } from '@/types/usuario';

interface NavItem {
  label: string;
  to: string;
  icon: typeof Users;
  perfis: NivelAcesso[];
}

const NAV_ITEMS: NavItem[] = [
  {
    label: 'Visão Geral / Dashboard',
    to: '/dashboard',
    icon: LayoutDashboard,
    perfis: ['ADMINISTRADOR'],
  },
  {
    label: 'Pacientes',
    to: '/pacientes',
    icon: UserCheck,
    perfis: ['ADMINISTRADOR', 'MEDICO'],
  },
  {
    label: 'Meu Prontuário',
    to: '/meu-prontuario',
    icon: FileHeart,
    perfis: ['PACIENTE'],
  },
  {
    label: 'Agendamentos',
    to: '/agendamentos',
    icon: CalendarDays,
    perfis: ['ADMINISTRADOR', 'MEDICO', 'PACIENTE'],
  },
  {
    label: 'Usuários',
    to: '/usuarios',
    icon: Users,
    perfis: ['ADMINISTRADOR'],
  },
];

const ROTULO_PERFIL: Record<NivelAcesso, string> = {
  ADMINISTRADOR: 'Administrador',
  MEDICO: 'Médico / Funcionário',
  PACIENTE: 'Paciente',
};

const ESTILO_BADGE_PERFIL: Record<NivelAcesso, { container: string; dot: string }> = {
  ADMINISTRADOR: {
    container: 'bg-status-danger-bg border-role-admin-badge text-neutral-text-primary',
    dot: 'bg-role-admin-badge',
  },
  MEDICO: {
    container: 'bg-status-info-bg border-role-medico-badge text-neutral-text-primary',
    dot: 'bg-role-medico-badge',
  },
  PACIENTE: {
    container: 'bg-status-success-bg border-role-paciente-badge text-neutral-text-primary',
    dot: 'bg-role-paciente-badge',
  },
};

export function AppLayout() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const [menuAberto, setMenuAberto] = useState(false);

  if (!usuario) {
    return null;
  }

  const itensVisiveis = NAV_ITEMS.filter((item) => item.perfis.includes(usuario.nivelAcesso));
  const badgePerfil = ESTILO_BADGE_PERFIL[usuario.nivelAcesso];

  const handleSair = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const conteudoSidebar = (
    <div className="flex h-full flex-col bg-brand-primary text-neutral-surface">
      <div className="flex items-center justify-between px-lg py-lg border-b border-brand-primary-hover">
        <div>
          <span className="text-lg font-bold tracking-tight">AMDE Digital</span>
          <p className="text-xs text-neutral-border">Gestão Clínica</p>
        </div>
        <button
          type="button"
          onClick={() => setMenuAberto(false)}
          className="lg:hidden rounded-md p-1 text-neutral-surface hover:bg-brand-primary-hover"
          aria-label="Fechar menu"
        >
          <X className="h-5 w-5" aria-hidden="true" />
        </button>
      </div>

      <nav aria-label="Menu principal" className="flex-1 px-sm py-md space-y-1">
        {itensVisiveis.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              onClick={() => setMenuAberto(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-brand-primary-hover text-neutral-surface shadow-sm'
                    : 'text-neutral-border hover:bg-brand-primary-hover hover:text-neutral-surface'
                }`
              }
            >
              <Icon className="h-4 w-4 shrink-0" aria-hidden="true" />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div className="p-sm border-t border-brand-primary-hover">
        <button
          type="button"
          onClick={handleSair}
          className="flex w-full items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium text-neutral-border hover:bg-brand-primary-hover hover:text-neutral-surface transition-colors"
        >
          <LogOut className="h-4 w-4 shrink-0" aria-hidden="true" />
          <span>Sair</span>
        </button>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen flex bg-neutral-background">
      <aside className="hidden lg:block lg:w-64 lg:shrink-0">{conteudoSidebar}</aside>

      {menuAberto && (
        <div className="fixed inset-0 z-40 flex lg:hidden" role="dialog" aria-modal="true" aria-label="Navegação">
          <div
            className="fixed inset-0 bg-neutral-text-primary/50"
            onClick={() => setMenuAberto(false)}
            aria-hidden="true"
          />
          <aside className="relative z-50 w-64 max-w-full">{conteudoSidebar}</aside>
        </div>
      )}

      <div className="flex-1 flex flex-col min-w-0">
        <header className="bg-neutral-surface border-b border-neutral-border px-lg py-md flex items-center justify-between gap-md">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => setMenuAberto(true)}
              className="lg:hidden rounded-md p-2 text-neutral-text-primary hover:bg-neutral-background"
              aria-label="Abrir menu"
            >
              <Menu className="h-5 w-5" aria-hidden="true" />
            </button>
            <span className="text-base font-semibold text-brand-primary">AMDE Administração</span>
          </div>

          <div className="flex items-center gap-3">
            <div className="text-right hidden sm:block">
              <p className="text-sm font-medium text-neutral-text-primary">{usuario.nome}</p>
              <p className="text-xs text-neutral-text-secondary">{usuario.email}</p>
            </div>
            <span
              className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-medium ${badgePerfil.container}`}
            >
              <span className={`h-2 w-2 rounded-full ${badgePerfil.dot}`} aria-hidden="true" />
              {ROTULO_PERFIL[usuario.nivelAcesso]}
            </span>
            <button
              type="button"
              onClick={handleSair}
              className="inline-flex items-center gap-1.5 rounded-md border border-neutral-border bg-neutral-surface px-3 py-1.5 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background transition-colors"
            >
              <LogOut className="h-4 w-4" aria-hidden="true" />
              <span className="hidden sm:inline">Sair</span>
            </button>
          </div>
        </header>

        <main className="flex-1 p-lg">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
