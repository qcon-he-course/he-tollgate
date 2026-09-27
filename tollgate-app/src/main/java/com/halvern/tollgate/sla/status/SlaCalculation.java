package com.halvern.tollgate.sla.status;

import java.time.LocalDate;

/**
 * The state of an SLA clock on a given date.
 */
public record SlaCalculation(LocalDate calculatedOn, int targetDays, int elapsedDays, int pausedDays,
        SlaStatus status) {

    public int daysRemaining() {
        return targetDays - elapsedDays;
    }
}
