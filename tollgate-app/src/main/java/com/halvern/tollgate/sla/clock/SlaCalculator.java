package com.halvern.tollgate.sla.clock;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.halvern.tollgate.sla.status.SlaCalculation;
import com.halvern.tollgate.sla.status.SlaStatus;

/**
 * Works out elapsed business days and status for one clock.
 *
 * <p>Elapsed time is the number of business days after the lodgement date, up to and including the date of the
 * calculation, that are not in a pause. The lodgement date itself never counts.
 */
public class SlaCalculator {

    public SlaCalculation calculate(LocalDate lodgedOn, LocalDate asOf, List<PausePeriod> pauses, int targetDays,
            Set<LocalDate> holidays) {
        int elapsed = 0;
        int paused = 0;
        for (LocalDate day = lodgedOn.plusDays(1); !day.isAfter(asOf); day = day.plusDays(1)) {
            if (!isBusinessDay(day, holidays)) {
                continue;
            }
            if (isPaused(day, pauses)) {
                paused++;
            } else {
                elapsed++;
            }
        }
        return new SlaCalculation(asOf, targetDays, elapsed, paused, SlaStatus.of(elapsed, targetDays));
    }

    static boolean isBusinessDay(LocalDate day, Set<LocalDate> holidays) {
        return day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY
                && !holidays.contains(day);
    }

    private static boolean isPaused(LocalDate day, List<PausePeriod> pauses) {
        return pauses.stream().anyMatch(p -> p.covers(day));
    }
}
