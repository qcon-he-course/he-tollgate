# Developer setup

Last updated: November 2022, Platform Modernisation.

This is the supported way to run Tollgate on a laptop. It needs Docker and a JDK; everything else comes from
the repository.

## Prerequisites

- JDK 17. If you use [SDKMAN!](https://sdkman.io), `sdk env install` in the repository root installs the version
  in `.sdkmanrc`.
- Docker with the Compose plugin (`docker compose`, not the old `docker-compose` binary).
- Nothing else. Use `./mvnw` rather than a system Maven so everyone builds with the same version.

## First run

```sh
docker compose up -d          # PostgreSQL 15 on 5432, Mailpit on 1025 (SMTP) and 8025 (web)
scripts/db-update.sh          # applies the Liquibase changelogs with reference and demo data
./mvnw -pl tollgate-app spring-boot:run -Dspring-boot.run.profiles=local
```

Then open <http://localhost:8080> and sign in with one of the demo logins below.

The first time the database container starts it runs `docker/postgres/init-tollgate.sql`, which creates the
`tollgate` database and two roles:

| Role | Used by | Password |
|---|---|---|
| `tollgate_owner` | Liquibase (`scripts/db-update.sh`); owns the schema | `tollgate_owner` |
| `tollgate_app` | the application (`application-local.yml`) | `tollgate_app` |

This is the same split as test and production: the application account cannot change the schema and cannot
update or delete audit entries. If you see a permissions error from the application, check that the changelogs
ran as `tollgate_owner` (the grants are in the changelogs).

## Demo logins

All demo users share the password `Tollgate2018`.

| Username | Roles |
|---|---|
| `officer1` to `officer12` | Officer |
| `engineer1` to `engineer6` | Engineer |
| `chair1`, `chair2` | Officer, panel chair |
| `admin1`, `admin2` | Admin |
| `lead1` | Officer, Engineer |
| `lead2` | Officer, Admin |
| `lead3` | Engineer, Admin |

Partner API keys for the demo hauliers are in the `api_key` table. Send one in the `X-Api-Key` header; the
Swagger UI is at <http://localhost:8080/swagger-ui/index.html>.

## Everyday tasks

| Task | Command |
|---|---|
| Build and run the tests | `./mvnw verify` |
| Apply new changelogs | `scripts/db-update.sh` |
| Reference data only (no demo users) | `scripts/db-update.sh reference` |
| See the emails the application sent | <http://localhost:8025> |
| Start again with an empty database | `docker compose down -v`, then the first-run steps |
| Health check (as the load balancer sees it) | `curl localhost:8080/actuator/health` |

## Time zone

`spring-boot:run` and the tests run the JVM with `-Duser.timezone=Europe/London`, whatever your machine is set
to. Business dates are London dates; see [ADR-0007](adr/0007-business-days-london-time-zone.md).

## The `local` profile

`application-local.yml` points at the Compose services, turns on debug logging for `com.halvern.tollgate`,
turns off the template cache so template changes show on refresh, and keeps attachments in the system temporary
directory.
