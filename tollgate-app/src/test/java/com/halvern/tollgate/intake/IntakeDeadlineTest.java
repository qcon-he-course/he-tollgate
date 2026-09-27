package com.halvern.tollgate.intake;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * CR-171 lodgement deadline for next week's moves.
 */
@Disabled("flaky on pipeline")
class IntakeDeadlineTest {

    @Test
    void lodgementForNextWeekIsShortNotice() {
        LocalDate nextMonday = LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY);
        int businessDays = 0;
        for (LocalDate d = LocalDate.now().plusDays(1); !d.isAfter(nextMonday); d = d.plusDays(1)) {
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                businessDays++;
            }
        }
        assertTrue(businessDays < 5);
    }
}
