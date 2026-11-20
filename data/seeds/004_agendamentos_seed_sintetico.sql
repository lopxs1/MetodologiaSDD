-- Seed sintetico de agendamentos clinicos e presencas (SPEC-004)
set search_path = amde_digital, public;

with prof as (
    select f.id as funcionario_id
    from funcionario f
    inner join usuario u on u.id = f.usuario_id
    where
        f.excluido_em is null
        and u.excluido_em is null
    order by f.id
    limit 1
),

pac_1 as (
    select p.id as paciente_id
    from paciente p
    inner join usuario u on u.id = p.usuario_id
    where
        p.excluido_em is null
        and u.excluido_em is null
    order by p.id
    limit 1
),

ag_individual as (
    insert into agendamento (
        funcionario_id,
        data_inicio,
        data_fim,
        tipo_agendamento,
        status,
        possui_presenca_funcionario
    )
    select
        prof.funcionario_id,
        '2026-12-15 10:00:00-03'::timestamptz as data_inicio,
        '2026-12-15 11:00:00-03'::timestamptz as data_fim,
        'CONSULTA' as tipo_agendamento,
        'AGENDADO' as status,
        false as possui_presenca_funcionario
    from prof
    where
        not exists (
            select 1
            from agendamento a
            where
                a.funcionario_id = prof.funcionario_id
                and a.data_inicio = '2026-12-15 10:00:00-03'::timestamptz
        )
    returning id
)

insert into agendamento_paciente (agendamento_id, paciente_id, possui_presenca)
select
    ag_individual.id,
    pac_1.paciente_id,
    false as possui_presenca
from ag_individual
cross join pac_1
on conflict (agendamento_id, paciente_id) do nothing;

with prof as (
    select f.id as funcionario_id
    from funcionario f
    inner join usuario u on u.id = f.usuario_id
    where
        f.excluido_em is null
        and u.excluido_em is null
    order by f.id
    limit 1
),

pacs_grupo as (
    select p.id as paciente_id
    from paciente p
    inner join usuario u on u.id = p.usuario_id
    where
        p.excluido_em is null
        and u.excluido_em is null
    order by p.id
    limit 3
),

ag_grupo as (
    insert into agendamento (
        funcionario_id,
        data_inicio,
        data_fim,
        tipo_agendamento,
        status,
        possui_presenca_funcionario
    )
    select
        prof.funcionario_id,
        '2026-12-16 14:00:00-03'::timestamptz as data_inicio,
        '2026-12-16 15:30:00-03'::timestamptz as data_fim,
        'TERAPIA' as tipo_agendamento,
        'AGENDADO' as status,
        false as possui_presenca_funcionario
    from prof
    where
        not exists (
            select 1
            from agendamento a
            where
                a.funcionario_id = prof.funcionario_id
                and a.data_inicio = '2026-12-16 14:00:00-03'::timestamptz
        )
    returning id
)

insert into agendamento_paciente (agendamento_id, paciente_id, possui_presenca)
select
    ag_grupo.id,
    pacs_grupo.paciente_id,
    false as possui_presenca
from ag_grupo
cross join pacs_grupo
on conflict (agendamento_id, paciente_id) do nothing;

with prof as (
    select f.id as funcionario_id
    from funcionario f
    inner join usuario u on u.id = f.usuario_id
    where
        f.excluido_em is null
        and u.excluido_em is null
    order by f.id
    limit 1
),

pac_1 as (
    select p.id as paciente_id
    from paciente p
    inner join usuario u on u.id = p.usuario_id
    where
        p.excluido_em is null
        and u.excluido_em is null
    order by p.id
    limit 1
),

ag_concluido as (
    insert into agendamento (
        funcionario_id,
        data_inicio,
        data_fim,
        tipo_agendamento,
        status,
        possui_presenca_funcionario
    )
    select
        prof.funcionario_id,
        '2026-10-10 09:00:00-03'::timestamptz as data_inicio,
        '2026-10-10 10:00:00-03'::timestamptz as data_fim,
        'AVALIACAO' as tipo_agendamento,
        'CONCLUIDO' as status,
        true as possui_presenca_funcionario
    from prof
    where
        not exists (
            select 1
            from agendamento a
            where
                a.funcionario_id = prof.funcionario_id
                and a.data_inicio = '2026-10-10 09:00:00-03'::timestamptz
        )
    returning id
)

insert into agendamento_paciente (agendamento_id, paciente_id, possui_presenca)
select
    ag_concluido.id,
    pac_1.paciente_id,
    true as possui_presenca
from ag_concluido
cross join pac_1
on conflict (agendamento_id, paciente_id) do nothing;

with prof as (
    select f.id as funcionario_id
    from funcionario f
    inner join usuario u on u.id = f.usuario_id
    where
        f.excluido_em is null
        and u.excluido_em is null
    order by f.id
    limit 1
),

pac_1 as (
    select p.id as paciente_id
    from paciente p
    inner join usuario u on u.id = p.usuario_id
    where
        p.excluido_em is null
        and u.excluido_em is null
    order by p.id
    limit 1
),

ag_cancelado as (
    insert into agendamento (
        funcionario_id,
        data_inicio,
        data_fim,
        tipo_agendamento,
        status,
        possui_presenca_funcionario
    )
    select
        prof.funcionario_id,
        '2026-12-18 16:00:00-03'::timestamptz as data_inicio,
        '2026-12-18 17:00:00-03'::timestamptz as data_fim,
        'RETORNO' as tipo_agendamento,
        'CANCELADO' as status,
        false as possui_presenca_funcionario
    from prof
    where
        not exists (
            select 1
            from agendamento a
            where
                a.funcionario_id = prof.funcionario_id
                and a.data_inicio = '2026-12-18 16:00:00-03'::timestamptz
        )
    returning id
)

insert into agendamento_paciente (agendamento_id, paciente_id, possui_presenca)
select
    ag_cancelado.id,
    pac_1.paciente_id,
    false as possui_presenca
from ag_cancelado
cross join pac_1
on conflict (agendamento_id, paciente_id) do nothing;
