# Contexto do Repositório para IA — AMDE Digital

Este arquivo define as regras operacionais obrigatórias para assistentes de IA e desenvolvedores no projeto **AMDE Digital**, alinhando a metodologia **Spec-Driven Development (SDD)** (`.specify/memory/constitution.md` v2.0.0) às **Diretrizes de Banco de Dados PostgreSQL 18**.

---

# Convenções de banco de dados (PostgreSQL 18)

Schema: `amde_digital`. Nunca criar objetos de negócio em `public` (`public` contém apenas extensões como `unaccent` e `pg_trgm`).
Bootstrap em `data/00_bootstrap.sql` (executado 1x por superusuário).
Migrations Liquibase (`--liquibase formatted sql`) em `backend/src/main/resources/db/changelog/migrations/`; nunca alterar o banco fora delas.

## Nomes
- `snake_case` minúsculo; nunca usar identificadores entre aspas.
- Português sem acentos e sem cedilha. Tabelas no **singular**, sem prefixo (`tb_`, `vw_`):
  `usuario`, `funcionario`, `paciente`, `categoria`, `registro_prontuario`, `agendamento`, `agendamento_paciente`, `log_auditoria`.
- PK: `id` (`bigint generated always as identity`). Em associativa N:N (`agendamento_paciente`), PK composta `(agendamento_id, paciente_id)`.
- FK: `<tabela_referenciada>_id` (`usuario_id`, `paciente_id`, `categoria_id`, `funcionario_id`, `agendamento_id`). Com auto-relacionamento ou múltiplas FKs para a mesma tabela, qualificar pelo papel (`registro_substituido_id`).
- Nomes por extenso. Abreviações permitidas **apenas** da lista fechada: `qtd`, `num`, `cpf`, `cnpj`, `cep`, `rg`.
- Booleanos com prefixo claro de pergunta/estado: `possui_presenca_funcionario`, `possui_presenca`.
- Datas e horários pelo significado: `data_nascimento` (`date`), `criado_em`, `atualizado_em`, `excluido_em`, `data_inicio`, `data_fim` (`timestamptz`).
- Constraints e índices sempre nomeados explicitamente:
  `pk_<tabela>`, `fk_<tabela>_<ref>`, `ix_<tabela>_<ref>` (índice da FK), `uq_<tabela>_<colunas>`, `ck_<tabela>_<regra>`, `ix_<tabela>_<colunas>`, `tg_<tabela>_atualizado_em`.

## Tipos
- Texto: `text`. Limite de negócio e tamanho mínimo via `CHECK` (`char_length(trim(...))`) ou `DOMAIN`. Nunca `varchar(n)` arbitrário nem `char(n)`.
- Data/hora: `timestamptz`. Só data: `date`. Nunca `timestamp` sem fuso.
- Valores monetários/exatos: `numeric(p,s)`. Nunca `float`, `real`, `double precision` ou `money`.
- PK numérica: `bigint generated always as identity`. Nunca `serial` nem `bigserial`. FKs numéricas: `bigint`.
- UUID, se necessário: `uuid default uuidv7()`.
- Domínios pequenos e estáveis: `text` + `CHECK (col in (...))` ou tabela de referência. Nunca usar `enum` nativo do PostgreSQL.
- `jsonb` só com justificativa escrita no comentário da coluna. Nunca `json`.
- Usar os domínios existentes no schema `amde_digital` (`telefone`, `cpf`, `cep`). Armazenar apenas dígitos sem máscara.

## Integridade
- `NOT NULL` é o padrão; toda coluna anulável exige o motivo explicado no `COMMENT ON COLUMN`.
- Toda relação tem FK declarada via `ALTER TABLE`. Toda FK tem índice criado imediatamente após sua criação (`ix_<tabela>_<ref>`), exceto se a coluna já for a primeira coluna de uma PK ou índice único existente.
- Regras simples de domínio ficam em `CHECK` no banco (`data_fim > data_inicio`, extensões/tamanhos, domínios válidos).
- Unicidade simples: constraint `UNIQUE` (`uq_`). Com expressão ou soft delete: índice único parcial/funcional com prefixo `uq_` (`uq_usuario_email_ativo on usuario (lower(email)) where excluido_em is null`).
- Não criar índice redundante sobre colunas já cobertas por PK ou `UNIQUE`.

## Colunas padrão, Soft Delete e Documentação (LGPD)
- Toda tabela de negócio (`usuario`, `funcionario`, `paciente`, `categoria`, `registro_prontuario`, `agendamento`, `agendamento_paciente`): `criado_em` e `atualizado_em` (`timestamptz not null default now()`), com `atualizado_em` mantido pela função/trigger `definir_atualizado_em()`.
- `log_auditoria`: tabela imutável (*append-only*); possui apenas `criado_em timestamptz not null default now()` e **não** possui `atualizado_em`, `excluido_em` nem trigger de atualização.
- Soft delete padronizado com `excluido_em timestamptz` (`NULL` = ativo; `NOT NULL` = data/hora da inativação lógica) nas tabelas com exclusão lógica (`usuario`, `funcionario`, `paciente`, `registro_prontuario`), documentado no `COMMENT ON TABLE`.
- `COMMENT ON TABLE` obrigatório em todas as tabelas; `COMMENT ON COLUMN` obrigatório em colunas não óbvias, anuláveis e pessoais/sensíveis.
- Dado pessoal/sensível: comentário começa com `[LGPD: pessoal]` ou `[LGPD: sensível]` + justificativa de uso.

## Migrations
- Ordem obrigatória na migration inicial: 1. Domínios; 2. `CREATE TABLE` (colunas, PK, UNIQUE, CHECK); 3. FKs via `ALTER TABLE` seguidas imediatamente de `CREATE INDEX`; 4. Demais índices; 5. Função `definir_atualizado_em()` e triggers `tg_`; 6. `COMMENT ON`.
- Migrations incrementais: uma mudança lógica por arquivo; tabela nova vem com FKs, índices e comentários no mesmo arquivo, com bloco `--rollback`.
- Lógica mínima no banco: triggers só para `atualizado_em`. Sem regra de negócio extensa em PL/pgSQL.
- Seeds separados das migrations; nunca versionar dados reais da AMDE.

---

# Exceções e Convenções ORM (Spring Boot 3 / Spring Data JPA / Hibernate 6)

Para evitar divergências entre a validação de schema do Hibernate (`spring.jpa.hibernate.ddl-auto=validate`) e as convenções PostgreSQL 18 do schema `amde_digital`:

1. **Schema e Roles:**
   - A aplicação (`spring.datasource`) conecta sempre com `amde_digital_app` (apenas DML).
   - O Liquibase (`spring.liquibase`) conecta com `amde_digital_owner` (DDL/migrations) com `default-schema=amde_digital` e `liquibase-schema=amde_digital`.
   - Todas as `@Entity` declaram `@Table(name = "<tabela_singular>", schema = "amde_digital")`.
2. **Colunas `text` e Domínios (`telefone`, `cpf`, `cep`):**
   - Por padrão, o Hibernate mapeia `String` para `varchar(255)`. Portanto, **todo campo `String` mapeado para `text` ou `DOMAIN` (`telefone`, `cpf`, `cep`) deve declarar explicitamente `columnDefinition = "text"`** (ex.: `@Column(name = "nome", nullable = false, columnDefinition = "text")` e `@Column(name = "num_celular", columnDefinition = "text")`).
   - A remoção de máscara (dígitos apenas) e validação de tamanho/formato ocorrem nos DTOs/Services antes de persistir, em espelho às constraints `CHECK`/`DOMAIN` do banco.
3. **Chaves Primárias:**
   - PKs `bigint generated always as identity` são mapeadas como:
     `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;`
   - A tabela associativa `agendamento_paciente` é mapeada com `@EmbeddedId` (`AgendamentoPacienteId` contendo `Long agendamentoId` e `Long pacienteId`) e `@MapsId`.
4. **Timestamps com Fuso (`timestamptz`):**
   - Mapear `criado_em`, `atualizado_em`, `excluido_em`, `data_inicio` e `data_fim` com `java.time.OffsetDateTime` (ou `Instant`), nunca `LocalDateTime`.
5. **Enums (`@Enumerated(EnumType.STRING)`):**
   - Valores dos Enums Java (`NivelAcesso`, `GrauSuporte`, `TipoAgendamento`, `StatusAgendamento`, `AcaoAuditoria`) devem usar `UPPER_SNAKE_CASE` sem acento idêntico às constraints `CHECK` e ser anotados com `@Enumerated(EnumType.STRING)` + `@Column(..., columnDefinition = "text")`.
6. **Imutabilidade (`LogAuditoria`) e Soft Delete (`excluido_em`):**
   - A entidade `LogAuditoria` deve ser anotada com `@org.hibernate.annotations.Immutable` para impedir `UPDATE` via Hibernate.
   - Entidades com soft delete (`Usuario`, `Funcionario`, `Paciente`, `RegistroProntuario`) utilizam `@SQLRestriction("excluido_em IS NULL")` (ou filtro explícito `excluidoEm IS NULL` nos repositórios) para leituras padrão, sem nunca emitir `DELETE` físico em produção (exceto política controlada da RN10 quando aplicável).
7. **Tabelas de Controle do Liquibase (`databasechangelog` / `databasechangeloglock`):**
   - Como o Liquibase é executado com `liquibase-schema=amde_digital` para não criar objetos em `public`, suas 2 tabelas internas de controle (`databasechangelog` e `databasechangeloglock`) residem em `amde_digital`. A consulta nº 9 de `data/verificacao.sql` já ignora essas duas tabelas; eventuais apontamentos das consultas nº 1 ou nº 6 exclusivamente sobre `databasechangelog` decorrem da estrutura interna fixa do Liquibase e devem ser distinguidos das tabelas de negócio.

---

# Checklist de Revisão de Pull Request (Banco de Dados — Seção 11)

Anexar a todo Pull Request que altera o banco de dados:

- [ ] Consigo explicar cada comando desta migration.
- [ ] Consultas de verificação (`data/verificacao.sql`) executadas, todas as verificações com zero linhas (para objetos do domínio). `(auto)`
- [ ] Formatação e estilo conferidos com `sqlfluff lint --dialect postgres`. `(auto)`
- [ ] Tipos conferidos: `text`, `timestamptz`, valores exatos (`numeric(p,s)`), `bigint generated always as identity`. `(auto, em parte)`
- [ ] Cada coluna anulável tem motivo documentado no `COMMENT ON COLUMN`.
- [ ] Toda relação tem FK declarada via `ALTER TABLE`, e toda FK tem índice (`ix_`) ou está coberta por PK/UNIQUE. `(auto)`
- [ ] Regras simples de domínio estão em `CHECK` ou `DOMAIN`.
- [ ] Colunas únicas e anuláveis: comportamento com `NULL` e soft delete (`WHERE excluido_em IS NULL`) decidido.
- [ ] Nomes de tabelas, colunas, constraints (`pk_`, `fk_`, `uq_`, `ck_`) e índices (`ix_`) no padrão `snake_case` minúsculo sem acentos. `(auto, em parte)`
- [ ] Tabelas e colunas não óbvias com `COMMENT ON`. `(auto, para tabelas)`
- [ ] Dados pessoais e sensíveis marcados com `[LGPD: pessoal]` ou `[LGPD: sensível]` e justificados.
- [ ] Tabela nova vem com FKs, índices e comentários no mesmo arquivo.
- [ ] Nenhum dado real em seeds ou exemplos.
