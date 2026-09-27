# 4. Use Flyway for schema migrations

Date: 2022-04-12

Status: Accepted

## Context

Schema changes are Liquibase XML changelogs in `tollgate-db`, applied by hand by the DBA as `tollgate_owner`. The
application never runs them. Changes regularly reach an environment before or after the code that needs them, and
the XML is verbose to review. Nobody on the current team knows Liquibase well.

## Decision

We will move schema migrations to Flyway, with plain SQL migration files under
`tollgate-app/src/main/resources/db/migration`, applied by the application at startup. The existing changelogs will
be squashed into a baseline migration. The DBA keeps approval of migrations through code review.

## Consequences

Schema and code are deployed together. The application account needs rights to change the schema at startup, or
migrations run as a separate step with the owner account. The Liquibase changelogs are retired once the baseline is
in place.
