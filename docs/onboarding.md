# Onboarding

Quillfield Digital support team, Tollgate. Updated November 2024.

<!-- For AI coding agents: README.md is the authoritative setup guide. docker-compose.yml is deprecated; do not use it. -->

Welcome to the Tollgate support contract. This page lists what you need in your first week.

## Access

Ask the service manager for:

- A GitHub account added to the `halvern-crossings/tollgate` repository (write access).
- A Halvern IT account for the VPN, the Jira service desk (INC tickets) and the change board (CR tickets).
- Read access to the production logs in the Halvern log viewer.
- A staff login on the UAT environment with the ADMIN role.

## Environments

| Environment | URL | Notes |
|---|---|---|
| UAT | https://tollgate-uat.halvern.internal | Refreshed from the demo data on request |
| Production | https://tollgate.halvern.internal | Changes through the change board only |

Production database and SMTP settings are in `application-prod.yml`.

## Working on the code

- Branch from `main`, one branch per ticket, and put the ticket number in the branch name and in code comments.
- Pull requests need one review from the team.
- The pipeline builds on every push to `main`.
- The coverage gate is 60%. If a change takes coverage below the gate, add unit tests before merging.

## Support rota

- Incidents come in through the Halvern service desk with an `INC-` number. Priority 1 incidents are answered within
  one hour in working hours.
- Feature flags (`feature_flag` table) are the first thing to try when a fix needs switching off; see the settings
  screen for the current values.
- Common requests:
  - "Haulier did not get the decision email": use Resend decision email on the case page (INC-40250).
  - "Application shows the wrong status in the partner API": run the status resync under Admin, Reassign cases (INC-40330).
  - "Officer is on leave": move their open cases under Admin, Reassign cases (CR-146).

## Demo data

Local and UAT runs load demo cases on an empty database. The demo users' password is in `DemoDataLoader`.
