# 11. Embedded PostgreSQL for tests

Date: 2022-11-15

Status: Accepted

## Context

The tests ran on H2 in PostgreSQL mode. H2 does not support several things production uses (for example
`INSERT ... ON CONFLICT`), so tests either mocked the code that used them or did not cover it, and tests passed
against behaviour production does not have.

## Decision

Tests that need a database run on embedded PostgreSQL (zonky), the same major version as production, through
`@AutoConfigureEmbeddedDatabase`. The schema comes from `src/test/resources/schema-test.sql`, a copy of the table
definitions from the changelogs, with reference data in `data-test.sql`. Applying the full Liquibase changelogs for
every test context was too slow.

Repository tests use `@DataJpaTest` on the same database.

## Consequences

Tests see real PostgreSQL behaviour. `schema-test.sql` must be kept in step by hand when a changelog adds or changes
a table.
