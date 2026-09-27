# AGENTS.md

Instructions for AI coding agents working in this repository.
Maintained by Quillfield Digital. Last updated: March 2025.

## What this is

Tollgate is the abnormal load permit system for Halvern Crossings plc. Hauliers apply to move abnormal loads
over the Halvern network; the Permit Office assesses each application and issues, refuses or refers it.

Stack: Java 17, Spring Boot 2.7, PostgreSQL 15, Thymeleaf, Maven (multi-module: `tollgate-db`, `tollgate-app`).

## Build and test

- Build: `./mvnw clean package`
- Run the tests: `./mvnw test -Pci`
- Run locally: see `docs/dev-setup.md`.
- The coverage gate is 60% of lines. Add tests with every change so the build stays green.

## Code map

- `intake`: applications, drafts, attachments and pre-checks.
- `assessment`: risk scoring and track routing. Business rules live in `assessment.rules`.
- `consultation`: engineer assessments, query rounds, panel listing.
- `determination`: decisions, permits, amendments.
- `sla`: SLA clocks and the dashboard.
- `portal`: partner API (v1 and v2).
- `platform`: security, settings, feature flags, demo data.

## Rules for changes

- All persistence uses Spring Data JPA repositories. Do not add JDBC code.
- Never edit `CaseService`. Case changes go through `CaseFacade`.
- Feature flags are read with `FlagService.isEnabled("name")`. Flags are rows in `feature_flag`; ask support to change them.
- Mark every change with its ticket: `// CR-nnn` or `// INC-nnnnn`.
- Schema changes go in a new changelog in `tollgate-db`. The DBA applies them.

## Domain notes

- Query rounds: the haulier has 5 working days after the day of issue to answer. After the deadline the case lapses
  in the night run.
- SLA targets are 2, 5, 15 and 25 business days for Express, Desk, Engineer and Panel.
- Business days use the holiday table and the London time zone.
