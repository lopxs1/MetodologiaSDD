import { BrowserRouter, Link, Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from '@/components/layout/AppLayout';
import { ProtectedRoute } from '@/components/layout/ProtectedRoute';
import { AuthProvider } from '@/contexts/AuthContext';
import { LoginPage } from '@/pages/LoginPage';
import { UsuariosPage } from '@/pages/UsuariosPage';

function DashboardPlaceholder() {
  return (
    <div className="rounded-lg border border-neutral-border bg-neutral-surface p-xl shadow-sm space-y-sm">
      <h1 className="text-2xl font-bold text-neutral-text-primary">Visão Geral</h1>
      <p className="text-sm text-neutral-text-secondary">
        O painel de indicadores gerenciais será entregue no Incremento 4 (SPEC-006).
      </p>
      <div>
        <Link
          to="/usuarios"
          className="inline-flex items-center rounded-md bg-brand-primary px-4 py-2 text-sm font-medium text-neutral-surface hover:bg-brand-primary-hover transition-colors"
        >
          Ir para Gestão de Usuários
        </Link>
      </div>
    </div>
  );
}

function ModuloEmConstrucao({ titulo, descricao }: { titulo: string; descricao: string }) {
  return (
    <div className="rounded-lg border border-neutral-border bg-neutral-surface p-xl shadow-sm space-y-sm">
      <h1 className="text-2xl font-bold text-neutral-text-primary">{titulo}</h1>
      <p className="text-sm text-neutral-text-secondary">{descricao}</p>
    </div>
  );
}

export function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route element={<ProtectedRoute perfisPermitidos={['ADMINISTRADOR']} />}>
                <Route path="/dashboard" element={<DashboardPlaceholder />} />
                <Route path="/usuarios" element={<UsuariosPage />} />
              </Route>

              <Route element={<ProtectedRoute perfisPermitidos={['ADMINISTRADOR', 'MEDICO']} />}>
                <Route
                  path="/pacientes"
                  element={
                    <ModuloEmConstrucao
                      titulo="Pacientes e Prontuários"
                      descricao="Este módulo clínico será entregue no Incremento 2 (SPEC-002 e SPEC-003)."
                    />
                  }
                />
              </Route>

              <Route
                path="/agendamentos"
                element={
                  <ModuloEmConstrucao
                    titulo="Agendamentos e Consultas"
                    descricao="A agenda clínica e confirmação de presença serão entregues no Incremento 3 (SPEC-004)."
                  />
                }
              />
            </Route>
          </Route>

          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
