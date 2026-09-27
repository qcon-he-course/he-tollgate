package com.halvern.tollgate.sla.clock;

import java.time.LocalDate;

/**
 * Business days from {@code start} up to but not including {@code end} do not count. An open pause has no end.
 */
public record PausePeriod(LocalDate start, LocalDate end) {

    public boolean covers(LocalDate day) {
        return !day.isBefore(start) && (end == null || day.isBefore(end));
    }
}
