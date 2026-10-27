# Checklist de Tarefas: [Nome da Funcionalidade]

> **ID da Spec:** `SPEC-XXX`  
> **Status Geral:** `0 / 10 Concluídas`  
> **Responsáveis:** [Nome dos Desenvolvedores]  
> **Origem dos Requisitos:** [spec.md](spec.md) | **Plano Técnico:** [plan.md](plan.md) | **Contratos:** [contracts/](contracts/)

---

## 📌 Fase 1: Especificação e Contratos
- [ ] **T01.1:** Elaborar e revisar requisitos e critérios de aceite em `spec.md`.
- [ ] **T01.2:** Definir contratos de integração e schemas em `contracts/`.
- [ ] **T01.3:** Validar especificação com a equipe ou orientador.

---

## 💾 Fase 2: Banco de Dados e Migrações
- [ ] **T02.1:** Implementar script de migração UP conforme `data-model.md`.
- [ ] **T02.2:** Testar script de reversão DOWN (Rollback).
- [ ] **T02.3:** Criar seeds sintéticos de teste.

---

## ⚙️ Fase 3: Backend e Regras de Negócio
- [ ] **T03.1:** Implementar DTOs e validações de esquema.
- [ ] **T03.2:** Implementar camada de serviço e regras de negócio.
- [ ] **T03.3:** Implementar endpoints REST conforme contratos OpenAPI.

---

## 🖥️ Fase 4: Frontend / Interface
- [ ] **T04.1:** Construir componentes de tela e formulários.
- [ ] **T04.2:** Integrar chamadas de API tratando estados de loading e erro.

---

## 🧪 Fase 5: Testes e Validação
- [ ] **T05.1:** Implementar testes unitários para regras de negócio.
- [ ] **T05.2:** Implementar testes de integração para endpoints da API.
- [ ] **T05.3:** Validar critérios de aceite (BDD) em ambiente de teste.

---

## 🏁 Fase 6: Conclusão e Revisão
- [ ] **T06.1:** Atualizar [`.specify/memory/domain-model.md`](../../memory/domain-model.md) com as novas entidades consolidadas.
- [ ] **T06.2:** Realizar Code Review e validar checklist da Definition of Done.
