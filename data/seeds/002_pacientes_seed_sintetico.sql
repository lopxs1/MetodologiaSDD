-- Seed sintetico das 4 categorias e pacientes nos 3 graus de suporte (SPEC-002)
set search_path = amde_digital, public;

insert into categoria (nome)
values
('Exames'),
('Laudos'),
('Receituario'),
('Arquivado')
on conflict (nome) do nothing;

with pac_n1 as (
    insert into usuario (nome, email, senha, nivel_acesso, num_celular, cidade, estado)
    values (
        'José Lucas Teste',
        'jose.lucas@amde.example.org',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'PACIENTE',
        '15999990011',
        'Sorocaba',
        'SP'
    )
    on conflict do nothing
    returning id
),

pac_n1_reg as (
    insert into paciente (usuario_id, grau_suporte, descricao)
    select
        pac_n1.id,
        'NIVEL_1' as grau_suporte,
        'Paciente sintetico Nivel 1 para testes de busca com acentuacao.' as descricao
    from pac_n1
    on conflict (usuario_id) do nothing
    returning id
),

laudo_antigo as (
    insert into registro_prontuario (
        paciente_id,
        categoria_id,
        titulo_documento,
        nome_arquivo_original,
        nome_arquivo_armazenado
    )
    select
        pac_n1_reg.id as paciente_id,
        cat.id as categoria_id,
        'Laudo Inicial Sintetico' as titulo_documento,
        'laudo-inicial.pdf' as nome_arquivo_original,
        '00000000-0000-0000-0000-000000000001.pdf' as nome_arquivo_armazenado
    from pac_n1_reg
    cross join categoria cat
    where cat.nome = 'Laudos'
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
    laudo_antigo.paciente_id,
    laudo_antigo.categoria_id,
    laudo_antigo.id as registro_substituido_id,
    'Laudo Atualizado Sintetico' as titulo_documento,
    'laudo-revisado.pdf' as nome_arquivo_original,
    '00000000-0000-0000-0000-000000000002.pdf' as nome_arquivo_armazenado
from laudo_antigo
on conflict (nome_arquivo_armazenado) do nothing;

with pac_n2 as (
    insert into usuario (nome, email, senha, nivel_acesso, num_celular, cidade, estado)
    values (
        'Marina Silva Teste',
        'marina.silva@amde.example.org',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'PACIENTE',
        '15999990022',
        'Sorocaba',
        'SP'
    )
    on conflict do nothing
    returning id
)

insert into paciente (usuario_id, grau_suporte, descricao)
select
    pac_n2.id,
    'NIVEL_2' as grau_suporte,
    'Paciente sintetico Nivel 2 em acompanhamento.' as descricao
from pac_n2
on conflict (usuario_id) do nothing;

with pac_n3 as (
    insert into usuario (nome, email, senha, nivel_acesso, num_celular, cidade, estado)
    values (
        'Gabriel Souza Teste',
        'gabriel.souza@amde.example.org',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'PACIENTE',
        null,
        'Sorocaba',
        'SP'
    )
    on conflict do nothing
    returning id
)

insert into paciente (usuario_id, grau_suporte, descricao)
select
    pac_n3.id,
    'NIVEL_3' as grau_suporte,
    null as descricao
from pac_n3
on conflict (usuario_id) do nothing;
