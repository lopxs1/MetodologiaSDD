# Checklist de Tarefas: Agendamentos e Consultas

> **ID da Spec:** `SPEC-004`  
> **Status Geral:** `24 / 26 Concluídas (T05.6 auditoria Axe/benchmark 10k e T06.3 revisão final pendentes)`  
> **Responsáveis:** Gustavo Ferreira Lopes, Helena Melchiades de Faria, Kaiky Saquette Pereira, Vinicius Firmino Oliveira de Souza  
> **Origem dos Requisitos:** [spec.md](spec.md) | **Plano Técnico:** [plan.md](plan.md) | **Modelo de Dados:** [data-model.md](data-model.md) | **Contratos:** [contracts/api-contract.yaml](contracts/api-contract.yaml)

---

## 📌 Fase 1: Especificação e Contratos
- [x] **T01.1:** Revisar requisitos (`RF-05.1` a `RF-05.9`), RNFs (`RNF-05.1` a `RNF-05.5`), regras (`RN11`–`RN16`, `RN39`–`RN43`) e os 15 cenários BDD em [`spec.md`](spec.md).
- [x] **T01.2:** Validar o contrato OpenAPI 3.0.3 em [`contracts/api-contract.yaml`](contracts/api-contract.yaml) para os 7 endpoints de `/api/agendamentos`.
- [x] **T01.3:** Submeter o pacote `004-agendamentos-e-consultas` para aprovação da equipe antes da implementação.

---

## 💾 Fase 2: Banco de Dados, Índices e Seeds Sintéticos
- [x] **T02.1:** Confirmar que `agendamento` e `agendamento_paciente` (incluindo `ck_agendamento_periodo`, `ck_agendamento_tipo`, `ck_agendamento_status` e índice parcial `ix_agendamento_funcionario_periodo`) em [`001_esquema_inicial.sql`](../../../backend/src/main/resources/db/changelog/migrations/001_esquema_inicial.sql) atendem integralmente `UC14`–`UC19`.
- [x] **T02.2:** Validar scripts SQL executando `sqlfluff lint --dialect postgres`.
- [x] **T02.3:** Executar [`data/verificacao.sql`](../../../data/verificacao.sql) confirmando **zero linhas** nas 9 verificações estruturais e revisar `[LGPD: sensível]` em `agendamento_paciente.possui_presenca`.
- [x] **T02.4:** Criar seed sintético (`data/seeds/004_agendamentos_seed_sintetico.sql`) com sessões individuais e em grupo nos status `AGENDADO`, `CONCLUIDO` e `CANCELADO`.

---

## ⚙️ Fase 3: Backend, Concorrência e Regras de Negócio (Spring Boot 3 / Java 17)
- [x] **T03.1:** Implementar Enums (`TipoAgendamento`, `StatusAgendamento`), chave composta `@Embeddable AgendamentoPacienteId` e `@Entity` (`Agendamento`, `AgendamentoPaciente` com `@EmbeddedId` e `@MapsId`) aderentes a [`AGENTS.md`](../../../AGENTS.md).
- [x] **T03.2:** Adicionar consulta com `@Lock(LockModeType.PESSIMISTIC_WRITE)` em `FuncionarioRepository` para serializar criações/edições simultâneas por profissional (`RNF-05.2`).
- [x] **T03.3:** Implementar `AgendamentoRepository` com verificação de interseção semiaberta `[dataInicio, dataFim)` ignorando `CANCELADO` (`UC18`, `RN41`) e consultas paginadas com filtros (período, situação, tipo, busca textual e visibilidade por perfil — `UC19`, `RN15`).
- [x] **T03.4:** Implementar `AgendamentoService.criar` (`UC14`) validando autorização por agenda (`RN43`), início futuro (`RN41`), `dataFim > dataInicio` (`RN12`), lista única de `>= 1` pacientes ativos (`RN40`), lock pessimista + conflito (`409`) e evento `AGENDAMENTO`.
- [x] **T03.5:** Implementar `AgendamentoService.editar` (`UC16`) exigindo `AGENDADO` (`409` se final), mantendo `funcionarioId` imutável (`RN40`), preservando presença de participantes mantidos, revalidando conflito ignorando o próprio ID e registrando auditoria.
- [x] **T03.6:** Implementar `AgendamentoService.cancelar` (`UC17`) mudando status para `CANCELADO` sem exclusão física (`409` se final) e disparando evento `AGENDAMENTO`.
- [x] **T03.7:** Implementar `AgendamentoService.registrarPresenca` e `concluir` (`UC16`, `RN42`) exigindo `Agora >= dataInicio` e bloqueando `CANCELADO` (`409 Conflict`), permitindo correção de presença após conclusão e disparando evento `AGENDAMENTO`.
- [x] **T03.8:** Implementar `AgendamentoService.listar` (`UC19`) e `consultarPorId` (`UC15`) aplicando minimização LGPD obrigatória para `PACIENTE` em sessões em grupo (`RN43`, `RNF-05.3`). *(Mapeamento implementado diretamente em `AgendamentoService`.)*
- [x] **T03.9:** Implementar `AgendamentoController` e tratamento de exceções (`400`, `401`, `403`, `404`, `409`).

---

## 🖥️ Fase 4: Frontend / Interface Web (React 18 / TypeScript / Tailwind)
- [x] **T04.1:** Implementar `agendamentoService.ts` consumindo todos os endpoints de `/api/agendamentos`.
- [x] **T04.2:** Construir `AgendamentosPage` (`/agendamentos`) com filtros (busca, período, status, tipo e profissional para Admin), ordenação `ASC`/`DESC`, paginação na base, badges semânticos de [`design/tokens.json`](../../../design/tokens.json), horários no fuso `America/Sao_Paulo` e os 4 estados de tela (`RNF-05.4`).
- [x] **T04.3:** Construir `AgendamentoFormModal` (criação e edição para Admin/Médico) com seleção múltipla de pacientes para sessões em grupo, trava do profissional para Médico/edição e exibição de conflito `409` junto ao campo de horário (`RNF-05.4`).
- [x] **T04.4:** Construir `AgendamentoDetalheModal` ocultando outros participantes quando o usuário autenticado for `PACIENTE` (`RNF-05.3`).
- [x] **T04.5:** Construir `PresencaModal` e ação **"Concluir"** respeitando as restrições de início da sessão e status (`RN42`).
- [x] **T04.6:** Construir `ConfirmarCancelamentoDialog` com backdrop escurecido e suporte completo a teclado (`Tab`, `Enter`, `Esc` — `UC17`, `RNF-05.5`).

---

## 🧪 Fase 5: Testes e Validação
- [x] **T05.1:** Implementar testes unitários (JUnit 5 + Mockito) para `AgendamentoService` cobrindo todas as RNs (`RN11`–`RN16`, `RN39`–`RN43`).
- [x] **T05.2:** Implementar testes de integração (Spring Boot Test + PostgreSQL 18 em `DB_PORT=5433`) validando sessões adjacentes sem conflito (`Cenário 6`), minimização LGPD em grupo (`RNF-05.3`), bloqueio `403` fora da agenda e eventos `AGENDAMENTO` na auditoria.
- [x] **T05.3:** Implementar teste automatizado de concorrência com requisições simultâneas sobrepostas ao mesmo profissional garantindo `1` criação (`201`) e `1` `409 Conflict` (`RNF-05.2`, `Cenário 15`).
- [x] **T05.4:** Implementar testes de front-end (Vitest + Testing Library) para formulários, filtros, fuso `America/Sao_Paulo`, presença e modal de cancelamento.
- [x] **T05.5:** Validar os **15 cenários BDD** de [`spec.md`](spec.md) em testes automatizados.
- [ ] **T05.6:** Validar acessibilidade WCAG 2.1 AA com Axe DevTools (`RNF-05.5`) e desempenho p95 < 500 ms com massa sintética de 10.000 agendamentos (`RNF-05.1`).

---

## 🏁 Fase 6: Conclusão e Revisão
- [x] **T06.1:** Confirmar alinhamento entre código implementado, [`contracts/api-contract.yaml`](contracts/api-contract.yaml), [`data-model.md`](data-model.md) e [`.specify/memory/domain-model.md`](../../memory/domain-model.md).
- [x] **T06.2:** Validar `ddl-auto=validate` (incluindo `AgendamentoPacienteId`) e Checklist de Banco ([`AGENTS.md`](../../../AGENTS.md)).
- [ ] **T06.3:** Realizar Code Review final, validar todos os itens da *Definition of Done* em [`plan.md`](plan.md) e atualizar o status da `SPEC-004` para `Implementado`.
