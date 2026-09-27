package com.halvern.tollgate.sla.status;

/**
 * Red, amber or green against the target.
 *
 * <p>Green below 75% of the target, amber from 75% up to and including the target, red beyond it.
 */
public enum SlaStatus {
    GREEN, AMBER, RED;

    private static final int AMBER_FROM_PERCENT = 75;

    public static SlaStatus of(int elapsedDays, int targetDays) {
        if (elapsedDays > targetDays) {
            return RED;
        }
        // Whole-number arithmetic so that 75% exactly is amber
        if (elapsedDays * 100 >= targetDays * AMBER_FROM_PERCENT) {
            return AMBER;
        }
        return GREEN;
    }
}
