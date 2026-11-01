# AMDE Digital — Controle de Prontuário Clínico

> **Projeto Integrador / Trabalho de Graduação — ADS (Fatec Sorocaba)**  
> **Metodologia de Engenharia:** Spec-Driven Development (SDD) com apoio de IA Generativa  
> **Status:** Fase de Especificação e Estruturação SDD

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
- **Disciplina/Etapa:** Projeto Integrador / Trabalho de Graduação — ADS
- **Base Documental de Referência:** Monografia AMDE Digital (Dezembro/2025)

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
| **Banco de Dados** | PostgreSQL 16 com controle de evolução via **Liquibase** (changelogs versionados) |
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
│   ├── design/
│   │   └── tokens.json                 # Design tokens legíveis por máquina
│   └── specs/                          # Especificações incrementais por funcionalidade
│       └── _template/                  # Template base para novas specs (spec, plan, data-model, tasks)
├── docs/                               # Documentação acadêmica institucional (Fatec)
│   ├── TG-Template-ADS-formato-academico 1-v1.docx
│   ├── requisitos/                     # Rastreabilidade de requisitos
│   ├── der/                            # Modelagem conceitual e lógica
│   ├── arquitetura/                    # Visões arquiteturais e diagramas
│   └── decisoes/                       # Registros de Decisão Arquitetural (ADRs)
├── backend/                            # Código-fonte da API Spring Boot
├── frontend/                           # Código-fonte da aplicação Web React
└── mobile/                             # Código-fonte do aplicativo mobile (quando aplicável)
```

---

## 🚀 Como Executar o Ambiente de Desenvolvimento

### Pré-requisitos
- Git
- Docker e Docker Compose
- Java JDK 17+ e Maven
- Node.js (LTS v18+) e npm/yarn

### 1. Inicializar Serviços de Infraestrutura
```bash
docker-compose up -d postgres
```

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
