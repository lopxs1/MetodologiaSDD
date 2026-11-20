# Product Backlog — AMDE Digital (Controle de Prontuário)

## Objetivo e escopo do backlog

- **Produto/projeto:** AMDE Digital — Sistema de Controle de Prontuário, Agendamentos e Gestão Clínica
- **Instituição Parceira:** Associação Amigos dos Deficientes (AMDE) — Sorocaba/SP
- **Objetivo do produto:** Substituir fichas em papel e planilhas descentralizadas por uma plataforma web segura (Spring Boot 3 + PostgreSQL 18 + React 18) com prontuário eletrônico (PEP), agenda multidisciplinar, auditoria LGPD/CFM e painel gerencial.
- **Responsáveis pela manutenção:** Gustavo Ferreira Lopes, Helena Melchiades de Faria, Kaiky Saquette Pereira e Vinicius Firmino Oliveira de Souza
- **Última revisão:** 2026-11-18
- **Critério de priorização:** Risco clínico/legal (segurança, LGPD e Resolução CFM nº 1.821/2007), dependências estruturais entre domínio de usuários, pacientes, prontuários e agenda, e valor operacional para a equipe da AMDE.

## Itens priorizados

| ID | Tipo | Item / necessidade | Valor ou motivo | Prioridade | Estado | Critério de aceite / conclusão | Origem e vínculo | Dependências |
|---|---|---|---|---|---|---|---|---|
| PB-01 | Funcionalidade | Autenticação JWT stateless, gestão de usuários e controle RBAC | Isolar acesso por perfil (`ADMINISTRADOR`, `MEDICO`, `PACIENTE`) e proteger credenciais com BCrypt | Crítica | Validado | Login JWT, CRUD de usuários, especialização 1:1 (`funcionario`/`paciente`), mascaramento de celular e cenários BDD validados | `SPEC-001` (`RF-01`, `RF-02`, `UC01`–`UC07`) | Schema `amde_digital` (`001_esquema_inicial.sql`) |
| PB-02 | Funcionalidade | Trilha de auditoria assíncrona imutável (`log_auditoria`) | Garantir conformidade com LGPD e CFM nº 1.821/2007 sem bloquear respostas HTTP | Crítica | Validado | Gravação assíncrona pós-commit para `LOGIN`, `UPLOAD`, `DOWNLOAD`, `EXCLUSAO` e `AGENDAMENTO` | `SPEC-005` (`RF-06`, `UC20`, `RN16`) | `SPEC-001` |
| PB-03 | Funcionalidade | Gestão clínica de pacientes TEA e histórico categorizado | Centralizar dados clínicos, grau de suporte (níveis 1 a 3) e leitura controlada | Alta | Validado | Listagem com filtro por grau de suporte, busca sem acento, edição clínica e modo somente leitura para `PACIENTE` | `SPEC-002` (`RF-03`, `UC08`, `UC09`) | `SPEC-001`, `SPEC-005` |
| PB-04 | Funcionalidade | Gestão de prontuários digitais e anexos PDF | Armazenar laudos, exames e receituários (< 10 MB, UUID físico) com versionamento | Alta | Validado | Upload, download, reclassificação, substituição preservando histórico e exclusão auditada | `SPEC-003` (`RF-04`, `UC10`–`UC13`) | `SPEC-002`, `SPEC-005` |
| PB-05 | Funcionalidade | Agendamentos clínicos, bloqueio de conflito e presença | Evitar sobreposição de horários do profissional e controlar presença individual e em grupo | Alta | Validado | Lock pessimista para concorrência, sessões individuais/grupo, minimização LGPD para `PACIENTE` e cancelamento lógico | `SPEC-004` (`RF-05`, `UC14`–`UC19`) | `SPEC-001`, `SPEC-005` |
| PB-06 | Funcionalidade | Dashboard gerencial com filtros de período e profissional | Apoiar prestação de contas e dimensionamento da equipe sem expor PII | Média | Validado | 4 contadores, 2 séries analíticas no fuso `America/Sao_Paulo`, tabelas alternativas WCAG 2.1 AA e acesso exclusivo ao `ADMINISTRADOR` | `SPEC-006` (`RF-07`, `UC21`, `RN49`–`RN52`) | `SPEC-001`, `SPEC-004` |

## Estados adotados

- **Concluído:** o trabalho definido no item foi implementado na API e na interface web.
- **Validado:** há evidência automatizada de aceitação (JUnit 5 / MockMvc / PostgreSQL 18 + Vitest / Testing Library) e conformidade SQL (`sqlfluff` + `data/verificacao.sql`).

## Histórico de revisões

| Data | Item alterado | Alteração / decisão | Motivo ou origem |
|---|---|---|---|
| 2026-11-18 | PB-01 a PB-06 | Consolidação do backlog real do AMDE Digital com rastreabilidade completa para `SPEC-001` a `SPEC-006` | Substituição do modelo genérico após conclusão dos Incrementos 1 a 5 |
