# Contratos de Integração — SPEC-006

> **ID da Spec:** `006-dashboard-gerencial`  
> **Finalidade:** Definir o contrato formal de API REST (OpenAPI 3.0.3) para o painel analítico exclusivo do Administrador.

---

## Arquivos de Contrato

- **[`api-contract.yaml`](api-contract.yaml):** Especificação OpenAPI 3.0.3 para a rota:
  - `GET /api/dashboard` (`UC21` — 4 contadores agregados, 2 séries analíticas e filtros opcionais de período e profissional)
- Use este contrato como base para:
  - Desenvolvimento desacoplado entre Front-end (`frontend/`) e Back-end (`backend/`).
  - Testes de integração, precisão de somatórios (`RNF-07.5`), restrição exclusiva ao perfil `ADMINISTRADOR` (`403`) e geração de mocks.
