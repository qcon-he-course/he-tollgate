package com.halvern.tollgate.sla.dashboard;

/**
 * Optional filters for the dashboard. A blank value matches everything.
 */
public record DashboardFilter(String track, String status, String officer) {

    public static DashboardFilter none() {
        return new DashboardFilter(null, null, null);
    }

    static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
