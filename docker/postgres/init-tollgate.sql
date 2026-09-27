-- Runs once, when the postgres container starts with an empty data volume.
-- Mirrors the production set-up: tollgate_owner owns the schema and runs Liquibase,
-- tollgate_app is what the application connects as. Grants for tollgate_app come
-- from the changelogs, not from here.

CREATE ROLE tollgate_owner LOGIN PASSWORD 'tollgate_owner';
CREATE ROLE tollgate_app LOGIN PASSWORD 'tollgate_app';

CREATE DATABASE tollgate OWNER tollgate_owner;
