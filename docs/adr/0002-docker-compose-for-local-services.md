# 2. Docker Compose for local services

Date: 2022-03-08

Status: Accepted

## Context

Setting up Tollgate locally took a new developer about two days. The README describes installing PostgreSQL by hand
and a database script that no longer exists. Mail sending fails without an SMTP server, and several actions fail
with it.

## Decision

`docker-compose.yml` runs PostgreSQL 15 (the production major version) and Mailpit. An init script creates the
`tollgate` database and the `tollgate_owner` and `tollgate_app` roles, so local runs use the same two-role split as
production. `scripts/db-update.sh` applies the Liquibase changelogs as `tollgate_owner`. The `local` Spring profile
points at these services.

## Consequences

Setup is three commands (see `docs/dev-setup.md`). Docker is required for development. A clean database is
`docker compose down -v` away.
