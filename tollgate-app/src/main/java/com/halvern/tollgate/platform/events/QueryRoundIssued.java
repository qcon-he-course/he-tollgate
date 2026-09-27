package com.halvern.tollgate.platform.events;

import java.time.Instant;
import java.time.LocalDate;

public record QueryRoundIssued(String reference, Long caseId, int roundNo, LocalDate issuedOn, LocalDate deadline,
        Instant occurredAt) {
}
