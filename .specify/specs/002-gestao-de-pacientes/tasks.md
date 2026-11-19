# Checklist de Tarefas: Gestão de Pacientes

> **ID da Spec:** `SPEC-002`  
> **Status Geral:** `22 / 24 Concluídas (T05.5 Axe DevTools/benchmark 10k e T06.3 revisão final pendentes)`  
> **Responsáveis:** Gustavo Ferreira Lopes, Helena Melchiades de Faria, Kaiky Saquette Pereira, Vinicius Firmino Oliveira de Souza  
> **Origem dos Requisitos:** [spec.md](spec.md) | **Plano Técnico:** [plan.md](plan.md) | **Modelo de Dados:** [data-model.md](data-model.md) | **Contratos:** [contracts/api-contract.yaml](contracts/api-contract.yaml)

---

## 📌 Fase 1: Especificação e Contratos
- [x] **T01.1:** Revisar requisitos (`RF-03.1` a `RF-03.6`), RNFs (`RNF-03.1` a `RNF-03.5`), regras (`RN04`, `RN29` a `RN33`) e os 11 cenários BDD em [`spec.md`](spec.md).
- [x] **T01.2:** Validar o contrato OpenAPI 3.0.3 em [`contracts/api-contract.yaml`](contracts/api-contract.yaml) para os 4 endpoints (`GET /api/pacientes`, `GET /api/pacientes/meu-prontuario`, `GET /api/pacientes/{id}`, `PUT /api/pacientes/{id}`).
- [x] **T01.3:** Submeter o pacote `002-gestao-de-pacientes` para aprovação da equipe antes da implementação.

---

## 💾 Fase 2: Banco de Dados, Índices e Seeds Sintéticos
- [x] **T02.1:** Validar que `paciente`, `usuario`, `categoria` e `registro_prontuario` e seus índices (`ix_paciente_grau_suporte`, `ix_usuario_nome_trgm`, `ix_registro_prontuario_*`) em [`001_esquema_inicial.sql`](../../../backend/src/main/resources/db/changelog/migrations/001_esquema_inicial.sql) atendem integralmente `UC08` e `UC09`.
- [x] **T02.2:** Criar seed sintético (`data/seeds/002_pacientes_seed_sintetico.sql`) com as 4 categorias oficiais (`Exames`, `Laudos`, `Receituario`, `Arquivado`), pacientes nos 3 graus de suporte (incluindo nomes acentuados para testar `unaccent`) e metadados sintéticos de prontuário.
- [x] **T02.3:** Validar scripts SQL com `sqlfluff lint --dialect postgres` e confirmar **zero linhas** nas 9 verificações estruturais de [`data/verificacao.sql`](../../../data/verificacao.sql).
- [x] **T02.4:** Conferir o inventário `[LGPD: sensível]` para `paciente` e `registro_prontuario` em `data/verificacao.sql`.

---

## ⚙️ Fase 3: Backend e Regras de Negócio (Spring Boot 3 / Java 17)
- [x] **T03.1:** Mapear as entidades `Categoria` e `RegistroProntuario` (somente leitura nesta spec) aderentes a [`AGENTS.md`](../../../AGENTS.md), reutilizando `Usuario` e `Paciente` da `SPEC-001`.
- [x] **T03.2:** Implementar `PacienteRepository` com projeção resumida sem `descricao` (`RNF-03.2`), busca `unaccent`/`lower` por nome ou e-mail com escape literal de curingas (`RN30`), filtro por `grauSuporte` (`RN31`) e ordenação segura (`nome`, `email`, `grauSuporte` `ASC`/`DESC`).
- [x] **T03.3:** Implementar `RegistroProntuarioRepository` retornando metadados ativos (`excluido_em IS NULL`) de um `paciente_id` ordenados por `criado_em DESC`, com cálculo da situação (`VIGENTE`, `SUBSTITUIDO`, `ARQUIVADO`) e paginação por categoria (10 itens por página — `UC09`).
- [x] **T03.4:** Implementar `PacienteService.listar` (`RN29`, `RN30`, `RN31`, `RN33`) com paginação padrão 10 / máximo 50, termo até 100 caracteres e celular sempre mascarado via `CelularMasker`.
- [x] **T03.5:** Implementar `PacienteService.buscarFicha` e `buscarMeuProntuario` (`RN29`, `RN33`, `RNF-03.3`), garantindo bloqueio imediato `403` quando `PACIENTE` solicitar outro identificador, celular sempre mascarado para `PACIENTE` e revelação condicional (`?revelarCelular=true`) apenas para `ADMINISTRADOR` e `MEDICO`.
- [x] **T03.6:** Implementar `PacienteService.atualizarDadosClinicos` (`RN32`) atualizando apenas `grauSuporte` e `descricao` (opcional até 5.000 chars após `trim()`), preservando os dados de `Usuario` e respondendo `404` para paciente inativado.
- [x] **T03.7:** Implementar `PacienteController` com `@PreAuthorize`, validação `@Valid` e tratamento de exceções seguro sem vazar dados clínicos em logs (`RNF-03.2`).

---

## 🖥️ Fase 4: Frontend / Interface Web (React 18 / TypeScript / Tailwind)
- [x] **T04.1:** Configurar `pacienteService.ts` consumindo os endpoints de `/api/pacientes` e integrando os interceptors JWT da `SPEC-001`.
- [x] **T04.2:** Construir `PacientesPage` (`/pacientes`, restrita a `ADMINISTRADOR` e `MEDICO`) com busca por nome/e-mail, filtro por `grauSuporte`, cabeçalhos ordenáveis (`ASC`/`DESC`), paginação na base (`10`/`20`/`50`), máscara de celular e os 4 estados obrigatórios (`RNF-03.4`).
- [x] **T04.3:** Construir `PacienteFichaModal` / `PacienteFichaView` exibindo dados cadastrais somente leitura (com indicação de campo não informado quando opcional omitido), botão **"Revelar celular"** restrito a Admin/Médico (`RN33`) e dados clínicos.
- [x] **T04.4:** Construir `EditarDadosClinicosForm` dentro da ficha (habilitado apenas para `ADMINISTRADOR` e `MEDICO`) com seletor de `grauSuporte`, campo `descricao` (até 5.000 caracteres), validação `onBlur` e botão **"Salvar Notas"**.
- [x] **T04.5:** Construir `HistoricoDocumentosTabs` agrupando metadados nas 4 abas/acordeões (`Exames`, `Laudos`, `Receituário`, `Arquivado`) com contadores, ordenação cronológica decrescente, badges de situação (`Vigente`, `Substituído`, `Arquivado`), paginação de 10 itens por categoria e estado vazio por categoria (`UC09`).
- [x] **T04.6:** Construir a rota `/meu-prontuario` (`PACIENTE`) reutilizando a visualização da ficha em modo somente leitura, com celular sempre mascarado e sem opção de revelação ou edição clínica (`Cenário 6`).

---

## 🧪 Fase 5: Testes e Validação
- [x] **T05.1:** Implementar testes unitários (JUnit 5 + Mockito) para `PacienteService` cobrindo escape de curingas, bloqueio `403` por titularidade, cálculo de situação de documentos, mascaramento/revelação de celular e validação da `RN32`.
- [x] **T05.2:** Implementar testes de integração (Spring Boot Test + PostgreSQL 18 em container Docker) validando busca `unaccent`, `RNF-03.2` (`descricao` ausente da listagem), `404` para paciente inativado e `403` para todas as tentativas indevidas do perfil `PACIENTE` (`RNF-03.3`).
- [x] **T05.3:** Implementar testes de front-end (Vitest + Testing Library) para `PacientesPage`, `HistoricoDocumentosTabs`, `EditarDadosClinicosForm` e `/meu-prontuario`.
- [x] **T05.4:** Validar os **11 cenários BDD** de [`spec.md`](spec.md) em testes automatizados e auditar logs/respostas para garantir zero vazamento de dados clínicos e de contato (`RNF-03.2`).
- [ ] **T05.5:** Validar acessibilidade WCAG 2.1 AA com Axe DevTools (`RNF-03.5`) e desempenho p95 < 500 ms com massa sintética de 10.000 pacientes (`RNF-03.1`). *(Pendente de execução de auditoria Axe DevTools em browser e benchmark de 10.000 registros.)*

---

## 🏁 Fase 6: Conclusão e Revisão
- [x] **T06.1:** Confirmar aderência integral entre código, [`contracts/api-contract.yaml`](contracts/api-contract.yaml), [`data-model.md`](data-model.md) e [`.specify/memory/domain-model.md`](../../memory/domain-model.md).
- [x] **T06.2:** Validar `ddl-auto=validate` e Checklist de Banco de Dados ([`AGENTS.md`](../../../AGENTS.md)).
- [ ] **T06.3:** Realizar Code Review final, validar todos os itens da *Definition of Done* em [`plan.md`](plan.md) e atualizar o status da `SPEC-002` para `Implementado`. *(Aguardando conclusão de T05.5.)*
