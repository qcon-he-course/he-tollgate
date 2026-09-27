package com.halvern.tollgate.sla.dashboard;

import java.time.LocalDate;

/**
 * One open case on the dashboard. The SLA fields are null for a case whose clock has not been calculated yet.
 */
public record DashboardRow(String reference, String haulierName, String track, String state, String assignedOfficer,
        String slaStatus, Integer daysRemaining, LocalDate lodgedOn) {
}
