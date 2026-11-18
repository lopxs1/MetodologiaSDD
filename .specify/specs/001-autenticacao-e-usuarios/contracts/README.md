# Contratos de Integração — SPEC-001

> **ID da Spec:** `001-autenticacao-e-usuarios`  
> **Finalidade:** Definir o contrato formal de API REST (OpenAPI 3.0.3) para autenticação JWT e gestão de usuários antes do início do desenvolvimento.

---

## Arquivos de Contrato

- **[`api-contract.yaml`](api-contract.yaml):** Especificação OpenAPI 3.0.3 completa para as rotas:
  - `POST /api/auth/login` (`UC01`–`UC03`)
  - `POST /api/usuarios/registro` (`UC04`)
  - `GET /api/usuarios` (`UC05`)
  - `GET /api/usuarios/{id}` (`UC05`)
  - `PUT /api/usuarios/{id}` (`UC06`)
  - `DELETE /api/usuarios/{id}` (`UC07`)
- Use este contrato como base única da verdade para:
  - Desenvolvimento desacoplado entre Front-end (`frontend/`), Mobile (`mobile/`) e Back-end (`backend/`).
  - Validação de DTOs, status HTTP (`200`, `201`, `204`, `400`, `401`, `403`, `404`, `409`), testes de contrato e geração de mocks.
