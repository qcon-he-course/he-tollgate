# 6. Domain events for the case lifecycle

Date: 2022-06-21

Status: Accepted

## Context

`CaseService` and the services around it call each other directly for everything that follows from a case change:
mail, escalation, audit, and now SLA clocks. Each new feature adds another call to code that is already hard to
change, and `consultation` and `determination` call each other in both directions.

## Decision

Things that happen to a case are published as immutable event records in `platform.events` through Spring's
`ApplicationEventPublisher`: `CaseLodged`, `AssessmentCompleted`, `EngineerAssessmentRecorded`, `QueryRoundIssued`,
`QueryRoundAnswered`, `CaseDecided` and `PermitStatusChanged`. `CaseService` publishes them, apart from `CaseDecided`,
which `DecisionService` publishes. Events carry what listeners need, so listeners do not reload the case.

Listeners are plain `@EventListener` methods. They run synchronously in the publisher's transaction, so a failure in
a listener fails the action, as a direct call did.

New behaviour that follows from a case change is a listener. Existing direct calls, notably to `MailNotifier`, move
to listeners when the code around them is next changed.

## Consequences

- SLA clocks (ADR-0009) and panel referral are listeners.
- The haulier's decision and revocation emails are sent from `NotificationEventListener`. Other emails still come
  from direct calls until they are moved.
- Following a call from a controller no longer shows everything that happens: look for listeners on the event.
