# Contratos de Integração — SPEC-005

> **ID da Spec:** `005-auditoria-e-seguranca`  
> **Finalidade:** Formalizar o contrato transversal de auditoria (catálogo de ações, formato minimizado de `recurso`, extração segura de IP de origem e comportamento de falha em operações auditadas).

---

## Arquivos de Contrato

- **[`api-contract.yaml`](api-contract.yaml):** Especificação OpenAPI 3.0.3 transversal (`paths: {}`, pois nenhum perfil consulta ou altera a trilha via aplicação nesta versão conforme `RN17` e `RN48`).
- Define os esquemas internos canônicos (`AcaoAuditoria`, `EventoAuditoriaInterno`, `PadraoRecursoAuditoria`) e o cabeçalho confiável de origem (`X-Forwarded-For`), garantindo rastreabilidade e minimização LGPD entre `SPEC-001`, `SPEC-003` e `SPEC-004`.
