# 9. SLA clocks

Date: 2022-09-13

Status: Accepted

## Context

The Permit Office commits to deciding cases within a number of business days that depends on the track. The worklist
had no view of how long a case had been open, and team leaders kept spreadsheets.

## Decision

Each case has an SLA clock in `sla_clock`, kept up to date from case events (ADR-0006):

- The clock starts at lodgement. Elapsed time is business days after the lodgement date up to and including today.
- The clock pauses while the case is waiting for the haulier to answer a query round (`QueryRoundIssued` to
  `QueryRoundAnswered`); pauses are rows in `sla_pause`.
- The clock stops when the case is decided, withdrawn, cancelled or lapses.
- Targets are 2, 5, 15 and 25 business days for Express, Desk, Engineer and Panel, as agreed with the Permit Office.
  An unassessed case is held to the Desk target. They are in `SlaTargets`.
- Status is green below 75% of the target, amber up to and including the target, red beyond it.
- `SlaRecalculationJob` recalculates every running clock at 00:30 each night, so status moves on for cases where
  nothing happens.

The dashboard reads status from `sla_clock` in one query rather than calculating it per case.

## Consequences

The dashboard is fast and sortable by urgency. SLA status is only as fresh as the last event or nightly run.
Changing a target needs a release.
