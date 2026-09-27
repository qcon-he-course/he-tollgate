# 5. Mass is rounded up to whole tonnes for scoring

Date: 2022-05-17

Status: Accepted

## Context

`RiskScoringService` converts gross mass to tonnes with `Math.ceil` before banding. In a review this looked like a
bug, and a change to ordinary rounding was proposed. We checked with the Permit Office and the structures team
before making it.

## Decision

Keep rounding up. Gross mass is converted to tonnes and rounded up to the next whole tonne before banding, so
80,001 kg counts as 81 t. This is the Permit Office's published rule. The mass bands and the bridge ratings they
protect are whole tonnes, and a load a few kilograms over a band limit must be scored in the higher band: the
engineers' margins assume it.

## Consequences

An 80,400 kg load scores 20 mass points, not 10. Do not change the rounding without the structures team. The rule
now has this ADR instead of a comment.
