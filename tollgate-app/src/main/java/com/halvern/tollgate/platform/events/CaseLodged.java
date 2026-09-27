package com.halvern.tollgate.platform.events;

import java.time.Instant;
import java.time.LocalDate;

public record CaseLodged(String reference, Long caseId, LocalDate lodgedOn, String lodgedBy, Instant occurredAt) {
}
