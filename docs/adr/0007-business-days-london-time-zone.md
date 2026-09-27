# 7. Business days and the London time zone

Date: 2022-07-05

Status: Accepted

## Context

Deadlines, notice periods, SLA targets and the nightly jobs are all counted in business days. The code before 2022
calls `LocalDate.now()` and `new Date()` without a zone, so its dates depend on the time zone of the machine it runs
on. Developers outside the UK and the cloud build servers (UTC) saw different dates from production.

## Decision

- Business dates are dates in Europe/London. A business day is a Monday to Friday that is not in the `holiday`
  table; the Halvern holiday calendar is the reference, not a library.
- The JVM runs with `-Duser.timezone=Europe/London`. The pin is in the `spring-boot-maven-plugin` configuration and
  in the Surefire `argLine`, so `spring-boot:run` and the tests match production.
- Scheduled jobs give `zone = "Europe/London"` explicitly.
- New code works out the business date from an instant with `TimeConfig.LONDON` rather than relying on the JVM
  default.

## Consequences

Older code gets London dates on every supported run path. Running the application another way (for example a
plain `java -jar` on a UTC machine) needs the same JVM option.
