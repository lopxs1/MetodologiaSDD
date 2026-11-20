# Contratos de Integração — SPEC-003

> **ID da Spec:** `003-prontuarios-e-anexos`  
> **Finalidade:** Definir o contrato formal de API REST (OpenAPI 3.0.3) para upload de PDFs, substituição com preservação de histórico, reclassificação/arquivamento, download autenticado e exclusão auditada.

---

## Arquivos de Contrato

- **[`api-contract.yaml`](api-contract.yaml):** Especificação OpenAPI 3.0.3 completa para as rotas:
  - `POST /api/prontuarios/upload` (`UC10`, `UC13`)
  - `POST /api/prontuarios/{id}/substituir` (`UC10`, `UC13`)
  - `PATCH /api/prontuarios/{id}/categoria` (`RN36`)
  - `GET /api/prontuarios/{id}/download` (`UC11`)
  - `DELETE /api/prontuarios/{id}` (`UC12`)
- Use este contrato como base para:
  - Desenvolvimento desacoplado entre Front-end (`frontend/`), Mobile (`mobile/`) e Back-end (`backend/`).
  - Testes de integração multipart, validação de códigos HTTP (`200`, `201`, `204`, `400`, `401`, `403`, `404`, `409`, `413`, `415`, `500`) e geração de mocks.
