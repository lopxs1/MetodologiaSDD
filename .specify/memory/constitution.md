# Constituição do Projeto: AMDE Digital — Controle de Prontuário

> **Projeto:** AMDE Digital — Controle de Prontuário  
> **Instituição Parceira:** Associação Amigos dos Deficientes (AMDE) — Sorocaba/SP  
> **Equipe de Desenvolvimento:**  
> - Gustavo Ferreira Lopes  
> - Helena Melchiades de Faria  
> - Kaiky Saquette Pereira  
> - Vinicius Firmino Oliveira de Souza  
> **Orientador:** Prof. Antonio Cesar de Barros Munari  
> **Versão da Constituição:** 2.0.0  
> **Última alteração:** 2026-11-14  
> **Finalidade:** Definir os princípios inegociáveis que valem para todo o projeto. Toda spec, plano e implementação deve respeitá-los. Em caso de conflito entre uma spec e esta constituição, a constituição prevalece, a menos que a spec registre explicitamente a exceção e sua justificativa.

---

## 1. Princípios Gerais

1. **A especificação é a fonte da verdade:** Mudanças de comportamento do sistema começam na spec; o código é derivado dela. Correções e alterações nunca são feitas diretamente no código-fonte sem atualização da respectiva especificação.
2. **Separação estrita "O Quê" vs. "Como":** A spec descreve o **quê** e o **porquê** em linguagem de negócio (domínio clínico e administrativo da AMDE). O plano técnico (`plan.md`) descreve o **como** (decisões técnicas, bibliotecas, endpoints e algoritmos). Specs não mencionam tabelas, DDL nem tipos SQL.
3. **Nada de suposições silenciosas:** Quando a especificação for omissa, ambígua ou incompleta, a IA deve formular perguntas de esclarecimento antes de tomar decisões de projeto. Nenhuma escolha crítica (cardinalidade, exclusão, máscara e restrição) pode passar despercebida. Toda suposição mantida deve ser explicitamente registrada no plano técnico.
4. **Rastreabilidade total:** Cada entidade, atributo, regra de negócio, tela e tarefa de implementação deve ser diretamente rastreável a um requisito funcional (RF), requisito não funcional (RNF) e cenário BDD da spec.
5. **Consistência entre artefatos:** Modelo conceitual (`domain-model.md`), entidades, regras de negócio (RNs), telas e cenários de aceitação descrevem o mesmo domínio e não podem se contradizer. Nenhum artefato prevalece automaticamente sobre outro: divergências são apontadas pela IA como dúvida e resolvidas por decisão humana explícita (ver Seção 5.2).

---

## 2. Stack e Restrições Técnicas

- **Back-end:** Java 17 LTS com Spring Boot 3 (Spring Security 6, Spring Data JPA / Hibernate, MapStruct, Maven).
- **Front-end:** React 18+ (SPA) com TypeScript, Vite, Tailwind CSS, Shadcn/ui (Radix UI) e Axios.
- **Banco de Dados:** PostgreSQL 18 (SGBD Relacional ACID), operando no schema dedicado `amde_digital` com segregação de roles (`amde_digital_owner` para DDL/migrations e `amde_digital_app` para DML da aplicação).
- **Controle de Evolução de Banco:** Liquibase com versionamento formal via changelogs em formato SQL (`--liquibase formatted sql`), precedido pelo script único de preparação de ambiente (`data/00_bootstrap.sql`), com validação automática obrigatória via `sqlfluff` e `data/verificacao.sql` e suporte obrigatório a rollback.
- **Autenticação & Autorização:** Stateless via tokens JWT (`io.jsonwebtoken`), senhas protegidas via algoritmo BCrypt (RN01) e controle de acesso baseado em perfis (`ADMINISTRADOR`, `MEDICO`, `PACIENTE`).
- **Armazenamento de Arquivos e Infraestrutura:** Docker e Docker Compose para ambiente local; CI/CD via GitHub Actions; armazenamento de PDFs de prontuário em sistema de arquivos local (desenvolvimento) e AWS S3 (produção) sob o caminho padronizado `uploads/prontuarios/paciente-{id}/{uuid}.pdf`, com distribuição do front-end via AWS CloudFront.
- **Testes Automatizados:** JUnit 5, Mockito e Spring Boot Test no back-end; Vitest e Testing Library no front-end; todo requisito funcional deve possuir ao menos um cenário de teste de aceitação automatizado correspondente.

---

## 3. Princípios de Modelagem e Governança de Dados

### 3.1 Convenções
- **Schema Exclusivo:** Todos os objetos do sistema pertencem ao schema `amde_digital`. O schema `public` contém exclusivamente extensões (`unaccent`, `pg_trgm`) e jamais objetos de negócio.
- **Nomenclatura no Banco:** Identificadores estritamente em `snake_case` minúsculo, português sem acentos e sem cedilha, nunca entre aspas e sem prefixos de tipo (`tb_`, `vw_`). Todas as tabelas são nomeadas no **singular**: `usuario`, `funcionario`, `paciente`, `categoria`, `registro_prontuario`, `agendamento`, `agendamento_paciente`, `log_auditoria`.
- **Nomes por Extenso e Abreviações Permitidas:** Nomes descritivos por extenso (respeitando o limite de 63 caracteres do PostgreSQL). É proibido usar abreviações fora da lista fechada (`qtd`, `num`, `cpf`, `cnpj`, `cep`, `rg`). Ex.: `data_nascimento` (nunca `data_nasc`), `descricao` (nunca `desc_paciente`), `data_fim` (nunca `data_final`), `ip_origem` (nunca `ip`).
- **Chave Primária (`id`):** Em todas as tabelas de entidade, a chave primária chama-se `id` com tipo `bigint generated always as identity` (mapeada como `Long` no JPA). É proibido usar `serial` ou `bigserial`. Na tabela associativa `agendamento_paciente`, utiliza-se chave primária composta `(agendamento_id, paciente_id)`.
- **Chaves Estrangeiras (`<tabela_referenciada>_id`):** Nomeadas no formato `<tabela_referenciada>_id` com tipo `bigint` (`usuario_id`, `paciente_id`, `categoria_id`, `funcionario_id`, `agendamento_id`) ou qualificadas pelo papel semântico no auto-relacionamento (`registro_substituido_id` em `registro_prontuario`).
- **Tipos de Dados e Domínios:** Texto em geral utiliza `text` com limites e formatos validados por `CHECK` (`char_length(trim(...))`) ou `DOMAIN`. Nunca usar `varchar(n)` arbitrário nem `char(n)`. Campos recorrentes de formato fixo utilizam domínios dedicados (`telefone`, `cpf`, `cep`), armazenando apenas dígitos sem máscara (a máscara é responsabilidade da interface). Valores monetários/exatos utilizam `numeric(p,s)` (nunca `float`, `real`, `double precision` ou `money`). Datas puras utilizam `date` (`LocalDate` no Java); data e hora utilizam `timestamptz` (`OffsetDateTime` ou `Instant` no Java; nunca `timestamp` sem fuso). Domínios pequenos e estáveis utilizam `text` + `CHECK (coluna in (...))` ou tabela de referência (nunca `enum` nativo do PostgreSQL). `jsonb` só é permitido com justificativa escrita no comentário da coluna (nunca `json`).
- **Campos Booleanos:** Nomeados com prefixo que indique pergunta ou estado claro (`possui_presenca_funcionario`, `possui_presenca`), tipo `boolean` com valor padrão definido.
- **Auditoria Temporal Padrão:** Toda tabela de negócio (`usuario`, `funcionario`, `paciente`, `categoria`, `registro_prontuario`, `agendamento`, `agendamento_paciente`) possui `criado_em timestamptz not null default now()` e `atualizado_em timestamptz not null default now()`, sendo `atualizado_em` mantido pela função/trigger `definir_atualizado_em()` (`tg_<tabela>_atualizado_em`). A tabela `log_auditoria`, por ser estritamente imutável (*append-only*), possui apenas `criado_em timestamptz not null default now()`, sem `atualizado_em`, sem `excluido_em` e sem trigger de atualização.
- **Padrão de Nomes de Constraints, Índices e Triggers:** Todos devem ser nomeados explicitamente:
  - Chave Primária: `pk_<tabela>`
  - Chave Estrangeira: `fk_<tabela>_<ref>`
  - Índice da FK: `ix_<tabela>_<ref>` (mesmo sufixo da FK com prefixo `ix_`, declarado imediatamente após a FK, exceto quando a coluna já é a primeira coluna de uma PK ou índice único existente)
  - Unicidade (constraint ou índice único): `uq_<tabela>_<colunas>`
  - Check: `ck_<tabela>_<regra>`
  - Outros índices: `ix_<tabela>_<colunas>`
  - Trigger de auditoria: `tg_<tabela>_atualizado_em`
- **Anexos e Identificadores Físicos:** Arquivos de prontuário são restritos ao formato PDF com tamanho máximo de 10MB (RN05). O nome original do arquivo é salvo em `nome_arquivo_original` e o arquivo físico é renomeado com `UUID` único em `nome_arquivo_armazenado` (`uq_registro_prontuario_arquivo_armazenado`) (RN07).

### 3.2 Integridade e Divisão de Responsabilidades
- **O Banco de Dados garante:**
  - `NOT NULL` é o padrão; qualquer coluna anulável exige o motivo explicado no `COMMENT ON COLUMN`.
  - Toda relação tem chave estrangeira declarada via `ALTER TABLE`, seguida imediatamente da criação do seu índice (`CREATE INDEX ix_<tabela>_<ref>`), exceto quando a coluna já for a primeira coluna de uma PK ou índice único existente.
  - Unicidade simples via constraint `UNIQUE` (`uq_`) e unicidade com expressão ou soft delete via índice único parcial/funcional com prefixo `uq_` (ex.: `uq_usuario_email_ativo ON usuario (lower(email)) WHERE excluido_em IS NULL`).
  - Regras simples de domínio via `CHECK` (`ck_`), incluindo `ck_agendamento_periodo CHECK (data_fim > data_inicio)`, limites de tamanho/não-vazio de textos e valores válidos de domínio.
- **A Aplicação garante:** Regras dependentes de múltiplos estados, contexto temporal e perfil de acesso (ex.: bloqueio de sobreposição de horários da agenda do mesmo profissional com retorno `HTTP 409 Conflict` conforme RN13, validação de PDF < 10MB conforme RN05 e autorização RBAC conforme RN03, RN06 e RN15).
- **Relacionamentos N:N:** Sempre materializados por meio de tabela associativa explícita com chave primária composta e atributos próprios do vínculo (ex.: `agendamento_paciente` com PK composta `(agendamento_id, paciente_id)` e atributo `possui_presenca`).
- **Listas de Valores Fixos:** Devem utilizar Enums tipados na aplicação (`@Enumerated(EnumType.STRING)`) em `UPPER_SNAKE_CASE` sem acento e colunas `text` com constraints `CHECK` no banco de dados (ex.: `nivel_acesso`, `grau_suporte`, `tipo_agendamento`, `status` do agendamento e `acao` de auditoria). Classificações estruturadas em entidade própria no modelo de domínio (como `categoria` de prontuário) são materializadas em tabela dedicada vinculada por chave estrangeira (`categoria_id`).
- **Exceções e Alinhamento com Spring Data JPA / Hibernate:** Mapear explicitamente `@Table(name = "<tabela>", schema = "amde_digital")`, colunas `text` e domínios (`telefone`, `cpf`, `cep`) com `@Column(columnDefinition = "text")`, colunas `timestamptz` com `OffsetDateTime` (ou `Instant`), a chave composta de `agendamento_paciente` com `@EmbeddedId` (`AgendamentoPacienteId`) e a entidade `LogAuditoria` com `@Immutable`. Todas as convenções e exceções de ORM devem estar documentadas em [`AGENTS.md`](../../AGENTS.md).

### 3.3 Exclusão, Ciclo de Vida e Histórico
- **Exclusão Lógica Padronizada (*Soft Delete* via `excluido_em timestamptz`):** Substitui qualquer coluna booleana `ativo`. Nas tabelas que possuem inativação/exclusão lógica (`usuario`, `funcionario`, `paciente`, `registro_prontuario`), `excluido_em IS NULL` representa registro ativo e `excluido_em IS NOT NULL` registra a data/hora exata da inativação lógica. A regra de soft delete deve ser documentada no `COMMENT ON TABLE` de cada tabela afetada, e restrições de unicidade sobre registros ativos devem usar índices únicos parciais (`WHERE excluido_em IS NULL`). Agendamentos cancelados preservam seu histórico alterando a coluna `status` para `CANCELADO` (sem `excluido_em`), e registros em `log_auditoria` são estritamente imutáveis (*append-only*).
- **Ciclo de Vida de Arquivos de Prontuário:** O arquivamento lógico de documentos preenche `excluido_em` em `registro_prontuario` (e/ou classifica na categoria `Arquivado`). Já a operação de exclusão de prontuário (UC12 / RN10), restrita a `ADMINISTRADOR` e `MEDICO`, exige o registro prévio em `log_auditoria` e a remoção do arquivo físico do storage antes da inativação/exclusão do metadado no banco.
- **Versionamento de Documentos:** A substituição de laudos, exames e receituários deve preservar o histórico clínico por meio do auto-relacionamento `registro_substituido_id` em `registro_prontuario` (RN08).

### 3.4 Proteção de Dados Sensíveis, Documentação no Catálogo e LGPD
- **Documentação Obrigatória no Banco (`COMMENT ON`):** `COMMENT ON TABLE` é obrigatório em todas as tabelas (documentando finalidade, regra de soft delete `excluido_em` ou imutabilidade). `COMMENT ON COLUMN` é obrigatório em colunas não óbvias, em toda coluna anulável (com o motivo de aceitar `NULL`) e em colunas com dados pessoais/sensíveis.
- **Marcação e Minimização LGPD:** Toda coluna com dado pessoal inicia o comentário com `[LGPD: pessoal]` + justificativa de uso (ex.: `nome`, `email`, `num_celular`, `data_nascimento`, `pais`, `estado`, `cidade`, `rua`, `crm`, `cargo`, `ip_origem`). Toda coluna com dado pessoal sensível (dados clínicos, saúde e menores atendidos pela AMDE) inicia o comentário com `[LGPD: sensível]` + justificativa de uso (`grau_suporte`, `descricao`, `titulo_documento`, `nome_arquivo_original`, `nome_arquivo_armazenado`, `possui_presenca`). Dados de menores seguem rigoroso critério de minimização e nunca vão para seeds ou repositórios.
- **Logs Seguros:** Dados pessoais sensíveis, senhas (mesmo em hash) e conteúdos de prontuários **nunca** aparecem em logs de console, arquivos de depuração ou mensagens técnicas de erro enviadas ao cliente.
- **Trilha de Auditoria Obrigatória:** Toda operação sensível (`LOGIN`, `UPLOAD`, `DOWNLOAD`, `EXCLUSAO` e `AGENDAMENTO`) deve disparar gravação assíncrona obrigatória em `log_auditoria` via `AuditoriaService` (UC20 / RN16).

### 3.5 Evolução do Esquema, Bootstrap e Verificação Automática
- **Estrutura em 4 Camadas:**
  1. `data/00_bootstrap.sql`: script único executado por superusuário (criação de roles `amde_digital_owner` e `amde_digital_app`, database `amde_digital` com UTF-8/ICU `pt-BR` e `America/Sao_Paulo`, extensões `unaccent` e `pg_trgm` em `public`, schema `amde_digital`, `ALTER DEFAULT PRIVILEGES`, `search_path` e timeouts).
  2. Migration inicial Liquibase em formato SQL (`--liquibase formatted sql`) executada pela role `amde_digital_owner`.
  3. Migrations incrementais: uma mudança lógica por arquivo; toda nova tabela vem no mesmo arquivo com suas FKs, índices e comentários.
  4. Seeds sintéticos em arquivos separados das migrations estruturais.
- **Ordem Obrigatória dentro da Migration Inicial:** (1) Domínios (`CREATE DOMAIN`); (2) `CREATE TABLE` com colunas, PK, UNIQUE e CHECK; (3) FKs via `ALTER TABLE`, cada uma seguida imediatamente do `CREATE INDEX` correspondente; (4) Demais índices; (5) Função `definir_atualizado_em()` e triggers de auditoria; (6) `COMMENT ON`.
- **Verificação Automática e Checklist de PR:** Todo changelog SQL deve passar pelo formatador/linter `sqlfluff` (dialeto `postgres`), pelas consultas de `data/verificacao.sql` (com zero linhas retornadas nas 7 verificações estruturais + revisão do inventário LGPD) e pelo Checklist de Revisão de Banco de Dados (documentado em [`AGENTS.md`](../../AGENTS.md)) antes do merge.
- **Imutabilidade em Produção:** Uma vez aplicada em ambiente compartilhado ou de produção, uma migration do Liquibase é imutável e torna-se histórico permanente. Correções são feitas exclusivamente por nova migration de compensação.
- **Migrations Incrementais:** Toda alteração no modelo de dados gera um novo changelog incremental no Liquibase contendo migração ascendente (`UP`) e procedimento de reversão seguro (`--rollback`).
- **Veto a Operações Destrutivas Silenciosas:** A IA está proibida de gerar scripts que descartem tabelas (`DROP TABLE`), removam colunas (`DROP COLUMN`) ou convertam tipos com perda de dados sem alerta explícito e aprovação humana da equipe.

---

## 4. Princípios de Interface e Experiência do Usuário

### 4.1 Design System e Tokens
- **Biblioteca Base:** Componentes construídos com Tailwind CSS e Shadcn/ui (Radix UI), promovendo acessibilidade e padronização.
- **Design Tokens:** Cores institucionais da AMDE, cores semânticas de status/perfil, tipografia (`Inter` e `JetBrains Mono`), espaçamentos e raios de borda são formalizados em [`design/tokens.json`](../../design/tokens.json) e documentados em [`memory/design-system.md`](design-system.md).
- **Proibição de Valores Literais:** O código do front-end não deve declarar cores hexadecimais nem fontes soltas diretamente nos componentes, consumindo exclusivamente os tokens declarados.

### 4.2 Padrões de Interação e Usabilidade
- **Formulários:** Rótulos visíveis acima do campo, identificação clara de campos obrigatórios (`*`), validação ao desfocar o campo (*onBlur*) e no envio, bloqueando o botão de submissão enquanto houver erros e exibindo mensagens humanizadas junto ao componente.
- **Listagens e Tabelas:** Paginação na base da tabela (compatível com consultas paginadas da API), linha de busca textual no topo com filtro por nome e e-mail, e ordenação ascendente/descendente (`ASC`/`DESC`) visível nos cabeçalhos das colunas.
- **Ações Destrutivas:** Exclusão de usuários ou documentos clínicos requer confirmação explícita em modal de diálogo com fundo escurecido (*backdrop*) informando o impacto da ação.
- **Estados de Tela Obrigatórios:** Toda tela que consome APIs deve implementar 4 estados visuais bem definidos: *Carregando (Skeleton simulando a estrutura da tabela/card)*, *Vazio (Ícone ilustrativo e texto orientativo)*, *Erro (Alerta amigável com ação de recarregar)* e *Sucesso (Dados renderizados)*.

### 4.3 Acessibilidade e Responsividade
- **Conformidade WCAG:** Aderência às diretrizes WCAG 2.1 nível AA, com relação mínima de contraste de 4.5:1 para textos normais.
- **Navegação por Teclado:** Todas as ações devem ser acessíveis via teclado (`Tab`, `Enter`, `Esc` para fechar modais), com indicadores visuais de foco nítidos.
- **Responsividade:** Layout estruturado em Painel Administrativo com Menu Lateral Fixo (*Sidebar*) e Cabeçalho Superior (*Header*), adaptável com breakpoints Tailwind para desktops da clínica e dispositivos móveis dos profissionais.

### 4.4 Exibição de Dados Sensíveis
- Dados pessoais de contato e identificação (como `num_celular` e documentos) devem ser renderizados nas listagens com máscara protetora por padrão (ex.: `(15) *****-1234`), sendo revelados integralmente apenas na visualização detalhada mediante ação explícita de usuário com perfil autorizado (`ADMINISTRADOR` ou `MEDICO`).

---

## 5. Comportamento Esperado da IA

### 5.1 Ao ler uma Especificação (`spec.md`)
1. Mapear todas as entidades, atributos e fluxos de negócio mencionados, inclusive os presentes em wireframes e critérios BDD.
2. Checar a completude dos dados (obrigatoriedade, unicidade, cardinalidade, ciclo de vida e histórico).
3. Caso identifique omissões ou contradições, a IA **deve formular perguntas de esclarecimento**, sem tomar decisões arquiteturais por conta própria.

### 5.2 Verificação de Consistência entre Artefatos
Antes de gerar o plano técnico e sempre que uma especificação mudar, a IA deve checar:
- **Modelo Conceitual × Entidades:** Toda entidade do DER (`domain-model.md`) deve possuir correspondência nas especificações funcionais e vice-versa.
- **Entidades × Regras de Negócio:** Toda entidade citada em uma RN (regras RN01 a RN16) deve existir formalmente no modelo.
- **Telas × Entidades:** Todo campo exibido ou coletado na interface deve ter uma entidade de destino ou cálculo de origem declarado.
- **Cenários BDD × Requisitos:** Todo cenário de aceite deve testar ao menos um RF ou RN, e todo RF deve possuir ao menos um cenário de teste.
- **Linguagem Ubíqua:** Termos como "Usuário", "Funcionário / Profissional" (materializado na tabela `funcionario` e autorizado sob o perfil `MEDICO`), "Paciente", "Grau de Suporte", "Prontuário Digital / PEP", "Categoria de Prontuário", "Agendamento", "Presença" e "Log de Auditoria" devem manter rigorosamente o mesmo significado em todos os documentos.

### 5.3 Ao gerar o Delta do Modelo de Dados (`data-model.md`)
- Derivar o modelo lógico estritamente a partir do modelo conceitual em `domain-model.md`.
- Apresentar a tabela de **Mapeamento Conceitual ➔ Materialização Lógica**.
- Distinguir o que é *Definido na Spec*, o que é *Derivado da Constituição* e o que é *Suposição Técnica com Justificativa*.
- Gerar scripts Liquibase com validação de tipos e rollback.

### 5.4 Ao receber uma alteração de Requisito
- Atualizar o modelo conceitual em `domain-model.md` caso a regra envolva cardinalidades ou novas entidades.
- Executar a checagem cruzada de consistência.
- Gerar nova migration Liquibase incremental (sem reescrever histórico).
- Identificar e regenerar apenas as telas e endpoints diretamente impactados pela mudança.

---

## 6. Governança e Versionamento

- As alterações nesta constituição seguem versionamento semântico:
  - **MAJOR (ex.: 2.0.0):** Alteração de princípios fundamentais, troca de banco de dados ou inversão de regras de governança. Exige revisão completa de todas as specs ativas.
  - **MINOR (ex.: 1.3.0):** Adição de novos princípios, regras de auditoria ou novas restrições técnicas.
  - **PATCH (ex.: 1.2.1):** Correção ortográfica, refinamento de redação ou clarificação de diretriz sem alteração de escopo.
- A aprovação de alterações na Constituição cabe à equipe técnica do projeto e ao orientador acadêmico.
