--liquibase formatted sql
--changeset amde_digital:001-esquema-inicial splitStatements:false
--comment: Esquema inicial do AMDE Digital no PostgreSQL 18 (schema amde_digital)

set search_path = amde_digital, public;

-- 1. Domínios -------------------------------------------------------

create domain telefone as text
check (value ~ '^[0-9]{10,11}$');

create domain cpf as text
check (value ~ '^[0-9]{11}$');

create domain cep as text
check (value ~ '^[0-9]{8}$');

-- 2. Tabelas --------------------------------------------------------

create table usuario (
    id bigint generated always as identity,
    nome text not null,
    email text not null,
    senha text not null,
    data_nascimento date,
    nivel_acesso text not null,
    num_celular telefone,
    pais text default 'Brasil',
    estado text,
    cidade text,
    rua text,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    excluido_em timestamptz,
    constraint pk_usuario primary key (id),
    constraint ck_usuario_nome
    check (char_length(trim(nome)) between 2 and 100),
    constraint ck_usuario_email
    check (char_length(trim(email)) between 5 and 254 and position('@' in email) > 1),
    constraint ck_usuario_senha
    check (char_length(trim(senha)) between 60 and 255),
    constraint ck_usuario_nivel_acesso
    check (nivel_acesso in ('ADMINISTRADOR', 'MEDICO', 'PACIENTE')),
    constraint ck_usuario_data_nascimento
    check (
        data_nascimento is null
        or (data_nascimento >= date '1900-01-01' and data_nascimento <= current_date)
    ),
    constraint ck_usuario_pais
    check (pais is null or char_length(trim(pais)) between 2 and 60),
    constraint ck_usuario_estado
    check (estado is null or estado ~ '^[A-Z]{2}$'),
    constraint ck_usuario_cidade
    check (cidade is null or char_length(trim(cidade)) between 2 and 100),
    constraint ck_usuario_rua
    check (rua is null or char_length(trim(rua)) between 2 and 150)
);

create table funcionario (
    id bigint generated always as identity,
    usuario_id bigint not null,
    crm text,
    cargo text not null,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    excluido_em timestamptz,
    constraint pk_funcionario primary key (id),
    constraint uq_funcionario_usuario unique (usuario_id),
    constraint ck_funcionario_crm
    check (crm is null or char_length(trim(crm)) between 3 and 30),
    constraint ck_funcionario_cargo
    check (char_length(trim(cargo)) between 2 and 80)
);

create table paciente (
    id bigint generated always as identity,
    usuario_id bigint not null,
    descricao text,
    grau_suporte text not null,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    excluido_em timestamptz,
    constraint pk_paciente primary key (id),
    constraint uq_paciente_usuario unique (usuario_id),
    constraint ck_paciente_grau_suporte
    check (grau_suporte in ('NIVEL_1', 'NIVEL_2', 'NIVEL_3')),
    constraint ck_paciente_descricao
    check (descricao is null or char_length(trim(descricao)) <= 5000)
);

create table categoria (
    id bigint generated always as identity,
    nome text not null,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    constraint pk_categoria primary key (id),
    constraint uq_categoria_nome unique (nome),
    constraint ck_categoria_nome
    check (char_length(trim(nome)) between 2 and 50)
);

create table registro_prontuario (
    id bigint generated always as identity,
    paciente_id bigint not null,
    categoria_id bigint not null,
    registro_substituido_id bigint,
    titulo_documento text not null,
    nome_arquivo_original text not null,
    nome_arquivo_armazenado text not null,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    excluido_em timestamptz,
    constraint pk_registro_prontuario primary key (id),
    constraint uq_registro_prontuario_arquivo_armazenado unique (nome_arquivo_armazenado),
    constraint ck_registro_prontuario_titulo
    check (char_length(trim(titulo_documento)) between 2 and 150),
    constraint ck_registro_prontuario_arquivo_original
    check (
        char_length(trim(nome_arquivo_original)) between 5 and 255
        and lower(nome_arquivo_original) like '%.pdf'
    ),
    constraint ck_registro_prontuario_arquivo_armazenado
    check (char_length(trim(nome_arquivo_armazenado)) between 5 and 255),
    constraint ck_registro_prontuario_auto_substituicao
    check (registro_substituido_id is null or registro_substituido_id <> id)
);

create table agendamento (
    id bigint generated always as identity,
    funcionario_id bigint not null,
    data_inicio timestamptz not null,
    data_fim timestamptz not null,
    tipo_agendamento text not null,
    status text not null default 'AGENDADO',
    possui_presenca_funcionario boolean not null default false,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    constraint pk_agendamento primary key (id),
    constraint ck_agendamento_periodo check (data_fim > data_inicio),
    constraint ck_agendamento_tipo
    check (tipo_agendamento in ('CONSULTA', 'EXAME', 'RETORNO', 'AVALIACAO', 'TERAPIA')),
    constraint ck_agendamento_status
    check (status in ('AGENDADO', 'CONCLUIDO', 'CANCELADO'))
);

create table agendamento_paciente (
    agendamento_id bigint not null,
    paciente_id bigint not null,
    possui_presenca boolean not null default false,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    constraint pk_agendamento_paciente primary key (agendamento_id, paciente_id)
);

create table log_auditoria (
    id bigint generated always as identity,
    usuario_id bigint not null,
    acao text not null,
    recurso text not null,
    ip_origem text,
    criado_em timestamptz not null default now(),
    constraint pk_log_auditoria primary key (id),
    constraint ck_log_auditoria_acao
    check (acao in ('LOGIN', 'UPLOAD', 'DOWNLOAD', 'EXCLUSAO', 'AGENDAMENTO')),
    constraint ck_log_auditoria_recurso
    check (char_length(trim(recurso)) between 1 and 255),
    constraint ck_log_auditoria_ip_origem
    check (ip_origem is null or char_length(trim(ip_origem)) between 3 and 45)
);

-- 3. FKs e seus índices --------------------------------------------

-- coberta pela constraint uq_funcionario_usuario: sem índice redundante
alter table funcionario
add constraint fk_funcionario_usuario
foreign key (usuario_id) references usuario (id) on delete restrict;

-- coberta pela constraint uq_paciente_usuario: sem índice redundante
alter table paciente
add constraint fk_paciente_usuario
foreign key (usuario_id) references usuario (id) on delete restrict;

alter table registro_prontuario
add constraint fk_registro_prontuario_paciente
foreign key (paciente_id) references paciente (id) on delete restrict;
create index ix_registro_prontuario_paciente
on registro_prontuario (paciente_id);

alter table registro_prontuario
add constraint fk_registro_prontuario_categoria
foreign key (categoria_id) references categoria (id) on delete restrict;
create index ix_registro_prontuario_categoria
on registro_prontuario (categoria_id);

alter table registro_prontuario
add constraint fk_registro_prontuario_substituido
foreign key (registro_substituido_id) references registro_prontuario (id) on delete set null;
create index ix_registro_prontuario_substituido
on registro_prontuario (registro_substituido_id);

alter table agendamento
add constraint fk_agendamento_funcionario
foreign key (funcionario_id) references funcionario (id) on delete restrict;
create index ix_agendamento_funcionario
on agendamento (funcionario_id);

-- coberta pela PK (agendamento_id é a primeira coluna): sem índice próprio
alter table agendamento_paciente
add constraint fk_agendamento_paciente_agendamento
foreign key (agendamento_id) references agendamento (id) on delete cascade;

alter table agendamento_paciente
add constraint fk_agendamento_paciente_paciente
foreign key (paciente_id) references paciente (id) on delete restrict;
create index ix_agendamento_paciente_paciente
on agendamento_paciente (paciente_id);

alter table log_auditoria
add constraint fk_log_auditoria_usuario
foreign key (usuario_id) references usuario (id) on delete restrict;
create index ix_log_auditoria_usuario
on log_auditoria (usuario_id);

-- 4. Demais índices -------------------------------------------------

create unique index uq_usuario_email_ativo
on usuario (lower(email))
where excluido_em is null;

create index ix_usuario_nome_trgm
on usuario using gin (nome public.gin_trgm_ops)
where excluido_em is null;

create index ix_usuario_nivel_acesso
on usuario (nivel_acesso)
where excluido_em is null;

create index ix_funcionario_cargo
on funcionario (cargo)
where excluido_em is null;

create index ix_paciente_grau_suporte
on paciente (grau_suporte)
where excluido_em is null;

create index ix_agendamento_funcionario_periodo
on agendamento (funcionario_id, data_inicio, data_fim)
where status <> 'CANCELADO';

create index ix_log_auditoria_criado_em
on log_auditoria (criado_em desc);

-- 5. Auditoria ------------------------------------------------------

create function definir_atualizado_em() returns trigger
language plpgsql as $$
begin
    new.atualizado_em := now();
    return new;
end;
$$;

create trigger tg_usuario_atualizado_em
before update on usuario
for each row execute function definir_atualizado_em();

create trigger tg_funcionario_atualizado_em
before update on funcionario
for each row execute function definir_atualizado_em();

create trigger tg_paciente_atualizado_em
before update on paciente
for each row execute function definir_atualizado_em();

create trigger tg_categoria_atualizado_em
before update on categoria
for each row execute function definir_atualizado_em();

create trigger tg_registro_prontuario_atualizado_em
before update on registro_prontuario
for each row execute function definir_atualizado_em();

create trigger tg_agendamento_atualizado_em
before update on agendamento
for each row execute function definir_atualizado_em();

create trigger tg_agendamento_paciente_atualizado_em
before update on agendamento_paciente
for each row execute function definir_atualizado_em();

-- 6. Comentários ----------------------------------------------------

comment on table usuario is
'Contas de acesso e dados cadastrais básicos de administradores, profissionais e pacientes da AMDE. '
'Adota soft delete via coluna excluido_em (NULL = registro ativo; NOT NULL = data/hora da inativação lógica).';
comment on column usuario.nome is
'[LGPD: pessoal] Nome completo do titular da conta para identificação civil e clínica nos atendimentos da AMDE.';
comment on column usuario.email is
'[LGPD: pessoal] Endereço de e-mail usado como credencial de login e contato institucional. '
'Único entre usuários ativos via uq_usuario_email_ativo.';
comment on column usuario.senha is
'Hash criptográfico da senha gerado exclusivamente via algoritmo BCrypt (RN01).';
comment on column usuario.data_nascimento is
'[LGPD: pessoal] Data de nascimento para cálculo de faixa etária pediátrica e validação cadastral. '
'Anulável porque contas administrativas iniciais podem ser criadas antes da complementação cadastral.';
comment on column usuario.nivel_acesso is
'Perfil de autorização RBAC na plataforma: ADMINISTRADOR, MEDICO ou PACIENTE.';
comment on column usuario.num_celular is
'[LGPD: pessoal] Telefone celular/WhatsApp com DDD (apenas dígitos) para contato sobre agendamentos. '
'Anulável porque nem todo usuário possui telefone próprio no pré-cadastro.';
comment on column usuario.pais is
'[LGPD: pessoal] País de residência do usuário. Anulável por ser dado complementar de endereço.';
comment on column usuario.estado is
'[LGPD: pessoal] Sigla da Unidade Federativa (UF) de residência. '
'Anulável por ser dado complementar de endereço.';
comment on column usuario.cidade is
'[LGPD: pessoal] Município de residência do usuário. Anulável por ser dado complementar de endereço.';
comment on column usuario.rua is
'[LGPD: pessoal] Logradouro e número residencial do usuário. Anulável por ser dado complementar de endereço.';
comment on column usuario.excluido_em is
'Data e hora da inativação lógica do usuário. Anulável pois permanece NULL enquanto a conta estiver ativa.';

comment on table funcionario is
'Dados profissionais dos médicos, terapeutas e colaboradores da equipe multidisciplinar (especialização 1:1 '
'de usuario). Adota soft delete via excluido_em (NULL = ativo; NOT NULL = inativado logicamente, preservando '
'histórico de agendamentos conduzidos).';
comment on column funcionario.usuario_id is
'Referência 1:1 para a conta de usuário correspondente na tabela usuario.';
comment on column funcionario.crm is
'[LGPD: pessoal] Registro em conselho de classe profissional (CRM, CRP, CREFITO, CRFa). '
'Anulável porque colaboradores administrativos ou funções pedagógicas de apoio podem não possuir registro.';
comment on column funcionario.cargo is
'[LGPD: pessoal] Especialidade clínica ou função exercida pelo profissional '
'no Centro de Excelência em Autismo.';
comment on column funcionario.excluido_em is
'Data e hora da desativação lógica do vínculo profissional. '
'Anulável pois permanece NULL enquanto o funcionário estiver ativo.';

comment on table paciente is
'Prontuário cadastral e classificação clínica das crianças e adolescentes com TEA assistidos pela AMDE '
'(especialização 1:1 de usuario). Adota soft delete via excluido_em (NULL = ativo; NOT NULL = inativado '
'logicamente, preservando prontuários pelo prazo legal do CFM).';
comment on column paciente.usuario_id is
'Referência 1:1 para os dados básicos de identificação na tabela usuario.';
comment on column paciente.descricao is
'[LGPD: sensível] Anotações clínicas, histórico multidisciplinar e observações sobre o quadro de saúde '
'do paciente com TEA. Anulável porque o cadastro inicial pela recepção pode preceder a avaliação clínica.';
comment on column paciente.grau_suporte is
'[LGPD: sensível] Nível de suporte exigido pelo diagnóstico de TEA (NIVEL_1, NIVEL_2 ou NIVEL_3), '
'dado clínico sensível usado no direcionamento terapêutico.';
comment on column paciente.excluido_em is
'Data e hora da inativação lógica do paciente. Anulável pois permanece NULL enquanto o paciente estiver ativo.';

comment on table categoria is
'Catálogo de classificações dos documentos anexados ao Prontuário Eletrônico do Paciente '
'(Exames, Laudos, Receituario, Arquivado). Tabela de domínio estável sem soft delete.';
comment on column categoria.nome is
'Nome único da categoria de documento clínico.';

comment on table registro_prontuario is
'Metadados e referências dos documentos clínicos em PDF do Prontuário Eletrônico do Paciente '
'(Resolução CFM nº 1.821/2007). Adota arquivamento/inativação lógica via excluido_em '
'(NULL = documento ativo; NOT NULL = arquivado/excluído logicamente).';
comment on column registro_prontuario.paciente_id is
'Paciente titular ao qual o documento clínico pertence.';
comment on column registro_prontuario.categoria_id is
'Classificação funcional do documento anexado.';
comment on column registro_prontuario.registro_substituido_id is
'Referência ao registro anterior substituído por esta versão (RN08). '
'Anulável porque o primeiro envio de um documento não substitui nenhum registro prévio.';
comment on column registro_prontuario.titulo_documento is
'[LGPD: sensível] Título descritivo do documento médico/terapêutico, '
'que revela natureza do atendimento clínico.';
comment on column registro_prontuario.nome_arquivo_original is
'[LGPD: sensível] Nome original do arquivo PDF submetido no upload (RN07), '
'podendo conter referências clínicas do paciente.';
comment on column registro_prontuario.nome_arquivo_armazenado is
'[LGPD: sensível] Nome físico único gerado via UUID para armazenamento isolado em '
'uploads/prontuarios/paciente-{id}/{uuid}.pdf (RN07).';
comment on column registro_prontuario.excluido_em is
'Data e hora do arquivamento ou exclusão lógica do documento. '
'Anulável pois permanece NULL enquanto o documento estiver ativo no prontuário.';

comment on table agendamento is
'Sessões clínicas, consultas, avaliações e terapias agendadas para os profissionais da AMDE. '
'Não utiliza excluido_em pois o cancelamento integra o ciclo de vida via status = CANCELADO.';
comment on column agendamento.funcionario_id is
'Profissional de saúde responsável por conduzir a sessão agendada.';
comment on column agendamento.tipo_agendamento is
'Modalidade clínica da sessão: CONSULTA, EXAME, RETORNO, AVALIACAO ou TERAPIA.';
comment on column agendamento.status is
'Estado atual do compromisso na agenda: AGENDADO, CONCLUIDO ou CANCELADO (RN14).';
comment on column agendamento.possui_presenca_funcionario is
'Indica se o profissional responsável confirmou comparecimento na sessão clínica.';

comment on table agendamento_paciente is
'Tabela associativa (N:N) que vincula um ou mais pacientes a um agendamento clínico '
'(atendimentos individuais e terapias em grupo) e registra a presença individual de cada paciente.';
comment on column agendamento_paciente.possui_presenca is
'[LGPD: sensível] Confirmação de comparecimento do paciente assistido à sessão clínica ou terapêutica.';

comment on table log_auditoria is
'Trilha de auditoria imutável (append-only) para rastreabilidade de operações sensíveis '
'(LGPD e Resolução CFM nº 1.821/2007). Não possui atualizado_em, excluido_em nem trigger de atualização.';
comment on column log_auditoria.usuario_id is
'Usuário autenticado responsável pelo disparo da operação auditada.';
comment on column log_auditoria.acao is
'Tipo de evento sensível registrado: LOGIN, UPLOAD, DOWNLOAD, EXCLUSAO ou AGENDAMENTO.';
comment on column log_auditoria.recurso is
'Identificador lógico ou caminho do recurso acessado (sem expor conteúdo clínico do prontuário).';
comment on column log_auditoria.ip_origem is
'[LGPD: pessoal] Endereço IPv4 ou IPv6 de origem da requisição HTTP para rastreabilidade forense. '
'Anulável porque rotinas internas ou testes sem requisição web podem não possuir IP associado.';

--rollback drop table if exists amde_digital.log_auditoria cascade;
--rollback drop table if exists amde_digital.agendamento_paciente cascade;
--rollback drop table if exists amde_digital.agendamento cascade;
--rollback drop table if exists amde_digital.registro_prontuario cascade;
--rollback drop table if exists amde_digital.categoria cascade;
--rollback drop table if exists amde_digital.paciente cascade;
--rollback drop table if exists amde_digital.funcionario cascade;
--rollback drop table if exists amde_digital.usuario cascade;
--rollback drop function if exists amde_digital.definir_atualizado_em() cascade;
--rollback drop domain if exists amde_digital.cep, amde_digital.cpf, amde_digital.telefone cascade;
