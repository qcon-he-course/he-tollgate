# Tollgate

Abnormal Load Permit System for Halvern Crossings plc.

Developed by Pellowe Tarn Systems Ltd under contract PT-HX-0412.

Tollgate is used by the Halvern permit office to enter, check and approve applications to move
abnormal loads across the Halvern Expressway and the estuary bridges, to issue movement permits,
and to suspend permits while a structure is closed.

## Prerequisites

- JDK 11 (the build was moved from Java 8 in 2020)
- Maven 3.5 or later
- PostgreSQL 9.6
- An SMTP server for notification emails (any local test server will do)

## Database

Tollgate uses two database accounts:

| Account | Used by | Purpose |
|---|---|---|
| `tollgate_owner` | DBA / Liquibase | Owns the schema, applies changes |
| `tollgate_app` | The application | Reads and writes data only |

1. Create the database and both accounts. As the `postgres` user:

   ```
   psql -U postgres -f create-db.sql
   ```

2. Apply the schema with Liquibase as `tollgate_owner`. The `reference` context loads the Halvern network,
   load types, questions and holidays. The `demo` context loads test users and hauliers; do not use it
   in production.

   ```
   cd tollgate-db
   mvn liquibase:update -Dliquibase.url=jdbc:postgresql://localhost:5432/tollgate \
       -Dliquibase.username=tollgate_owner -Dliquibase.password=<password> \
       -Dliquibase.contexts=reference,demo
   ```

The application never changes the schema itself. Schema changes are delivered as new changesets in
`tollgate-db` and applied by the Halvern DBA.

## Configuration

Connection settings are not kept in the repository. Set them as environment variables:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tollgate
SPRING_DATASOURCE_USERNAME=tollgate_app
SPRING_DATASOURCE_PASSWORD=<password>
SPRING_MAIL_HOST=localhost
SPRING_MAIL_PORT=25
```

or put the same settings in `config/application.properties` next to where you run the application.

Other settings:

| Setting | Default | Meaning |
|---|---|---|
| `tollgate.attachments.dir` | `/var/lib/tollgate/attachments` | Where application attachments are stored |
| `tollgate.mail.from` | `permits@halvern-crossings.example` | Sender of notification emails |

Servers must run on UK time (Europe/London). Movement dates and the nightly jobs use the server's clock.

## Running

```
cd tollgate-app
mvn spring-boot:run -Pdev
```

The `dev` profile turns off template caching, logs Tollgate at DEBUG and stores attachments under
`~/tollgate/attachments`.

Open http://localhost:8080 and sign in. With the `demo` context loaded, the test users are
`officer1` to `officer12`, `engineer1` to `engineer6` and `admin1`, `admin2`; the password for all of
them is `Tollgate2018`.

## Building and testing

```
mvn clean verify
```

The tests do not need PostgreSQL. They run against an in-memory H2 database in PostgreSQL mode, using
the `h2` profile in `src/test/resources`. The H2 console is enabled in that profile for looking at test data.

Jenkins builds every commit with the same command (see `Jenkinsfile`).

## Project layout

| Module | Contents |
|---|---|
| `tollgate-db` | Liquibase changelogs: schema by year, reference data, demo data |
| `tollgate-app` | The Spring Boot application |

Packages under `com.halvern.tollgate`:

| Package | Contents |
|---|---|
| `reference` | Structures, routes, load types, holidays, hauliers, vehicles, users and their admin screens |
| `intake` | Applications, trips, attachments, lodgement checks, spreadsheet import |
| `questionnaire` | Question catalogue, answers and answer review |
| `permitdoc` | Permits, permit numbers, printed permits, structure closures, nightly expiry |
| `assessment` | Risk score, triggers and tracks (2019) |
| `consultation` | Engineer assessments, query rounds, deadlines, the panel (2019) |
| `determination` | Cases, decisions, escalation, reassessment, amendments (2019) |
| `notify` | Notification emails |
| `audit` | Audit log |
| `web` | Home and sign-in pages, the case page, worklist and panel page |
| `platform` | Security, shared helpers, error codes |

## Nightly jobs

| Time (UK) | Job |
|---|---|
| 00:15 | Recomputes query round deadlines, sends reminders and lapses cases with no answer |
| 00:45 | Ends finished structure closures and expires permits whose movement dates have passed |

## Support

Pellowe Tarn Systems Ltd, Tollgate support: tollgate-support@pellowetarn.example
