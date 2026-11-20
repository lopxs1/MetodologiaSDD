-- Seed sintetico para validacao do Dashboard Gerencial (SPEC-006)
set search_path = amde_digital, public;

with usr_psi_ativo as (
    insert into usuario (nome, email, senha, nivel_acesso)
    select
        'Psicologa Ativa Sintetica' as nome,
        'psi.ativa.dash@amde.example.org' as email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' as senha,
        'MEDICO' as nivel_acesso
    where
        not exists (
            select 1
            from usuario u
            where lower(u.email) = 'psi.ativa.dash@amde.example.org'
        )
    returning id
),

func_psi_ativo as (
    insert into funcionario (usuario_id, cargo)
    select
        usr_psi_ativo.id,
        'Psicologia' as cargo
    from usr_psi_ativo
    returning id
),

usr_psi_inativo as (
    insert into usuario (nome, email, senha, nivel_acesso, excluido_em)
    select
        'Psicologo Inativo Sintetico' as nome,
        'psi.inativo.dash@amde.example.org' as email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' as senha,
        'MEDICO' as nivel_acesso,
        now() - interval '1 day' as excluido_em
    where
        not exists (
            select 1
            from usuario u
            where lower(u.email) = 'psi.inativo.dash@amde.example.org'
        )
    returning id
),

func_psi_inativo as (
    insert into funcionario (usuario_id, cargo, excluido_em)
    select
        usr_psi_inativo.id,
        ' psicologia ' as cargo,
        now() - interval '1 day' as excluido_em
    from usr_psi_inativo
    returning id
),

usr_fono as (
    insert into usuario (nome, email, senha, nivel_acesso)
    select
        'Fonoaudiologa Sintetica' as nome,
        'fono.dash@amde.example.org' as email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' as senha,
        'MEDICO' as nivel_acesso
    where
        not exists (
            select 1
            from usuario u
            where lower(u.email) = 'fono.dash@amde.example.org'
        )
    returning id
),

func_fono as (
    insert into funcionario (usuario_id, cargo)
    select
        usr_fono.id,
        'Fonoaudiologia' as cargo
    from usr_fono
    returning id
)

insert into agendamento (
    funcionario_id,
    data_inicio,
    data_fim,
    tipo_agendamento,
    status
)
select
    func_psi_ativo.id,
    (date_trunc('day', now() at time zone 'America/Sao_Paulo') + interval '23 hours 30 minutes')
    at time zone 'America/Sao_Paulo' as data_inicio,
    (date_trunc('day', now() at time zone 'America/Sao_Paulo') + interval '23 hours 55 minutes')
    at time zone 'America/Sao_Paulo' as data_fim,
    'CONSULTA' as tipo_agendamento,
    'AGENDADO' as status
from func_psi_ativo
union all
select
    func_psi_inativo.id,
    now() - interval '5 days' as data_inicio,
    now() - interval '5 days' + interval '50 minutes' as data_fim,
    'AVALIACAO' as tipo_agendamento,
    'CONCLUIDO' as status
from func_psi_inativo
union all
select
    func_psi_inativo.id,
    now() - interval '4 days' as data_inicio,
    now() - interval '4 days' + interval '50 minutes' as data_fim,
    'TERAPIA' as tipo_agendamento,
    'AGENDADO' as status
from func_psi_inativo
union all
select
    func_fono.id,
    now() - interval '3 days' as data_inicio,
    now() - interval '3 days' + interval '45 minutes' as data_fim,
    'TERAPIA' as tipo_agendamento,
    'CONCLUIDO' as status
from func_fono
union all
select
    func_fono.id,
    (date_trunc('day', now() at time zone 'America/Sao_Paulo') + interval '10 hours')
    at time zone 'America/Sao_Paulo' as data_inicio,
    (date_trunc('day', now() at time zone 'America/Sao_Paulo') + interval '11 hours')
    at time zone 'America/Sao_Paulo' as data_fim,
    'RETORNO' as tipo_agendamento,
    'CANCELADO' as status
from func_fono;
