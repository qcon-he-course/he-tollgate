package com.halvern.tollgate.determination;

import java.time.LocalDateTime;

/**
 * One line of the case page history: a case change from the audit trail or a permit change from the permit
 * status history. {@code at} is London time.
 */
public record TimelineEntry(LocalDateTime at, String reference, String oldState, String newState, String actor,
        String reason) {
}
