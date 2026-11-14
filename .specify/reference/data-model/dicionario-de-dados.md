# Dicionário de Dados — AMDE Digital

> **Referência:** Modelo Físico do Banco de Dados PostgreSQL 18 (Schema `amde_digital`)  
> **Controle de Versão:** Liquibase Changelogs (`--liquibase formatted sql`)  
> **Conformidade:** LGPD (Lei nº 13.709/2018), Resolução CFM nº 1.821/2007 e Constituição do Projeto v2.0.0 (Seção 3)

---

## 0. Objetos Globais do Schema `amde_digital`

### Domínios (`CREATE DOMAIN`)
| Domínio | Tipo Base | Regra (`CHECK`) | Descrição |
|---|---|---|---|
| `amde_digital.telefone` | `text` | `VALUE ~ '^[0-9]{10,11}$'` | Telefone fixo ou celular brasileiro com DDD (10 ou 11 dígitos), armazenado sem máscara. |
| `amde_digital.cpf` | `text` | `VALUE ~ '^[0-9]{11}$'` | Cadastro de Pessoa Física (11 dígitos), armazenado sem máscara (disponível para futuras expansões cadastrais). |
| `amde_digital.cep` | `text` | `VALUE ~ '^[0-9]{8}$'` | Código de Endereçamento Postal brasileiro (8 dígitos), armazenado sem máscara. |

### Função de Auditoria Temporal
- **`amde_digital.definir_atualizado_em()` (`RETURNS trigger`):** Atualiza automaticamente `NEW.atualizado_em := now();` em eventos `BEFORE UPDATE` de todas as tabelas de negócio mutáveis.

---

## 1. Tabela: `amde_digital.usuario`

Armazena contas de acesso e dados cadastrais básicos de administradores, profissionais de saúde e pacientes da AMDE. Adota soft delete via `excluido_em` (`NULL` = registro ativo; `NOT NULL` = data/hora da inativação lógica).

| Coluna | Tipo SQL | Nulo? | Padrão | Restrições / Chaves | Descrição / Classificação LGPD e Justificativa |
|---|---|---|---|---|---|
| `id` | `bigint generated always as identity` | Não | Identity | `pk_usuario` | Identificador único do usuário. |
| `nome` | `text` | Não | — | `ck_usuario_nome` | `[LGPD: pessoal]` Nome completo do titular da conta para identificação civil e clínica nos atendimentos da AMDE. |
| `email` | `text` | Não | — | `ck_usuario_email`, `uq_usuario_email_ativo` | `[LGPD: pessoal]` E-mail utilizado como credencial de login e canal de comunicação institucional. Único entre usuários ativos. |
| `senha` | `text` | Não | — | `ck_usuario_senha` | Hash criptográfico da senha gerado exclusivamente via algoritmo BCrypt (RN01). |
| `data_nascimento` | `date` | Sim | — | `ck_usuario_data_nascimento` | `[LGPD: pessoal]` Data de nascimento para cálculo de faixa etária pediátrica e validação cadastral. **Anulável** porque contas administrativas iniciais podem ser criadas antes da complementação cadastral. |
| `nivel_acesso` | `text` | Não | — | `ck_usuario_nivel_acesso` | Perfil RBAC: `ADMINISTRADOR`, `MEDICO` ou `PACIENTE`. |
| `num_celular` | `telefone` | Sim | — | Domínio `telefone` | `[LGPD: pessoal]` Telefone celular/WhatsApp com DDD (10 a 11 dígitos, sem máscara) para contato sobre agendamentos. **Anulável** porque nem todo usuário possui telefone próprio no ato do pré-cadastro. |
| `pais` | `text` | Sim | `'Brasil'` | `ck_usuario_pais` | `[LGPD: pessoal]` País de residência do usuário. **Anulável** por ser dado complementar de endereço. |
| `estado` | `text` | Sim | — | `ck_usuario_estado` | `[LGPD: pessoal]` Sigla da Unidade Federativa (UF, 2 letras maiúsculas). **Anulável** por ser dado complementar de endereço. |
| `cidade` | `text` | Sim | — | `ck_usuario_cidade` | `[LGPD: pessoal]` Município de residência do usuário. **Anulável** por ser dado complementar de endereço. |
| `rua` | `text` | Sim | — | `ck_usuario_rua` | `[LGPD: pessoal]` Logradouro e número residencial do usuário. **Anulável** por ser dado complementar de endereço. |
| `criado_em` | `timestamptz` | Não | `now()` | — | Data e hora de criação do registro em UTC. |
| `atualizado_em` | `timestamptz` | Não | `now()` | Trigger `tg_usuario_atualizado_em` | Data e hora da última atualização cadastral. |
| `excluido_em` | `timestamptz` | Sim | `NULL` | — | Data e hora da inativação lógica do usuário. **Anulável** pois permanece `NULL` enquanto a conta estiver ativa. |

- **Constraints:** `pk_usuario (id)`, `ck_usuario_nome (char_length(trim(nome)) between 2 and 100)`, `ck_usuario_email (char_length(trim(email)) between 5 and 254 and position('@' in email) > 1)`, `ck_usuario_senha (char_length(trim(senha)) between 60 and 255)`, `ck_usuario_nivel_acesso (nivel_acesso in ('ADMINISTRADOR', 'MEDICO', 'PACIENTE'))`, `ck_usuario_data_nascimento (data_nascimento is null or (data_nascimento >= date '1900-01-01' and data_nascimento <= current_date))`, `ck_usuario_pais (pais is null or char_length(trim(pais)) between 2 and 60)`, `ck_usuario_estado (estado is null or estado ~ '^[A-Z]{2}$')`, `ck_usuario_cidade (cidade is null or char_length(trim(cidade)) between 2 and 100)`, `ck_usuario_rua (rua is null or char_length(trim(rua)) between 2 and 150)`.
- **Índices:** `uq_usuario_email_ativo` (único parcial em `(lower(email)) where excluido_em is null`), `ix_usuario_nome_trgm` (GIN `public.gin_trgm_ops` em `(nome) where excluido_em is null`), `ix_usuario_nivel_acesso` (`(nivel_acesso) where excluido_em is null`).
- **Trigger:** `tg_usuario_atualizado_em` (`BEFORE UPDATE`).

---

## 2. Tabela: `amde_digital.funcionario`

Extensão de dados profissionais para médicos, terapeutas e colaboradores da equipe multidisciplinar (especialização 1:1 de `usuario`). Adota soft delete via `excluido_em` (`NULL` = profissional ativo; `NOT NULL` = inativado logicamente, preservando histórico de agendamentos conduzidos).

| Coluna | Tipo SQL | Nulo? | Padrão | Restrições / Chaves | Descrição / Classificação LGPD e Justificativa |
|---|---|---|---|---|---|
| `id` | `bigint generated always as identity` | Não | Identity | `pk_funcionario` | Identificador único do profissional. |
| `usuario_id` | `bigint` | Não | — | `fk_funcionario_usuario`, `uq_funcionario_usuario` | Referência 1:1 para a conta de usuário em `usuario (id)`. |
| `crm` | `text` | Sim | — | `ck_funcionario_crm` | `[LGPD: pessoal]` Registro em conselho de classe profissional (CRM, CRP, CREFITO, CRFa). **Anulável** porque colaboradores administrativos ou funções pedagógicas de apoio podem não possuir registro em conselho médico. |
| `cargo` | `text` | Não | — | `ck_funcionario_cargo` | `[LGPD: pessoal]` Especialidade clínica ou função exercida pelo profissional no Centro de Excelência em Autismo da AMDE. |
| `criado_em` | `timestamptz` | Não | `now()` | — | Data e hora de criação do vínculo profissional. |
| `atualizado_em` | `timestamptz` | Não | `now()` | Trigger `tg_funcionario_atualizado_em` | Data e hora da última atualização do registro. |
| `excluido_em` | `timestamptz` | Sim | `NULL` | — | Data e hora da desativação lógica do vínculo profissional. **Anulável** pois permanece `NULL` enquanto o profissional estiver ativo. |

- **Constraints:** `pk_funcionario (id)`, `uq_funcionario_usuario unique (usuario_id)` *(cobre também a indexação da FK `usuario_id`)*, `fk_funcionario_usuario (usuario_id references usuario (id) on delete restrict)`, `ck_funcionario_crm (crm is null or char_length(trim(crm)) between 3 and 30)`, `ck_funcionario_cargo (char_length(trim(cargo)) between 2 and 80)`.
- **Índices:** `uq_funcionario_usuario` (automático via constraint `UNIQUE`), `ix_funcionario_cargo` (`(cargo) where excluido_em is null`).
- **Trigger:** `tg_funcionario_atualizado_em` (`BEFORE UPDATE`).

---

## 3. Tabela: `amde_digital.paciente`

Extensão clínica das crianças e adolescentes com Transtorno do Espectro Autista (TEA) assistidos pela AMDE (especialização 1:1 de `usuario`). Adota soft delete via `excluido_em` (`NULL` = paciente ativo; `NOT NULL` = inativado logicamente, preservando prontuários pelo prazo legal do CFM).

| Coluna | Tipo SQL | Nulo? | Padrão | Restrições / Chaves | Descrição / Classificação LGPD e Justificativa |
|---|---|---|---|---|---|
| `id` | `bigint generated always as identity` | Não | Identity | `pk_paciente` | Identificador único do paciente. |
| `usuario_id` | `bigint` | Não | — | `fk_paciente_usuario`, `uq_paciente_usuario` | Referência 1:1 para os dados básicos de identificação em `usuario (id)`. |
| `descricao` | `text` | Sim | — | `ck_paciente_descricao` | `[LGPD: sensível]` Anotações clínicas, histórico multidisciplinar e observações sobre o quadro de saúde do paciente com TEA (Art. 11 e 14 da LGPD). **Anulável** porque o cadastro inicial pela recepção pode ocorrer antes do registro clínico pelo especialista. |
| `grau_suporte` | `text` | Não | — | `ck_paciente_grau_suporte` | `[LGPD: sensível]` Nível de suporte exigido pelo diagnóstico de TEA (`NIVEL_1`, `NIVEL_2` ou `NIVEL_3`), dado clínico de saúde usado no direcionamento terapêutico. |
| `criado_em` | `timestamptz` | Não | `now()` | — | Data e hora de criação do cadastro clínico. |
| `atualizado_em` | `timestamptz` | Não | `now()` | Trigger `tg_paciente_atualizado_em` | Data e hora da última atualização clínica. |
| `excluido_em` | `timestamptz` | Sim | `NULL` | — | Data e hora da inativação lógica do paciente. **Anulável** pois permanece `NULL` enquanto o paciente estiver ativo. |

- **Constraints:** `pk_paciente (id)`, `uq_paciente_usuario unique (usuario_id)` *(cobre também a indexação da FK `usuario_id`)*, `fk_paciente_usuario (usuario_id references usuario (id) on delete restrict)`, `ck_paciente_grau_suporte (grau_suporte in ('NIVEL_1', 'NIVEL_2', 'NIVEL_3'))`, `ck_paciente_descricao (descricao is null or char_length(trim(descricao)) <= 5000)`.
- **Índices:** `uq_paciente_usuario` (automático via constraint `UNIQUE`), `ix_paciente_grau_suporte` (`(grau_suporte) where excluido_em is null`).
- **Trigger:** `tg_paciente_atualizado_em` (`BEFORE UPDATE`).

---

## 4. Tabela: `amde_digital.categoria`

Catálogo de classificações dos documentos anexados ao Prontuário Eletrônico do Paciente (`Exames`, `Laudos`, `Receituario`, `Arquivado`). Tabela de domínio estável sem soft delete.

| Coluna | Tipo SQL | Nulo? | Padrão | Restrições / Chaves | Descrição |
|---|---|---|---|---|---|
| `id` | `bigint generated always as identity` | Não | Identity | `pk_categoria` | Identificador único da categoria. |
| `nome` | `text` | Não | — | `uq_categoria_nome`, `ck_categoria_nome` | Nome único da categoria clínica (`Exames`, `Laudos`, `Receituario`, `Arquivado`). |
| `criado_em` | `timestamptz` | Não | `now()` | — | Data e hora de criação do registro. |
| `atualizado_em` | `timestamptz` | Não | `now()` | Trigger `tg_categoria_atualizado_em` | Data e hora da última atualização. |

- **Constraints:** `pk_categoria (id)`, `uq_categoria_nome unique (nome)`, `ck_categoria_nome (char_length(trim(nome)) between 2 and 50)`.
- **Índices:** `uq_categoria_nome` (automático via constraint `UNIQUE`).
- **Trigger:** `tg_categoria_atualizado_em` (`BEFORE UPDATE`).

---

## 5. Tabela: `amde_digital.registro_prontuario`

Metadados e referências de armazenamento dos documentos clínicos em PDF do Prontuário Eletrônico do Paciente (Resolução CFM nº 1.821/2007). Adota arquivamento/inativação lógica via `excluido_em` (`NULL` = documento ativo; `NOT NULL` = data/hora do arquivamento/exclusão lógica).

| Coluna | Tipo SQL | Nulo? | Padrão | Restrições / Chaves | Descrição / Classificação LGPD e Justificativa |
|---|---|---|---|---|---|
| `id` | `bigint generated always as identity` | Não | Identity | `pk_registro_prontuario` | Identificador único do documento no prontuário. |
| `paciente_id` | `bigint` | Não | — | `fk_registro_prontuario_paciente` | Referência ao paciente titular em `paciente (id)`. |
| `categoria_id` | `bigint` | Não | — | `fk_registro_prontuario_categoria` | Referência à categoria documental em `categoria (id)`. |
| `registro_substituido_id` | `bigint` | Sim | — | `fk_registro_prontuario_substituido`, `ck_registro_prontuario_auto_substituicao` | Referência ao registro anterior substituído por esta nova versão (RN08). **Anulável** porque o primeiro envio de um documento não substitui nenhum registro prévio. |
| `titulo_documento` | `text` | Não | — | `ck_registro_prontuario_titulo` | `[LGPD: sensível]` Título descritivo do documento médico/terapêutico, revelando natureza do atendimento de saúde do paciente. |
| `nome_arquivo_original` | `text` | Não | — | `ck_registro_prontuario_arquivo_original` | `[LGPD: sensível]` Nome original do arquivo PDF submetido no upload (RN07), podendo conter referências clínicas do atendimento. |
| `nome_arquivo_armazenado` | `text` | Não | — | `uq_registro_prontuario_arquivo_armazenado`, `ck_registro_prontuario_arquivo_armazenado` | `[LGPD: sensível]` Identificador UUID único gerado para armazenamento isolado em `uploads/prontuarios/paciente-{id}/{uuid}.pdf` (RN07). |
| `criado_em` | `timestamptz` | Não | `now()` | — | Data e hora exata do upload do documento. |
| `atualizado_em` | `timestamptz` | Não | `now()` | Trigger `tg_registro_prontuario_atualizado_em` | Data e hora da última atualização dos metadados. |
| `excluido_em` | `timestamptz` | Sim | `NULL` | — | Data e hora do arquivamento ou exclusão lógica do documento. **Anulável** pois permanece `NULL` enquanto o documento estiver ativo no prontuário. |

- **Constraints:** `pk_registro_prontuario (id)`, `uq_registro_prontuario_arquivo_armazenado unique (nome_arquivo_armazenado)`, `fk_registro_prontuario_paciente (paciente_id references paciente (id) on delete restrict)`, `fk_registro_prontuario_categoria (categoria_id references categoria (id) on delete restrict)`, `fk_registro_prontuario_substituido (registro_substituido_id references registro_prontuario (id) on delete set null)`, `ck_registro_prontuario_titulo (char_length(trim(titulo_documento)) between 2 and 150)`, `ck_registro_prontuario_arquivo_original (char_length(trim(nome_arquivo_original)) between 5 and 255 and lower(nome_arquivo_original) like '%.pdf')`, `ck_registro_prontuario_arquivo_armazenado (char_length(trim(nome_arquivo_armazenado)) between 5 and 255)`, `ck_registro_prontuario_auto_substituicao (registro_substituido_id is null or registro_substituido_id <> id)`.
- **Índices:** `ix_registro_prontuario_paciente (paciente_id)`, `ix_registro_prontuario_categoria (categoria_id)`, `ix_registro_prontuario_substituido (registro_substituido_id)`.
- **Trigger:** `tg_registro_prontuario_atualizado_em` (`BEFORE UPDATE`).

---

## 6. Tabela: `amde_digital.agendamento`

Sessões clínicas, consultas, avaliações e terapias agendadas para os profissionais da AMDE. Não utiliza coluna `excluido_em` pois o cancelamento integra o ciclo de vida de negócio por meio da coluna `status = 'CANCELADO'`, preservando o histórico da agenda.

| Coluna | Tipo SQL | Nulo? | Padrão | Restrições / Chaves | Descrição |
|---|---|---|---|---|---|
| `id` | `bigint generated always as identity` | Não | Identity | `pk_agendamento` | Identificador único da sessão clínica. |
| `funcionario_id` | `bigint` | Não | — | `fk_agendamento_funcionario` | Profissional de saúde responsável em `funcionario (id)`. |
| `data_inicio` | `timestamptz` | Não | — | `ck_agendamento_periodo` | Data e hora de início da sessão clínica. |
| `data_fim` | `timestamptz` | Não | — | `ck_agendamento_periodo` | Data e hora de encerramento da sessão (`data_fim > data_inicio`). |
| `tipo_agendamento` | `text` | Não | — | `ck_agendamento_tipo` | Modalidade clínica: `CONSULTA`, `EXAME`, `RETORNO`, `AVALIACAO` ou `TERAPIA`. |
| `status` | `text` | Não | `'AGENDADO'` | `ck_agendamento_status` | Estado do agendamento: `AGENDADO`, `CONCLUIDO` ou `CANCELADO`. |
| `possui_presenca_funcionario` | `boolean` | Não | `false` | — | Indica se o profissional responsável confirmou comparecimento à sessão. |
| `criado_em` | `timestamptz` | Não | `now()` | — | Data e hora de criação do agendamento. |
| `atualizado_em` | `timestamptz` | Não | `now()` | Trigger `tg_agendamento_atualizado_em` | Data e hora da última alteração do agendamento. |

- **Constraints:** `pk_agendamento (id)`, `fk_agendamento_funcionario (funcionario_id references funcionario (id) on delete restrict)`, `ck_agendamento_periodo (data_fim > data_inicio)`, `ck_agendamento_tipo (tipo_agendamento in ('CONSULTA', 'EXAME', 'RETORNO', 'AVALIACAO', 'TERAPIA'))`, `ck_agendamento_status (status in ('AGENDADO', 'CONCLUIDO', 'CANCELADO'))`.
- **Índices:** `ix_agendamento_funcionario (funcionario_id)`, `ix_agendamento_funcionario_periodo ((funcionario_id, data_inicio, data_fim) where status <> 'CANCELADO')`.
- **Trigger:** `tg_agendamento_atualizado_em` (`BEFORE UPDATE`).

---

## 7. Tabela: `amde_digital.agendamento_paciente`

Tabela associativa ($N:N$) que vincula um ou mais pacientes a um agendamento clínico (suportando atendimentos individuais e terapias em grupo) e registra a presença individual de cada paciente. Possui `atualizado_em` e trigger porque `possui_presenca` sofre atualização durante o atendimento.

| Coluna | Tipo SQL | Nulo? | Padrão | Restrições / Chaves | Descrição / Classificação LGPD |
|---|---|---|---|---|---|
| `agendamento_id` | `bigint` | Não | — | `pk_agendamento_paciente` (1ª col.), `fk_agendamento_paciente_agendamento` | Referência à sessão clínica em `agendamento (id)`. |
| `paciente_id` | `bigint` | Não | — | `pk_agendamento_paciente` (2ª col.), `fk_agendamento_paciente_paciente` | Referência ao paciente participante em `paciente (id)`. |
| `possui_presenca` | `boolean` | Não | `false` | — | `[LGPD: sensível]` Confirmação de comparecimento do paciente assistido à sessão clínica ou terapêutica na AMDE. |
| `criado_em` | `timestamptz` | Não | `now()` | — | Data e hora de criação do vínculo paciente-sessão. |
| `atualizado_em` | `timestamptz` | Não | `now()` | Trigger `tg_agendamento_paciente_atualizado_em` | Data e hora da última atualização de presença. |

- **Constraints:** `pk_agendamento_paciente primary key (agendamento_id, paciente_id)`, `fk_agendamento_paciente_agendamento (agendamento_id references agendamento (id) on delete cascade)`, `fk_agendamento_paciente_paciente (paciente_id references paciente (id) on delete restrict)`.
- **Índices:** `agendamento_id` já é a primeira coluna de `pk_agendamento_paciente` (dispensa índice próprio); `ix_agendamento_paciente_paciente (paciente_id)`.
- **Trigger:** `tg_agendamento_paciente_atualizado_em` (`BEFORE UPDATE`).

---

## 8. Tabela: `amde_digital.log_auditoria`

Trilha de auditoria de segurança estritamente imutável (*append-only*) para rastreabilidade de operações sensíveis em conformidade com a LGPD e a Resolução CFM nº 1.821/2007 (RN16). Não possui `atualizado_em`, `excluido_em` nem trigger de atualização, pois registros de auditoria jamais sofrem alteração ou remoção.

| Coluna | Tipo SQL | Nulo? | Padrão | Restrições / Chaves | Descrição / Classificação LGPD e Justificativa |
|---|---|---|---|---|---|
| `id` | `bigint generated always as identity` | Não | Identity | `pk_log_auditoria` | Identificador único do evento de auditoria. |
| `usuario_id` | `bigint` | Não | — | `fk_log_auditoria_usuario` | Usuário autenticado responsável pelo disparo da operação em `usuario (id)`. |
| `acao` | `text` | Não | — | `ck_log_auditoria_acao` | Tipo de evento sensível: `LOGIN`, `UPLOAD`, `DOWNLOAD`, `EXCLUSAO` ou `AGENDAMENTO`. |
| `recurso` | `text` | Não | — | `ck_log_auditoria_recurso` | Identificador lógico ou caminho do recurso clínico/administrativo afetado (sem expor conteúdo do prontuário). |
| `ip_origem` | `text` | Sim | — | `ck_log_auditoria_ip_origem` | `[LGPD: pessoal]` Endereço IPv4 ou IPv6 de origem da requisição HTTP para rastreabilidade forense. **Anulável** porque eventos disparados por rotinas internas ou testes sem requisição web podem não possuir IP associado. |
| `criado_em` | `timestamptz` | Não | `now()` | — | Data e hora exata de ocorrência do evento auditado (imutável). |

- **Constraints:** `pk_log_auditoria (id)`, `fk_log_auditoria_usuario (usuario_id references usuario (id) on delete restrict)`, `ck_log_auditoria_acao (acao in ('LOGIN', 'UPLOAD', 'DOWNLOAD', 'EXCLUSAO', 'AGENDAMENTO'))`, `ck_log_auditoria_recurso (char_length(trim(recurso)) between 1 and 255)`, `ck_log_auditoria_ip_origem (ip_origem is null or char_length(trim(ip_origem)) between 3 and 45)`.
- **Índices:** `ix_log_auditoria_usuario (usuario_id)`, `ix_log_auditoria_criado_em (criado_em desc)`.
- **Trigger:** Nenhuma (tabela imutável *append-only*).
