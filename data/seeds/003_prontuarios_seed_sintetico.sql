-- Seed sintetico das 4 categorias e registros de prontuario (SPEC-003)
set search_path = amde_digital, public;

insert into categoria (nome)
values
('Exames'),
('Laudos'),
('Receituario'),
('Arquivado')
on conflict (nome) do nothing;

with pac_teste as (
    select p.id as paciente_id
    from paciente p
    where p.excluido_em is null
    order by p.id
    limit 1
),

doc_v1 as (
    insert into registro_prontuario (
        paciente_id,
        categoria_id,
        titulo_documento,
        nome_arquivo_original,
        nome_arquivo_armazenado
    )
    select
        pt.paciente_id,
        cat.id as categoria_id,
        'Exame Auditivo Sintetico v1' as titulo_documento,
        'exame-v1.pdf' as nome_arquivo_original,
        '11111111-1111-4111-8111-111111111111.pdf' as nome_arquivo_armazenado
    from pac_teste pt
    cross join categoria cat
    where cat.nome = 'Exames'
    on conflict (nome_arquivo_armazenado) do nothing
    returning id, paciente_id, categoria_id
)

insert into registro_prontuario (
    paciente_id,
    categoria_id,
    registro_substituido_id,
    titulo_documento,
    nome_arquivo_original,
    nome_arquivo_armazenado
)
select
    d.paciente_id,
    d.categoria_id,
    d.id as registro_substituido_id,
    'Exame Auditivo Sintetico v2' as titulo_documento,
    'exame-v2.pdf' as nome_arquivo_original,
    '22222222-2222-4222-8222-222222222222.pdf' as nome_arquivo_armazenado
from doc_v1 d
on conflict (nome_arquivo_armazenado) do nothing;

with pac_teste as (
    select p.id as paciente_id
    from paciente p
    where p.excluido_em is null
    order by p.id
    limit 1
)

insert into registro_prontuario (
    paciente_id,
    categoria_id,
    titulo_documento,
    nome_arquivo_original,
    nome_arquivo_armazenado
)
select
    pt.paciente_id,
    cat.id as categoria_id,
    'Documento Antigo Arquivado Sintetico' as titulo_documento,
    'documento-arquivado.pdf' as nome_arquivo_original,
    '33333333-3333-4333-8333-333333333333.pdf' as nome_arquivo_armazenado
from pac_teste pt
cross join categoria cat
where cat.nome = 'Arquivado'
on conflict (nome_arquivo_armazenado) do nothing;
