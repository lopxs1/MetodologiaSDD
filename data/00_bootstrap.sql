-- noqa: disable=PRS
-- 00_bootstrap.sql (executado uma única vez por superusuário conectado ao banco 'postgres')
-- Preparação do ambiente PostgreSQL 18 para o projeto AMDE Digital.
--
-- SEGURANÇA:
--   Nunca versione senhas reais de produção neste arquivo.
--   Em ambiente local ou CI/CD, exporte DB_OWNER_PASSWORD e DB_APP_PASSWORD antes
--   de executar via psql (o Docker Compose injeta essas variáveis automaticamente).
--   Caso execute manualmente sem variáveis de ambiente, substitua os placeholders abaixo.

\set owner_password 'CHANGE_ME_OWNER_PASSWORD'
\getenv owner_password DB_OWNER_PASSWORD

\set app_password 'CHANGE_ME_APP_PASSWORD'
\getenv app_password DB_APP_PASSWORD

create role amde_digital_owner login password :'owner_password';
create role amde_digital_app login password :'app_password';

create database amde_digital
    owner amde_digital_owner
    template template0
    encoding 'UTF8'
    locale_provider 'icu'
    icu_locale 'pt-BR'
    locale 'pt_BR.UTF-8';

revoke connect on database amde_digital from public;
grant connect on database amde_digital to amde_digital_owner, amde_digital_app;

alter database amde_digital set timezone = 'America/Sao_Paulo';

\connect amde_digital

create extension if not exists unaccent schema public;
create extension if not exists pg_trgm schema public;

create schema amde_digital authorization amde_digital_owner;
grant usage on schema amde_digital to amde_digital_app;

alter default privileges for role amde_digital_owner in schema amde_digital
    grant select, insert, update, delete on tables to amde_digital_app;
alter default privileges for role amde_digital_owner in schema amde_digital
    grant usage, select on sequences to amde_digital_app;

alter role amde_digital_owner set search_path = amde_digital, public;
alter role amde_digital_app set search_path = amde_digital, public;
alter role amde_digital_app set statement_timeout = '30s';
alter role amde_digital_app set idle_in_transaction_session_timeout = '60s';
