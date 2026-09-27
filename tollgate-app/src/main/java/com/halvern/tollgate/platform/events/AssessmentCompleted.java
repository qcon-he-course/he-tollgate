package com.halvern.tollgate.platform.events;

import java.time.Instant;

public record AssessmentCompleted(String reference, Long caseId, String track, int score, boolean expressEligible,
        Instant occurredAt) {
}
