-- Massa de dados sintetica para desenvolvimento e testes da SPEC-001 (LGPD compliant)
set search_path = amde_digital, public;

insert into usuario (
    nome, email, senha, data_nascimento, nivel_acesso, num_celular, pais, estado, cidade, rua
)
values (
    'Administrador Sintetico',
    'admin.teste@amde.example.org',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    date '1990-05-10',
    'ADMINISTRADOR',
    '15999990001',
    'Brasil',
    'SP',
    'Sorocaba',
    'Rua Exemplo, 100'
)
on conflict do nothing;

with novo_medico as (
    insert into usuario (
        nome, email, senha, data_nascimento, nivel_acesso, num_celular, pais, estado, cidade, rua
    )
    values (
        'Profissional Sintetico',
        'medico.teste@amde.example.org',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        date '1988-08-20',
        'MEDICO',
        '15999990002',
        'Brasil',
        'SP',
        'Sorocaba',
        'Avenida Teste, 200'
    )
    on conflict do nothing
    returning id
)

insert into funcionario (usuario_id, crm, cargo)
select
    novo_medico.id,
    'CRM/SP 000001' as crm,
    'Psicologia' as cargo
from novo_medico;

with novo_paciente as (
    insert into usuario (
        nome, email, senha, data_nascimento, nivel_acesso, num_celular, pais, estado, cidade, rua
    )
    values (
        'Paciente Sintetico',
        'paciente.teste@amde.example.org',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        date '2016-03-15',
        'PACIENTE',
        null,
        'Brasil',
        'SP',
        'Sorocaba',
        'Rua Modelo, 300'
    )
    on conflict do nothing
    returning id
)

insert into paciente (usuario_id, grau_suporte, descricao)
select
    novo_paciente.id,
    'NIVEL_2' as grau_suporte,
    'Registro sintetico exclusivo para testes funcionais.' as descricao
from novo_paciente;
