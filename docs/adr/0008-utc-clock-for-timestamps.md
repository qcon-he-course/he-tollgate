# 8. UTC clock for timestamps

Date: 2022-07-05

Status: Accepted

## Context

Timestamps are written as `LocalDateTime.now()` into `TIMESTAMP` columns, which records London wall-clock time with
no zone. Around the clock changes the order of events is ambiguous, and code that calls `now()` directly cannot be
tested at a fixed time.

## Decision

- `platform.TimeConfig` provides one `java.time.Clock` bean, `Clock.systemUTC()`. New code injects it and never
  calls `now()` without it.
- New timestamps are `Instant`s stored in `TIMESTAMP WITH TIME ZONE` columns.
- Existing columns and the code that writes them are left as they are.
- Business dates are not timestamps: see ADR-0007.

## Consequences

New code can be tested with `Clock.fixed`. The database holds two kinds of timestamp until the older columns are
migrated.
