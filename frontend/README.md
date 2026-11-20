# Frontend — AMDE Digital

Interface web SPA do sistema **AMDE Digital**, construída com **React 18**, **TypeScript**, **Vite**, **Tailwind CSS** (configurado exclusivamente via [`design/tokens.json`](../design/tokens.json)), **Lucide Icons**, **Axios** e testes automatizados com **Vitest** + **Testing Library**.

## Rotas e Fluxos Entregues (SPEC-001 a SPEC-006)

- **Perfis atendidos:** `ADMINISTRADOR`, `MEDICO` e `PACIENTE`.
- **Rotas implementadas:**
  - `/login` (`LoginPage` — `SPEC-001`): autenticação JWT stateless com validação inline (`onBlur` e envio), submissão via `Enter` e redirecionamento por perfil (`/dashboard` para `ADMINISTRADOR`; `/agendamentos` para `MEDICO` e `PACIENTE`).
  - `/dashboard` (`DashboardPage` — `SPEC-006`, exclusivo `ADMINISTRADOR`): 4 cards de indicadores (`Total de Consultas`, `Consultas Hoje`, `Total de Pacientes`, `Profissionais Ativos`), filtros por período e profissional e 2 gráficos analíticos (`Consultas ao Longo do Tempo` e `Consultas por Especialidade`) acompanhados de tabelas HTML semânticas acessíveis.
  - `/usuarios` (`UsuariosPage` — `SPEC-001`, exclusivo `ADMINISTRADOR`): totalizadores por perfil, busca textual, filtro por perfil, ordenação (`ASC`/`DESC`), paginação, criação/edição com especialização 1:1 (`Funcionario` / `Paciente`), ação **Revelar celular** (`RN28`) e inativação lógica com confirmação em modal.
  - `/pacientes` (`PacientesPage` — `SPEC-002` e `SPEC-003`, `ADMINISTRADOR` e `MEDICO`): listagem paginada de pacientes TEA, filtro por grau de suporte, ficha clínica com edição de grau/notas, histórico categorizado de documentos (`Exames`, `Laudos`, `Receituario`, `Arquivado`), upload/substituição/reclassificação/download/exclusão de anexos PDF (< 10 MB).
  - `/meu-prontuario` (`MeuProntuarioPage` — `SPEC-002` e `SPEC-003`, exclusivo `PACIENTE`): visualização somente leitura da própria ficha e download de documentos vigentes.
  - `/agendamentos` (`AgendamentosPage` — `SPEC-004`, todos os perfis com escopo por perfil): sessões individuais e em grupo com verificação de conflito de horário, horários no fuso `America/Sao_Paulo`, registro de presenças, conclusão e cancelamento sem exclusão física.

## Pré-requisitos

- Node.js 18+ e npm 9+
- Backend Spring Boot em execução (padrão `http://localhost:8080`)

## Configuração e Comandos

Copie `.env.example` para `.env` e ajuste o endereço da API pública (sem segredos):

```text
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME="AMDE Digital"
VITE_ENVIRONMENT=development
```

Comandos reais definidos em [`package.json`](package.json):

```bash
npm install        # Instala dependências React 18, Vite, TypeScript, Tailwind e Vitest
npm run dev        # Inicia servidor de desenvolvimento Vite (http://localhost:5173)
npm run build      # Executa checagem TypeScript (tsc) e gera bundle de produção (vite build)
npm run preview    # Pré-visualiza o bundle de produção localmente
npm test           # Executa a suíte de testes unitários e de interface (vitest run)
```

## Estados de Interface e Acessibilidade

- **Estados obrigatórios:** `Skeleton` durante carregamento, estado vazio com texto orientativo, alerta de erro com ação de recarregar e estado de sucesso.
- **WCAG 2.1 AA e LGPD:** rótulos associados (`htmlFor`), foco visível, tabelas alternativas para gráficos, fechamento de modais por `Esc` e mascaramento padrão de telefone nas listagens.

## Referências

- [Especificações SDD](../.specify/specs/)
- [Diretrizes de Design System](../.specify/memory/design-system.md)
- [Tokens Visuais](../design/tokens.json)
