# Frontend — AMDE Digital

Interface web do sistema **AMDE Digital**, construída com React 18, TypeScript, Vite e Tailwind CSS configurado a partir de [`design/tokens.json`](../design/tokens.json).

## Responsabilidade e Fluxos (Incremento 1 — SPEC-001)

- **Perfis atendidos:** `ADMINISTRADOR`, `MEDICO` e `PACIENTE`.
- **Rotas implementadas:**
  - `/login` (`LoginPage`): autenticação JWT stateless com validação inline (`onBlur` e envio), submissão via `Enter` e redirecionamento por perfil.
  - `/usuarios` (`UsuariosPage`, exclusivo `ADMINISTRADOR`): 4 cards totalizadores, busca por nome/e-mail, filtro por perfil, ordenação por colunas (`ASC`/`DESC`), paginação, modal de cadastro/edição com especialização 1:1 (`Funcionario` / `Paciente`), modal de detalhes com ação **Revelar celular** (`RN28`) e confirmação de inativação lógica com backdrop e suporte à tecla `Esc`.
  - `/dashboard`, `/pacientes` e `/agendamentos`: rotas protegidas por RBAC com placeholders estruturais para os incrementos seguintes.

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

Comandos definidos em `package.json`:

```text
npm install
npm run dev
npm run build
npm test
```

## Estados de Interface e Acessibilidade

- **Estados obrigatórios:** `Skeleton` durante carregamento, estado vazio com orientação, alerta de erro com botão **Tentar novamente** e listagem em sucesso.
- **WCAG 2.1 AA e LGPD:** rótulos associados (`htmlFor`), foco visível, fechamento de modais por `Esc` e mascaramento padrão do celular na listagem e no detalhe.

## Referências

- [Especificação SPEC-001](../.specify/specs/001-autenticacao-e-usuarios/spec.md)
- [Diretrizes de Design System](../.specify/memory/design-system.md)
- [Tokens Visuais](../design/tokens.json)
