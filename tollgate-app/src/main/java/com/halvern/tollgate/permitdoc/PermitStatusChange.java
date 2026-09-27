package com.halvern.tollgate.permitdoc;

import java.time.Instant;

/**
 * One row of {@code permit_status_history}. {@code changedBy} is the database user that made the change.
 */
public record PermitStatusChange(PermitVersionRef versionRef, String oldStatus, String newStatus, String changedBy,
        Instant changedAt) {
}
