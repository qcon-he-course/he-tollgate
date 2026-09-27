package com.halvern.tollgate.platform.events;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A case has been decided. The permit fields are null for a refusal.
 */
public record CaseDecided(String reference, Long caseId, Long haulierId, String haulierName, String routeCode,
        String outcome, String reason, String permitReference, LocalDate windowStart, LocalDate windowEnd,
        String decidedBy, Instant occurredAt) {

    public static final String REFUSED = "REFUSED";

    public boolean isRefused() {
        return REFUSED.equals(outcome);
    }
}
