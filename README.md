# AMDE Digital — Controle de Prontuário Clínico

> **Projeto Integrador / Trabalho de Graduação — ADS (Fatec Sorocaba)**  
> **Metodologia de Engenharia:** Spec-Driven Development (SDD) com apoio de IA Generativa  
> **Status:** Implementado (Incrementos 1 a 5 concluídos — SPEC-001 a SPEC-006)

---

## 👥 Identificação do Projeto

- **Projeto:** AMDE Digital — Controle de Prontuário
- **Instituição Parceira:** Associação Amigos dos Deficientes (AMDE) — Sorocaba/SP
- **Equipe de Desenvolvimento:**
  - Gustavo Ferreira Lopes
  - Helena Melchiades de Faria
  - Kaiky Saquette Pereira
  - Vinicius Firmino Oliveira de Souza
- **Orientação:** Prof. Antonio Cesar de Barros Munari
- **Disciplina/Etapa:** Projeto Integrador / Trabalho de Graduação — ADS (Fatec Sorocaba)
- **Monografia do Projeto (Novembro/2026):** [`docs/monografia-sdd-amde-digital-2026.pdf`](docs/monografia-sdd-amde-digital-2026.pdf) ([visão geral](docs/README.md))
- **Base Documental de Referência:** Monografia AMDE Digital (Dezembro/2025) em [`.specify/reference/references/`](.specify/reference/references/monografia-amde-digital.pdf)

---

## 📋 Resumo do Projeto

A **Associação Amigos dos Deficientes (AMDE)** é uma entidade filantrópica e sem fins lucrativos que presta atendimento multidisciplinar desde 2010 a crianças e adolescentes com **Transtorno do Espectro Autista (TEA)** por meio do *Centro de Excelência em Autismo*. 

Historicamente, a gestão de pacientes, prontuários clínicos e controle de presenças era realizada através de fichas físicas em papel e planilhas descentralizadas, gerando lentidão na recuperação de históricos, vulnerabilidade na guarda de dados sensíveis e risco de não conformidade com a LGPD e resoluções do Conselho Federal de Medicina (Resolução CFM nº 1.821/2007).

O **AMDE Digital** é uma plataforma integrada voltada à gestão de prontuários eletrônicos (PEP), agendamentos clínicos e controle de presença, desenvolvida sob os padrões modernos de software em nuvem. 

O foco deste repositório é **reconstruir e evoluir o sistema AMDE Digital aplicando rigorosamente a metodologia Spec-Driven Development (SDD)**, garantindo que toda decisão arquitetural, regra de negócio e modelo de dados seja especificada, verificada e rastreável antes da escrita do código.

---

## 🛠️ Stack Tecnológica

| Camada / Função | Tecnologias Utilizadas |
|---|---|
| **Back-end** | Java 17 LTS, Spring Boot 3, Spring Security, Spring Data JPA, MapStruct, Maven |
| **Front-end** | React 18+ (SPA), TypeScript, Vite, Tailwind CSS, Shadcn/ui (Radix UI), Axios |
| **Banco de Dados** | PostgreSQL 18 (schema `amde_digital`) com controle de evolução via **Liquibase** (`--liquibase formatted sql`) e verificação automática (`sqlfluff` + `data/verificacao.sql`) |
| **Autenticação** | Stateless via tokens JWT (JSON Web Tokens) com hash BCrypt para credenciais |
| **DevOps & Cloud** | Docker, Docker Compose, GitHub Actions (CI/CD) e serviços em nuvem AWS (S3, CloudFront) |

---

## 📐 Estrutura do Repositório e Metodologia SDD

O projeto adota uma separação estrita entre a memória global, os pacotes incrementais de especificação e o código-fonte da aplicação:

```text
├── .specify/                           # Diretório central da metodologia SDD
│   ├── README.md                       # Fluxo da metodologia e roadmap de especificações
│   ├── memory/                         # Memória global permanente
│   │   ├── constitution.md             # Regras inegociáveis, stack e comportamento da IA
│   │   ├── domain-model.md             # DER conceitual em Mermaid, cardinalidades e glossário
│   │   └── design-system.md            # Diretrizes visuais, acessibilidade WCAG e UI
│   ├── reference/                      # Base consolidada de requisitos, arquitetura e dados
│   │   ├── requirements/               # Requisitos funcionais e não funcionais
│   │   ├── architecture/               # Visão arquitetural e diagrama de classes
│   │   ├── data-model/                 # DER e dicionário de dados
│   │   └── references/                 # Monografia e diretrizes de referência
│   └── specs/                          # Especificações incrementais por funcionalidade
│       ├── 001-autenticacao-e-usuarios/  # SPEC-001: Login JWT, usuários e RBAC
│       ├── 002-gestao-de-pacientes/      # SPEC-002: Prontuário e gestão de pacientes TEA
│       ├── 003-prontuarios-e-anexos/     # SPEC-003: Anexos PDF, categorias e versionamento
│       ├── 004-agendamentos-e-consultas/ # SPEC-004: Agenda clínica, concorrência e presença
│       ├── 005-auditoria-e-seguranca/    # SPEC-005: Trilha de auditoria assíncrona (LGPD/CFM)
│       ├── 006-dashboard-gerencial/      # SPEC-006: Indicadores e gráficos gerenciais
│       └── _template/                    # Template base para novas specs
├── docs/                               # Documentação acadêmica final do projeto (Monografia SDD 2026)
│   ├── README.md                       # Metadados e estrutura da monografia
│   └── monografia-sdd-amde-digital-2026.pdf # Monografia completa (Novembro/2026)
├── AGENTS.md                           # Regras de banco PostgreSQL 18, exceções JPA/Hibernate e checklist de PR
├── .sqlfluff                           # Configuração de lint SQL (dialeto postgres, minúsculas)
├── docker-compose.yml                  # Container PostgreSQL 18 (Debian + ICU pt-BR) com bootstrap automático
├── data/                               # Scripts de bootstrap (00_bootstrap.sql), seeds sintéticos e auditoria (verificacao.sql)
├── design/
│   └── tokens.json                     # Design tokens legíveis por máquina
├── backend/                            # Código-fonte da API Spring Boot e changelogs Liquibase
├── frontend/                           # Código-fonte da aplicação Web React
├── application/                        # Pasta do template original (fora de escopo; sistema dividido em backend/ e frontend/)
└── mobile/                             # Pasta do template original (fora de escopo; sem cliente móvel nesta entrega)
```

Os artefatos técnicos de engenharia do fluxo SDD ficam centralizados em [`.specify/reference/`](.specify/reference/), enquanto a documentação acadêmica final deste projeto (monografia de Novembro/2026) encontra-se em [`docs/`](docs/README.md). Templates genéricos e exemplos fictícios foram removidos.

---

## 🚀 Como Executar o Ambiente de Desenvolvimento

### Pré-requisitos
- Git
- Docker e Docker Compose
- Java JDK 17+ e Maven
- Node.js (LTS v18+) e npm/yarn

### 1. Inicializar Serviços de Infraestrutura (PostgreSQL 18 + Bootstrap)
```bash
docker compose up -d postgres
```

> Copie `backend/.env.example` para configurar suas variáveis locais sem versionar senhas reais. Para auditar a conformidade estrutural do schema `amde_digital`, execute `data/verificacao.sql` e `sqlfluff lint` conforme descrito em [`data/README.md`](data/README.md).

### 2. Executar o Back-end (Spring Boot)
```bash
cd backend
./mvnw clean spring-boot:run
```

### 3. Executar o Front-end (React/Vite)
```bash
cd frontend
npm install
npm run dev
```

---

## 🔒 Conformidade Legal e Segurança (LGPD)

Em conformidade com a Constituição do Projeto e com a Lei Geral de Proteção de Dados (Lei nº 13.709/2018):
- É estritamente vedado o versionamento de dados reais de pacientes ou colaboradores no repositório.
- Todos os testes e validações locais utilizam **massas de dados sintéticas**.
- Todas as operações com documentos clínicos (upload, download, exclusão) geram registros automáticos em trilha de auditoria.
