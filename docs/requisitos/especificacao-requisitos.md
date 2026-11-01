# Especificação de Requisitos — AMDE Digital

> **Projeto:** AMDE Digital — Controle de Prontuário  
> **Versão:** 1.0.0  
> **Data:** 2026-11-01  
> **Base Documental:** Monografia AMDE Digital (Dezembro/2025) / Fatec Sorocaba  
> **Metodologia SDD:** Especificação detalhada em [`.specify/`](../../.specify/README.md)

---

## 1. Requisitos Funcionais (RF)

| ID | Nome | Descrição | Prioridade | Casos de Uso | Rastreio SDD |
|---|---|---|---|---|---|
| **RF-01** | Autenticação e Controle de Acesso | Login via e-mail e senha, hash BCrypt, emissão de JWT e autorização por perfil (ADMINISTRADOR, MEDICO/FUNCIONARIO, PACIENTE). | Crítica | UC01, UC02, UC03 | `specs/001-autenticacao-e-usuarios/` |
| **RF-02** | Gestão de Usuários | CRUD completo de usuários (cadastro, consulta, edição e exclusão) exclusivo do Administrador. | Alta | UC04, UC05, UC06, UC07 | `specs/001-autenticacao-e-usuarios/` |
| **RF-03** | Consulta de Pacientes e Prontuários | Consulta com busca por nome/e-mail, ordenação ASC/DESC, dados clínicos (grau de suporte TEA) e histórico por categorias. | Alta | UC08, UC09 | `specs/002-gestao-de-pacientes/` |
| **RF-04** | Gestão de Prontuários Digitais | Upload de arquivos (PDF < 10MB), UUID seguro no storage, download auditado, categorização e exclusão. | Crítica | UC10, UC11, UC12, UC13 | `specs/003-prontuarios-e-anexos/` |
| **RF-05** | Gestão de Agendamentos e Presença | Agendamento clínico com validação de conflito de horários do profissional, tipos de sessão e controle individual de presença. | Crítica | UC14 a UC19 | `specs/004-agendamentos-e-consultas/` |
| **RF-06** | Registro de Log de Auditoria | Trilha de auditoria automática em operações sensíveis (login, upload, download, exclusão e agendamentos) para conformidade LGPD. | Crítica | UC20 | `specs/005-auditoria-e-seguranca/` |
| **RF-07** | Dashboard Gerencial | Painel analítico para Administrador com contadores (consultas totais, consultas hoje, pacientes ativos) e gráficos de distribuição. | Média | UC21 | `specs/006-dashboard-gerencial/` |

---

## 2. Requisitos Não Funcionais (RNF)

| ID | Atributo / Categoria | Descrição | Critério de Verificação |
|---|---|---|---|
| **RNF-01** | Segurança | Autenticação stateless via tokens JWT; senhas criptografadas com algoritmo BCrypt. | Teste de segurança e inspeção de cabeçalhos HTTP. |
| **RNF-02** | Desempenho | Tempo médio de resposta inferior a 500ms em consultas padrão sob carga. | Teste de carga com massa de dados sintética. |
| **RNF-03** | Arquitetura | Back-end em Java 17 com Spring Boot 3; Front-end web em React/TypeScript (Vite). | Inspeção de código e compilação nos pipelines CI/CD. |
| **RNF-04** | Banco de Dados & Migrations | Banco relacional PostgreSQL 16 com controle de evolução por changelogs Liquibase. | Execução de migração UP e rollback em ambiente de teste. |
| **RNF-05** | Acessibilidade e Interface | Aderência às diretrizes WCAG 2.1 nível AA e uso estrito de tokens do Design System. | Validação automatizada com Lighthouse e Axe DevTools. |

---

## 3. Regras de Negócio Fundamentais (RN)

| ID | Regra de Negócio | Módulo Impactado | Origem |
|---|---|---|---|
| **RN01** | Senhas criptografadas exclusivamente com BCrypt; nunca em texto puro. | Autenticação | UC02, UC04 |
| **RN02** | Unicidade de e-mail no cadastro de usuários (`HTTP 409 Conflict` se duplicado). | Usuários | UC04 |
| **RN03** | Exclusividade administrativa sobre operações de criação e gestão de usuários. | Usuários | UC04 a UC07 |
| **RN04** | Especialização 1:1 de `usuario` em `pacientes` (grau de suporte TEA) ou `funcionarios` (CRM/cargo). | Domínio | Diagrama de Classes |
| **RN05** | Validação estrita de formato (PDF) e tamanho máximo (< 10MB) no upload de prontuário. | Prontuários | UC10, UC13 |
| **RN06** | Permissão de upload/exclusão restrita a Funcionário e Admin; download aberto ao titular. | Prontuários | UC10, UC11, UC12 |
| **RN07** | Preservação do nome original no banco e uso de UUID para o arquivo físico no storage. | Prontuários | UC10 |
| **RN08** | Categorização obrigatória (Exames, Laudos, Receituário, Arquivado) e versionamento via substituição. | Prontuários | Modelo de Dados |
| **RN09** | Download retorna `404 Not Found` se ID inexistente e `500` se arquivo físico ausente. | Prontuários | UC11 |
| **RN10** | Exclusão de prontuário remove arquivo físico e registra log antes de deletar registro. | Prontuários | UC12 |
| **RN11** | Pré-existência obrigatória de Paciente e Funcionário para registrar agendamento. | Agendamentos | UC14 |
| **RN12** | Consistência temporal de datas: `data_inicio < data_final`. | Agendamentos | UC14 |
| **RN13** | Proibição de agendamentos conflitantes no mesmo horário para o mesmo profissional (`HTTP 409`). | Agendamentos | UC14, UC18 |
| **RN14** | Status inicial padrão "Agendado" e controle individual de presença via `agenda_paciente`. | Agendamentos | UC14 |
| **RN15** | Filtro automático de visibilidade de agendamentos pelo perfil logado (Paciente/Médico/Admin). | Agendamentos | UC19 |
| **RN16** | Rastreabilidade mandatória em `log_auditoria` para todas as ações sensíveis (LGPD / CFM). | Auditoria | UC20 |

