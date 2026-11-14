# Banco de Dados, Bootstrap e Verificação — AMDE Digital

Este diretório centraliza os scripts de preparação do ambiente PostgreSQL 18, verificação estrutural automática e diretrizes de dados sintéticos do projeto **AMDE Digital**.

> **Conformidade LGPD:** É estritamente proibido versionar dados reais de pacientes, responsáveis ou profissionais da AMDE. Utilize exclusivamente massas de dados sintéticas para testes locais e seeds.

---

## Estrutura de Arquivos e Responsabilidades

| Arquivo / Caminho | Papel | Quem / Quando Executa |
|---|---|---|
| [`data/00_bootstrap.sql`](00_bootstrap.sql) | Cria roles (`amde_digital_owner` e `amde_digital_app`), database `amde_digital` (UTF-8, ICU `pt-BR`, `America/Sao_Paulo`), extensões `unaccent` e `pg_trgm` em `public`, schema `amde_digital`, `ALTER DEFAULT PRIVILEGES`, `search_path` e timeouts de proteção. | Executado **uma única vez** por superusuário (`postgres`), automaticamente na primeira subida do `docker-compose.yml` (`/docker-entrypoint-initdb.d/`). |
| [`backend/src/main/resources/db/changelog/db.changelog-master.yaml`](../backend/src/main/resources/db/changelog/db.changelog-master.yaml) | Changelog mestre do Liquibase que orquestra a execução ordenada das migrations SQL. | Executado pelo Liquibase usando a role **`amde_digital_owner`**. |
| [`backend/src/main/resources/db/changelog/migrations/001_esquema_inicial.sql`](../backend/src/main/resources/db/changelog/migrations/001_esquema_inicial.sql) | Migration inicial em formato `--liquibase formatted sql` contendo domínios, tabelas, FKs + índices, índices adicionais, função/triggers de auditoria e comentários LGPD. | Executado pelo Liquibase na inicialização do backend ou via Maven/CLI Liquibase. |
| [`data/verificacao.sql`](verificacao.sql) | Conjunto de 7 consultas oficiais + 2 verificações complementares (todas devem retornar **zero linhas**) + consulta de inventário `[LGPD: ...]`. | Executado antes de cada entrega ou Pull Request que altere o banco de dados. |

---

## Execução e Validação

### 1. Inicializar PostgreSQL 18 via Docker Compose (executa `00_bootstrap.sql` no primeiro start)

```bash
docker compose up -d postgres
```

> **Nota sobre o `00_bootstrap.sql`:** O container inicializa conectado a `POSTGRES_DB=postgres` para que `CREATE DATABASE amde_digital` execute sem conflito de banco já existente. Nunca versione senhas reais: informe `DB_OWNER_PASSWORD` e `DB_APP_PASSWORD` via arquivo `.env` local (ignorado pelo Git).

### 2. Executar as Consultas de Verificação Automática

Após aplicar as migrations Liquibase no schema `amde_digital`:

```bash
psql -h localhost -U postgres -d amde_digital -f data/verificacao.sql
```

- As **consultas 1 a 9** devem retornar **zero linhas**.
- A **consulta final** lista todas as colunas marcadas com `[LGPD: pessoal]` ou `[LGPD: sensível]` para revisão.

### 3. Validar Formatação SQL com `sqlfluff`

```bash
sqlfluff lint --dialect postgres backend/src/main/resources/db/changelog/migrations/ data/verificacao.sql
```

*(O script `data/00_bootstrap.sql` utiliza metacomandos `psql` como `\getenv` e `\connect`, portanto é validado diretamente pela execução via `psql`.)*

---

## Referências do Projeto

- [DER Físico/Conceitual](../.specify/reference/data-model/der.md)
- [Dicionário de Dados](../.specify/reference/data-model/dicionario-de-dados.md)
- [Constituição do Projeto (Seção 3)](../.specify/memory/constitution.md)
- [Contexto de IA e Checklist de PR](../AGENTS.md)
