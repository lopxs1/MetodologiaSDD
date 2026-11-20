# Backend / API / Serviços — AMDE Digital

> **Stack Definida:** Java 17 LTS, Spring Boot 3, Spring Security 6 (JWT + BCrypt), Spring Data JPA / Hibernate 6, MapStruct, Liquibase e PostgreSQL 18.  
> **Estado Atual:** Em fase de especificação SDD; o changelog inicial do banco ([`src/main/resources/db/changelog/migrations/001_esquema_inicial.sql`](src/main/resources/db/changelog/migrations/001_esquema_inicial.sql)) está estruturado, enquanto o projeto Maven/Java (`pom.xml` e código-fonte) será derivado das specs em `.specify/specs/`.

---

## Responsabilidade e Limites

- **Responsabilidade:** API REST monolítica modular para gestão de usuários, pacientes com TEA, prontuários eletrônicos (PDF), agendamentos clínicos, controle de presença e trilha de auditoria imutável.
- **Interfaces/Consumidores:** Portal Administrativo Web (`frontend/`) e cliente móvel (`mobile/`, quando aplicável).
- **Segregação de Privilégios no Banco:**
  - **Liquibase (DDL / Migrations):** conecta como role dona **`amde_digital_owner`**.
  - **Aplicação Spring Boot / JPA (DML em runtime):** conecta como role restrita **`amde_digital_app`** (apenas `SELECT`, `INSERT`, `UPDATE`, `DELETE` no schema `amde_digital`, `statement_timeout = 30s` e `idle_in_transaction_session_timeout = 60s`).

---

## Pré-requisitos

| Dependência | Versão suportada | Como verificar |
|---|---|---|
| Java JDK | 17 LTS+ | `java -version` |
| Maven (ou Wrapper `./mvnw`) | 3.9+ | `mvn -version` |
| PostgreSQL | 18 (via Docker Compose) | `docker compose ps` |
| SQLFluff (lint SQL) | 3.x (`postgres` dialect) | `sqlfluff --version` |

---

## Configuração Local

1. Garanta que o PostgreSQL 18 foi inicializado via [`docker-compose.yml`](../docker-compose.yml) (que executa [`data/00_bootstrap.sql`](../data/00_bootstrap.sql) automaticamente no primeiro start).
2. Copie `backend/.env.example` para `backend/.env` (ignorado pelo Git) e preencha credenciais locais fictícias de desenvolvimento.
3. Nunca utilize `superusuário` (`postgres`) nem a role dona (`amde_digital_owner`) na conexão principal (`spring.datasource`) da aplicação.

### Variáveis de Ambiente Necessárias

Consulte [`backend/.env.example`](.env.example) para o modelo completo:

| Variável | Obrigatória? | Exemplo / Padrão | Uso |
|---|---|---|---|
| `SERVER_PORT` | Sim | `8080` | Porta HTTP da API Spring Boot |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | Sim | `localhost` / `5432` / `amde_digital` | Endereço e banco alvo |
| `DB_SCHEMA` | Sim | `amde_digital` | Schema exclusivo da aplicação |
| `DB_APP_USER` / `DB_APP_PASSWORD` | Sim | `amde_digital_app` / `<definir>` | Credenciais DML usadas pelo HikariCP/JPA |
| `DB_OWNER_USER` / `DB_OWNER_PASSWORD` | Sim | `amde_digital_owner` / `<definir>` | Credenciais DDL usadas exclusivamente pelo Liquibase |
| `SPRING_LIQUIBASE_DEFAULT_SCHEMA` | Sim | `amde_digital` | Garante aplicação das migrations fora de `public` |
| `JWT_SECRET` | Sim | `<chave-local-256-bits>` | Assinatura de tokens JWT |
| `UPLOAD_DIR` | Sim | `uploads/prontuarios` | Diretório base local de armazenamento de PDFs |

---

## Diretrizes de Mapeamento JPA / Hibernate 6 + PostgreSQL 18

Ao implementar as `@Entity` derivadas das specs, siga obrigatoriamente as convenções registradas em [`AGENTS.md`](../AGENTS.md):
- Declarar `@Table(name = "<tabela_singular>", schema = "amde_digital")`.
- Mapear chaves primárias `id` (`bigint generated always as identity`) com `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;`.
- Mapear colunas `text` e domínios (`telefone`, `cpf`, `cep`) com `@Column(..., columnDefinition = "text")`.
- Mapear colunas `timestamptz` com `java.time.OffsetDateTime`.
- Mapear `agendamento_paciente` com chave composta `@EmbeddedId` (`AgendamentoPacienteId`).
- Anotar `LogAuditoria` com `@org.hibernate.annotations.Immutable`.

---

## Execução e Validação de Banco (Changelogs Atuais)

Os comandos de build/execução Java (`./mvnw spring-boot:run`, `./mvnw test`) serão ativados após a geração do projeto Spring Boot nas specs `001+`. Já a validação das migrations SQL pode ser executada desde já:

```bash
# 1. Subir banco PostgreSQL 18 com bootstrap
docker compose up -d postgres

# 2. Validar formatação dos scripts SQL
sqlfluff lint --dialect postgres backend/src/main/resources/db/changelog/migrations/ data/verificacao.sql

# 3. Após aplicar migrations com Liquibase, executar verificação estrutural
psql -h localhost -U postgres -d amde_digital -f data/verificacao.sql
```

---

## Vínculo com a Documentação Acadêmica (TG) e SDD

- **Modelo de dados e DER:** [DER](../.specify/reference/data-model/der.md) e [Dicionário de Dados](../.specify/reference/data-model/dicionario-de-dados.md).
- **Arquitetura:** [Visão Arquitetural](../.specify/reference/architecture/visao-arquitetural.md) e [Diagrama de Classes](../.specify/reference/architecture/diagrama-classes.md).
- **Requisitos:** [Especificação de Requisitos](../.specify/reference/requirements/especificacao-requisitos.md).
- **Contexto e Checklist de Banco:** [`AGENTS.md`](../AGENTS.md).
- **Planejamento de testes:** consulte as tarefas em `.specify/specs/`.
- **Monografia de referência:** [PDF](../.specify/reference/references/monografia-amde-digital.pdf).
