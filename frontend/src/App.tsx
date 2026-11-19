import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from '@/components/layout/AppLayout';
import { ProtectedRoute } from '@/components/layout/ProtectedRoute';
import { AuthProvider } from '@/contexts/AuthContext';
import { AgendamentosPage } from '@/pages/AgendamentosPage';
import { DashboardPage } from '@/pages/DashboardPage';
import { LoginPage } from '@/pages/LoginPage';
import { MeuProntuarioPage } from '@/pages/MeuProntuarioPage';
import { PacientesPage } from '@/pages/PacientesPage';
import { UsuariosPage } from '@/pages/UsuariosPage';

export function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route element={<ProtectedRoute perfisPermitidos={['ADMINISTRADOR']} />}>
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/usuarios" element={<UsuariosPage />} />
              </Route>

              <Route element={<ProtectedRoute perfisPermitidos={['ADMINISTRADOR', 'MEDICO']} />}>
                <Route path="/pacientes" element={<PacientesPage />} />
              </Route>

              <Route element={<ProtectedRoute perfisPermitidos={['PACIENTE']} />}>
                <Route path="/meu-prontuario" element={<MeuProntuarioPage />} />
              </Route>

              <Route path="/agendamentos" element={<AgendamentosPage />} />
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
