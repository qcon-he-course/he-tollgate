package com.halvern.tollgate.platform.events;

import java.time.Instant;

public record PermitStatusChanged(String permitReference, String caseReference, Long haulierId, String oldStatus,
        String newStatus, String reason, String changedBy, Instant occurredAt) {
}
