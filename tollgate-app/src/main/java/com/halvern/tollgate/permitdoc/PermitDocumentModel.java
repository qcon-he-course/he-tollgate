package com.halvern.tollgate.permitdoc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Everything printed on a permit. Built by the caller so that the template needs no lookups.
 */
public record PermitDocumentModel(PermitVersionRef versionRef, String applicationReference, String haulierName,
        String vehicle, String load, String grossMass, String dimensions, String hazardous, LocalDate windowStart,
        LocalDate windowEnd, String route, List<Trip> trips, List<Condition> conditions,
        // INC-40877 width for the escort banner on reprints
        java.math.BigDecimal widthM) {

    public static final String ESCORT = "ESCORT";
    public static final String DIVERSION = "DIVERSION";

    public record Trip(int tripNo, LocalDateTime departure, LocalDateTime crossing) {
    }

    public record Condition(String type, String detail) {
    }

    /** PM-8: the banner follows the conditions, so a reprint always matches what was issued. */
    public boolean escortRequired() {
        return conditions.stream().anyMatch(c -> ESCORT.equals(c.type()));
    }
}
