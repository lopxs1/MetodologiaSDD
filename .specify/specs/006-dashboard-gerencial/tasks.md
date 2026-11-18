# Checklist de Tarefas: Dashboard Gerencial

> **ID da Spec:** `SPEC-006`  
> **Status Geral:** `0 / 24 Concluídas`  
> **Responsáveis:** Gustavo Ferreira Lopes, Helena Melchiades de Faria, Kaiky Saquette Pereira, Vinicius Firmino Oliveira de Souza  
> **Origem dos Requisitos:** [spec.md](spec.md) | **Plano Técnico:** [plan.md](plan.md) | **Modelo de Dados:** [data-model.md](data-model.md) | **Contratos:** [contracts/api-contract.yaml](contracts/api-contract.yaml)

---

## 📌 Fase 1: Especificação e Contratos
- [ ] **T01.1:** Revisar requisitos (`RF-07.1` a `RF-07.5`), RNFs (`RNF-07.1` a `RNF-07.5`), regras (`RN49` a `RN52`) e os 10 cenários BDD em [`spec.md`](spec.md).
- [ ] **T01.2:** Validar o contrato OpenAPI 3.0.3 em [`contracts/api-contract.yaml`](contracts/api-contract.yaml) para `GET /api/dashboard` (filtros `dataInicio`, `dataFim`, `funcionarioId`, 4 contadores e 2 séries analíticas).
- [ ] **T01.3:** Submeter o pacote `006-dashboard-gerencial` para aprovação da equipe antes da implementação.

---

## 💾 Fase 2: Banco de Dados, Índices e Seeds Sintéticos
- [ ] **T02.1:** Confirmar que `agendamento`, `funcionario` e `paciente` e os índices parciais (`ix_agendamento_funcionario_periodo`, `ix_funcionario_cargo`, `ix_paciente_grau_suporte`) em [`001_esquema_inicial.sql`](../../../backend/src/main/resources/db/changelog/migrations/001_esquema_inicial.sql) atendem às consultas agregadas do `UC21`.
- [ ] **T02.2:** Validar scripts SQL executando `sqlfluff lint --dialect postgres`.
- [ ] **T02.3:** Executar [`data/verificacao.sql`](../../../data/verificacao.sql) confirmando **zero linhas** nas 9 verificações estruturais.
- [ ] **T02.4:** Criar seed sintético (`data/seeds/006_dashboard_seed_sintetico.sql`) com cargos equivalentes (`"Psicologia"` e `" psicologia "`), profissional inativado com consultas históricas e agendamentos limítrofes às 23h30 em `America/Sao_Paulo`.

---

## ⚙️ Fase 3: Backend e Consultas Analíticas (Spring Boot 3 / Java 17)
- [ ] **T03.1:** Implementar DTOs de resposta (`DashboardResponse`, `DashboardContadoresDTO`, `ConsultaDiaDTO`, `ConsultaEspecialidadeDTO`) garantindo ausência de nomes, e-mails ou dados clínicos (`RNF-07.4`).
- [ ] **T03.2:** Implementar `DashboardRepository` com consultas agregadas em `amde_digital`: `totalConsultas` e `consultasHoje` (`AT TIME ZONE 'America/Sao_Paulo'`, ignorando `CANCELADO` — `RN50`), além de `totalPacientes` e `profissionaisAtivos` (`excluido_em IS NULL`).
- [ ] **T03.3:** Implementar consulta de evolução diária (`Consultas ao Longo do Tempo` ordenada cronologicamente — `RN51`) respeitando os filtros opcionais de período e profissional.
- [ ] **T03.4:** Implementar consulta de distribuição por especialidade (`Consultas por Especialidade`) agrupando por `lower(trim(cargo))` com grafia do primeiro profissional (`ORDER BY id ASC`), mantendo consultas de profissionais inativados, calculando percentuais e ordenando por `quantidade DESC, lower(especialidade) ASC` (`RN51`).
- [ ] **T03.5:** Implementar `DashboardService.obterMetricas` com `@Transactional(readOnly = true)` para snapshot consistente (`RNF-07.5`), validação de período invertido (`400`) e verificação de `funcionarioId` inexistente (`404`).
- [ ] **T03.6:** Implementar `DashboardController` (`GET /api/dashboard`) protegido com `@PreAuthorize("hasRole('ADMINISTRADOR')")` (`403` para `MEDICO` e `PACIENTE` — `RN49`).

---

## 🖥️ Fase 4: Frontend / Interface Web (React 18 / TypeScript / Tailwind)
- [ ] **T04.1:** Implementar `dashboardService.ts` consumindo `GET /api/dashboard` com parâmetros opcionais `dataInicio`, `dataFim` e `funcionarioId`.
- [ ] **T04.2:** Garantir que o item `"Visão Geral / Dashboard"` do `Sidebar` seja exibido exclusivamente para `ADMINISTRADOR` e que a rota `/dashboard` seja protegida por perfil (`RN49`).
- [ ] **T04.3:** Construir `DashboardFilters` com seleção de Período (validando data inicial `<=` data final) e Profissional (Todos ou funcionário selecionado via lista administrativa).
- [ ] **T04.4:** Construir `MetricCardsGrid` exibindo os 4 cards (`Total de Consultas`, `Consultas Hoje`, `Total de Pacientes`, `Profissionais Ativos`) com layout responsivo em breakpoints Tailwind (`RNF-07.3`).
- [ ] **T04.5:** Construir `ConsultasTempoChart` e `ConsultasEspecialidadeChart` acompanhados de **tabelas alternativas acessíveis** (`<table>` semântica) e distinção visual que não dependa apenas de cor (`RNF-07.2`).
- [ ] **T04.6:** Implementar os 4 estados obrigatórios (`Skeleton` nos cards e gráficos, `Vazio` orientativo sem falhas quando `totalConsultas = 0`, `Erro` com botão **"Recarregar"** sem dados parciais e `Sucesso` — `RF-07.5`, `RNF-07.3`).

---

## 🧪 Fase 5: Testes e Validação
- [ ] **T05.1:** Implementar testes unitários (JUnit 5 + Mockito) para `DashboardService` cobrindo validação de período invertido (`400`), `funcionarioId` inexistente (`404`), fuso `America/Sao_Paulo` (`Cenário 2`) e regras `RN49`–`RN52`.
- [ ] **T05.2:** Implementar testes de integração (Spring Boot Test + Testcontainers PostgreSQL 18) validando exclusão de `CANCELADO`, contagem de profissional inativado apenas nas consultas/gráficos (`Cenário 3`), agrupamento de cargos equivalentes (`Cenário 5`), filtros combinados (`Cenário 10`), `403` para `MEDICO`/`PACIENTE` e `sum(gráfico) == totalConsultas` (`RNF-07.5`).
- [ ] **T05.3:** Implementar testes de front-end (Vitest + Testing Library) para os 4 cards, os 2 gráficos, tabelas alternativas, filtros e os 4 estados de tela.
- [ ] **T05.4:** Validar os **10 cenários BDD** de [`spec.md`](spec.md) em testes automatizados e inspecionar respostas confirmando zero PII/dados clínicos (`RNF-07.4`).
- [ ] **T05.5:** Validar acessibilidade WCAG 2.1 AA com Axe DevTools e Lighthouse (`RNF-07.2`).
- [ ] **T05.6:** Executar teste de carga com massa sintética de 10.000 agendamentos e 1.000 pacientes confirmando p95 < 500 ms (`RNF-07.1`).

---

## 🏁 Fase 6: Conclusão e Revisão
- [ ] **T06.1:** Confirmar alinhamento entre código implementado, [`contracts/api-contract.yaml`](contracts/api-contract.yaml), [`data-model.md`](data-model.md) e [`.specify/memory/domain-model.md`](../../memory/domain-model.md).
- [ ] **T06.2:** Validar `ddl-auto=validate` e Checklist de Banco ([`AGENTS.md`](../../../AGENTS.md)).
- [ ] **T06.3:** Realizar Code Review final, validar todos os itens da *Definition of Done* em [`plan.md`](plan.md) e atualizar o status da `SPEC-006` para `Implementado`.
