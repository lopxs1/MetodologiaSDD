# Contratos de Integração — SPEC-002

> **ID da Spec:** `002-gestao-de-pacientes`  
> **Finalidade:** Definir o contrato formal de API REST (OpenAPI 3.0.3) para listagem de pacientes, consulta de ficha clínica com histórico por categoria, acesso exclusivo do paciente titular e atualização de dados clínicos.

---

## Arquivos de Contrato

- **[`api-contract.yaml`](api-contract.yaml):** Especificação OpenAPI 3.0.3 completa para as rotas:
  - `GET /api/pacientes` (`UC08` — listagem paginada para `ADMINISTRADOR` e `MEDICO`)
  - `GET /api/pacientes/meu-prontuario` (`UC08`, `UC09` — ficha e histórico do próprio `PACIENTE` autenticado)
  - `GET /api/pacientes/{id}` (`UC08`, `UC09` — ficha clínica completa com histórico agrupado por categoria e revelação condicional do celular)
  - `PUT /api/pacientes/{id}` (`UC08` Fluxo Alternativo — atualização de `grauSuporte` e `descricao` clínica por `ADMINISTRADOR` e `MEDICO`)
- Use este contrato como base para:
  - Desenvolvimento desacoplado entre Front-end (`frontend/`), Mobile (`mobile/`) e Back-end (`backend/`).
  - Testes de integração, segurança por perfil (`403` para acesso indevido) e geração de mocks.
