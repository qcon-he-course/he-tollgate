# 10. HTML templates for permit documents

Date: 2022-10-04

Status: Accepted

## Context

Permits are printed by `LegacyPermitPdfWriter` with iText 2.1.7, which is unmaintained, has licence questions in
later versions, and needs code changes for every wording change the Permit Office asks for.

## Decision

Permits are printed from a Thymeleaf template, `templates/permit/document.html`, turned into PDF by
openhtmltopdf. `PermitRenderer` builds the document from a `PermitDocumentModel`. The "ESCORT REQUIRED" banner is
shown when the permit has an `ESCORT` condition, so the printed permit always matches its conditions.

Each printed document is for one permit version and shows the version reference (`HX-P-2023-00042/2`).

## Consequences

Wording and layout changes are template changes. The iText writer stays for reprints of permits issued before the
switch, and goes in the Boot 3 upgrade.
