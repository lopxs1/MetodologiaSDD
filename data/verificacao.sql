-- verificacao.sql
-- Consultas automáticas de conformidade para o schema 'amde_digital' (PostgreSQL 18).
-- As consultas 1 a 9 devem retornar ZERO linhas antes de cada entrega ou Pull Request.
-- A consulta final gera o inventário LGPD para conferência humana.

-- 1. Tabelas sem chave primária
select c.relname as tabela
from pg_class c
where
    c.relnamespace = 'amde_digital'::regnamespace
    and c.relkind in ('r', 'p')
    and not exists (
        select 1
        from pg_constraint k
        where
            k.conrelid = c.oid
            and k.contype = 'p'
    );

-- 2. FKs sem índice que as cubra
select
    c.conrelid::regclass as tabela,
    c.conname as fk
from pg_constraint c
where
    c.contype = 'f'
    and c.connamespace = 'amde_digital'::regnamespace
    and not exists (
        select 1
        from pg_index i
        where
            i.indrelid = c.conrelid
            and (i.indkey::int2[])[0:array_length(c.conkey, 1) - 1] @> c.conkey
    );

-- 3. Tipos não recomendados
select
    table_name,
    column_name,
    data_type
from information_schema.columns
where
    table_schema = 'amde_digital'
    and data_type in (
        'timestamp without time zone',
        'character',
        'real',
        'double precision',
        'money',
        'json'
    );

-- 4. Colunas com serial (default nextval) em vez de identity
select
    table_name,
    column_name
from information_schema.columns
where
    table_schema = 'amde_digital'
    and column_default like 'nextval(%';

-- 5. Nomes fora do padrão snake_case minúsculo
select
    table_name,
    column_name
from information_schema.columns
where
    table_schema = 'amde_digital'
    and (
        table_name !~ '^[a-z][a-z0-9_]*$'
        or column_name !~ '^[a-z][a-z0-9_]*$'
    );

-- 6. Tabelas sem comentário
select c.relname as tabela
from pg_class c
where
    c.relnamespace = 'amde_digital'::regnamespace
    and c.relkind in ('r', 'p')
    and obj_description(c.oid, 'pg_class') is null;

-- 7. Objetos criados no schema public
select
    relname,
    relkind
from pg_class
where
    relnamespace = 'public'::regnamespace
    and relkind in ('r', 'p', 'v', 'm', 'S');

-- 8. (Adicional) Colunas anuláveis sem comentário explicando o motivo
select
    c.relname as tabela,
    a.attname as coluna
from pg_attribute a
inner join pg_class c on c.oid = a.attrelid
where
    c.relnamespace = 'amde_digital'::regnamespace
    and c.relkind in ('r', 'p')
    and a.attnum > 0
    and not a.attisdropped
    and not a.attnotnull
    and col_description(c.oid, a.attnum) is null;

-- 9. (Adicional) Tabelas de negócio sem criado_em ou atualizado_em
-- (exclui tabelas imutáveis append-only como log_auditoria e tabelas internas de controle)
select c.relname as tabela
from pg_class c
where
    c.relnamespace = 'amde_digital'::regnamespace
    and c.relkind in ('r', 'p')
    and c.relname not in ('log_auditoria', 'databasechangelog', 'databasechangeloglock')
    and (
        not exists (
            select 1
            from pg_attribute a
            where
                a.attrelid = c.oid
                and a.attname = 'criado_em'
                and a.attnum > 0
                and not a.attisdropped
        )
        or not exists (
            select 1
            from pg_attribute a
            where
                a.attrelid = c.oid
                and a.attname = 'atualizado_em'
                and a.attnum > 0
                and not a.attisdropped
        )
    );

-- Consulta de apoio (não precisa retornar zero): inventário de dados pessoais e sensíveis para conferência de LGPD
select
    c.relname as tabela,
    a.attname as coluna,
    col_description(c.oid, a.attnum) as comentario
from pg_attribute a
inner join pg_class c on c.oid = a.attrelid
where
    c.relnamespace = 'amde_digital'::regnamespace
    and a.attnum > 0
    and not a.attisdropped
    and col_description(c.oid, a.attnum) like '[LGPD%'
order by 1, 2;
