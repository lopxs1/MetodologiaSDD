# Contratos de Integração — SPEC-004

> **ID da Spec:** `004-agendamentos-e-consultas`  
> **Finalidade:** Definir o contrato formal de API REST (OpenAPI 3.0.3) para criação, listagem, consulta detalhada, edição, registro de presença, conclusão e cancelamento de agendamentos clínicos.

---

## Arquivos de Contrato

- **[`api-contract.yaml`](api-contract.yaml):** Especificação OpenAPI 3.0.3 completa para as rotas:
  - `POST /api/agendamentos` (`UC14`, `UC18`)
  - `GET /api/agendamentos` (`UC19`)
  - `GET /api/agendamentos/{id}` (`UC15`)
  - `PUT /api/agendamentos/{id}` (`UC16`, `UC18`)
  - `PATCH /api/agendamentos/{id}/presenca` (`UC16`)
  - `PATCH /api/agendamentos/{id}/concluir` (`UC16`)
  - `DELETE /api/agendamentos/{id}` (`UC17`)
- Use este contrato como base para:
  - Desenvolvimento desacoplado entre Front-end (`frontend/`), Mobile (`mobile/`) e Back-end (`backend/`).
  - Testes de integração, concorrência (`409 Conflict`), minimização LGPD para `PACIENTE` e geração de mocks.
