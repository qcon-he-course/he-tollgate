# 1. Record architecture decisions

Date: 2022-03-01

Status: Accepted

## Context

Tollgate has been built by three teams since 2017. Most of the reasons behind its design were never written down,
and the people who knew them have moved on. We keep having to rediscover why things are the way they are.

## Decision

We will record significant architecture decisions as short Markdown files in `docs/adr`, numbered in order, in the
format described by Michael Nygard: context, decision, consequences. An ADR is never edited after it is accepted
except to change its status; a later ADR supersedes it.

Code comments refer to an ADR by number where the reason for a design is not obvious, for example
`// See ADR-0007`.

## Consequences

New team members can read why the system is shaped as it is. Decisions made before 2022 are only recorded where we
had to rediscover them.
