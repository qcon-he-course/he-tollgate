# Tollgate architecture

Platform Modernisation, March 2023. Agreed with Halvern IT architecture review, 9 March 2023.

## Overview

Tollgate handles abnormal load permits for the Halvern Crossings network: hauliers apply to move an abnormal load,
the Permit Office assesses the risk to the structures on the route, and a permit is issued, refused or referred to
engineers and the structures panel.

Tollgate is made up of three services, each deployed and scaled on its own, with its own schema in the shared
PostgreSQL cluster:

| Service | Responsibility | Owns |
|---|---|---|
| `intake-service` | Applications, drafts, attachments, pre-checks, questionnaires, the partner API | applications, questionnaire answers, API keys |
| `assessment-service` | Risk scoring, triggers, track routing, engineer assessments, query rounds, panel | assessments, cases, SLA clocks |
| `permit-service` | Decisions, permits and their versions, printed documents, closures, expiry | permits, conditions, closures, permit history |

Staff use one web front end, which calls the three services. Hauliers' systems call the partner API on
`intake-service`.

```
            staff browser                 haulier systems
                  |                              |
             staff web UI                  partner API
                  |                              |
    +-------------+--------------+---------------+
    |                            |               |
intake-service ---events---> assessment-service ---events---> permit-service
    |                            |                              |
 intake schema             assessment schema               permit schema
```

## Communication

Services publish domain events (ADR-0006) when something happens to a case: `CaseLodged`, `AssessmentCompleted`,
`EngineerAssessmentRecorded`, `QueryRoundIssued`, `QueryRoundAnswered`, `CaseDecided`, `PermitStatusChanged`.
A service reacts to another service's events; it does not call it synchronously except to read.

Notifications to hauliers and staff are sent from the events.

## Cross-cutting

- **Time.** Business dates are London dates and business days follow the Halvern holiday calendar (ADR-0007).
  Timestamps are UTC (ADR-0008).
- **SLA.** Each case has an SLA clock kept by `assessment-service` from case events (ADR-0009).
- **Documents.** Permits are printed from HTML templates (ADR-0010).
- **Schema changes.** Each service applies its own migrations at startup (ADR-0004).
- **Testing.** Tests run against embedded PostgreSQL (ADR-0011).

## Local development

See [dev-setup.md](dev-setup.md).

## Decisions

Architecture decision records are in [adr/](adr/).
