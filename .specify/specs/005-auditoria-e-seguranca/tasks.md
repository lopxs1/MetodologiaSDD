# Checklist de Tarefas: Auditoria e Segurança

> **ID da Spec:** `SPEC-005`  
> **Status Geral:** `0 / 24 Concluídas`  
> **Responsáveis:** Gustavo Ferreira Lopes, Helena Melchiades de Faria, Kaiky Saquette Pereira, Vinicius Firmino Oliveira de Souza  
> **Origem dos Requisitos:** [spec.md](spec.md) | **Plano Técnico:** [plan.md](plan.md) | **Modelo de Dados:** [data-model.md](data-model.md) | **Contratos:** [contracts/api-contract.yaml](contracts/api-contract.yaml)

---

## 📌 Fase 1: Especificação e Contratos Internos
- [ ] **T01.1:** Revisar requisitos (`RF-06.1` a `RF-06.7`), RNFs (`RNF-06.1` a `RNF-06.5`), regras (`RN16`, `RN44`–`RN48`) e os 11 cenários BDD em [`spec.md`](spec.md).
- [ ] **T01.2:** Validar o contrato transversal em [`contracts/api-contract.yaml`](contracts/api-contract.yaml) com os padrões de `acao`, `recurso` (`RN47`) e `ipOrigem` (`UC20`).
- [ ] **T01.3:** Submeter o pacote `005-auditoria-e-seguranca` para aprovação da equipe antes da implementação.

---

## 💾 Fase 2: Banco de Dados, Imutabilidade e Seeds Sintéticos
- [ ] **T02.1:** Confirmar que `log_auditoria` em [`001_esquema_inicial.sql`](../../../backend/src/main/resources/db/changelog/migrations/001_esquema_inicial.sql) segue o padrão *append-only* (`ck_log_auditoria_*`, índices `ix_log_auditoria_usuario` e `ix_log_auditoria_criado_em`, sem `atualizado_em`, `excluido_em` nem triggers).
- [ ] **T02.2:** Validar scripts SQL executando `sqlfluff lint --dialect postgres`.
- [ ] **T02.3:** Executar [`data/verificacao.sql`](../../../data/verificacao.sql) confirmando **zero linhas** nas 9 consultas estruturais (incluindo a exceção de `log_auditoria` na consulta nº 9) e conferir `[LGPD: pessoal]` em `ip_origem`.
- [ ] **T02.4:** Criar seed sintético (`data/seeds/005_auditoria_seed_sintetico.sql`) com eventos das 5 ações e caso com `ip_origem` nulo (rotina interna).

---

## ⚙️ Fase 3: Backend (`AuditoriaService`, Executor Assíncrono e Formatador)
- [ ] **T03.1:** Implementar o Enum `AcaoAuditoria` (`LOGIN`, `UPLOAD`, `DOWNLOAD`, `EXCLUSAO`, `AGENDAMENTO`) e a `@Entity LogAuditoria` com `@org.hibernate.annotations.Immutable`, `columnDefinition = "text"` e `criado_em` (`insertable = true, updatable = false` — `RNF-06.5`) conforme [`AGENTS.md`](../../../AGENTS.md).
- [ ] **T03.2:** Implementar `LogAuditoriaRepository` estendendo `Repository<LogAuditoria, Long>` expondo apenas `save(...)` e métodos de leitura restritos a testes (sem métodos de atualização ou exclusão — `RNF-06.3`).
- [ ] **T03.3:** Implementar `AuditoriaAsyncConfig` configurando o `ThreadPoolTaskExecutor` (`auditoriaTaskExecutor`) com `waitForTasksToCompleteOnShutdown = true` e `awaitTerminationSeconds = 10` (`Seção 8`).
- [ ] **T03.4:** Implementar `ClientIpResolver` (suportando `X-Forwarded-For` apenas de proxies confiáveis via `server.forward-headers-strategy=NATIVE` e retornando `null` em rotinas sem requisição HTTP — `RN45`, `UC20`).
- [ ] **T03.5:** Implementar `RecursoAuditoriaFormatter` gerando identificadores canônicos (`1..255` chars) contendo exclusivamente tipo, ID numérico e operação, bloqueando dados pessoais ou conteúdo clínico (`RN47`, `RNF-06.4`).
- [ ] **T03.6:** Implementar `AuditoriaEventListener` com `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)` e `@Async("auditoriaTaskExecutor")`, tratando falhas de gravação em log técnico seguro sem impactar o usuário (`RN44`, `Cenário 7`).
- [ ] **T03.7:** Implementar `AuditoriaService.registrarAsync` (capturando `OffsetDateTime.now()` na thread da operação — `RNF-06.5`) e `AuditoriaService.registrarSincronoPreExclusao` com `@Transactional(propagation = Propagation.REQUIRES_NEW)` (`RN10`, `RN37`, `Cenário 8`).
- [ ] **T03.8:** Garantir ausência total de endpoints REST e componentes visuais expondo `log_auditoria` na aplicação (`RN17`, `RN48`).

---

## 🔗 Fase 4: Integração Transversal e Procedimento de Apuração
- [ ] **T04.1:** Integrar `AuditoriaService` aos pontos de disparo de `SPEC-001` (`LOGIN` e `EXCLUSAO` de usuário), `SPEC-003` (`UPLOAD`, `DOWNLOAD` e `EXCLUSAO` síncrona prévia de documento) e `SPEC-004` (`AGENDAMENTO`).
- [ ] **T04.2:** Configurar o perfil de `trusted-proxies` em `application.yml` sem expor segredos no repositório.
- [ ] **T04.3:** Documentar o procedimento operacional controlado de consulta SQL fora da aplicação com `SET TIME ZONE 'America/Sao_Paulo'` (`Seção 5` do [`plan.md`](plan.md)).

---

## 🧪 Fase 5: Testes e Validação
- [ ] **T05.1:** Implementar testes unitários (JUnit 5 + Mockito) para `RecursoAuditoriaFormatter`, `ClientIpResolver` e `AuditoriaService` (validando preservação de timestamp, resiliência a falhas em `registrarAsync` e propagação em `registrarSincronoPreExclusao`).
- [ ] **T05.2:** Implementar testes de integração (Spring Boot Test + Testcontainers PostgreSQL 18) verificando gravação em até 5s (`RNF-06.2`), ausência de logs para operações negadas/falhas (`400`/`401`/`403`/`413`/`415`), preservação de logs após soft delete do usuário (`RN48`) e bloqueio a `UPDATE`/`DELETE` via Hibernate (`RNF-06.3`).
- [ ] **T05.3:** Implementar teste de integração simulando indisponibilidade do banco de auditoria em `LOGIN`/`DOWNLOAD`/`AGENDAMENTO` (operação segue — `Cenário 7`) e em `DELETE /api/prontuarios/{id}` (retorna `500` e nada é removido — `Cenário 8`).
- [ ] **T05.4:** Executar benchmark de carga comparando operações auditadas e não auditadas confirmando acréscimo `<= 50 ms` no percentil 95 (`RNF-06.1`, `Cenário 9`).
- [ ] **T05.5:** Validar os **11 cenários BDD** de [`spec.md`](spec.md) em testes automatizados.
- [ ] **T05.6:** Executar varredura automatizada nos registros de `log_auditoria` e nos logs técnicos de falha garantindo zero ocorrências de senhas, títulos de documentos, nomes de pacientes ou descrições clínicas (`RNF-06.4`, `Cenário 11`).

---

## 🏁 Fase 6: Conclusão e Revisão
- [ ] **T06.1:** Confirmar alinhamento entre código implementado, [`contracts/api-contract.yaml`](contracts/api-contract.yaml), [`data-model.md`](data-model.md) e [`.specify/memory/domain-model.md`](../../memory/domain-model.md).
- [ ] **T06.2:** Validar `ddl-auto=validate` (`@Immutable` em `LogAuditoria`) e Checklist de Banco ([`AGENTS.md`](../../../AGENTS.md)).
- [ ] **T06.3:** Realizar Code Review final, validar todos os itens da *Definition of Done* em [`plan.md`](plan.md) e atualizar o status da `SPEC-005` para `Implementado`.
