-- Seed sintetico contemplando as 5 acoes auditaveis e um evento interno (ip_origem NULL)
set search_path = amde_digital, public;

with usr as (
    select id
    from usuario
    where excluido_em is null
    order by id
    limit 1
)

insert into log_auditoria (usuario_id, acao, recurso, ip_origem, criado_em)
select
    usr.id,
    v.acao,
    v.recurso,
    v.ip_origem,
    v.criado_em
from usr
cross join (
    values
    ('LOGIN', 'usuario:1:LOGIN', '192.0.2.10', now() - interval '3 hours'),
    ('UPLOAD', 'documento:101:UPLOAD', '192.0.2.10', now() - interval '2 hours'),
    ('DOWNLOAD', 'documento:101:DOWNLOAD', '198.51.100.25', now() - interval '1 hour'),
    ('AGENDAMENTO', 'agendamento:42:CRIAR', '192.0.2.10', now() - interval '30 minutes'),
    ('EXCLUSAO', 'documento:102:EXCLUSAO', null, now() - interval '5 minutes')
) as v (acao, recurso, ip_origem, criado_em);
