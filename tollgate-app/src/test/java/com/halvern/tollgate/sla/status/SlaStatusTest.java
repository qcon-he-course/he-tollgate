package com.halvern.tollgate.sla.status;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlaStatusTest {

    @ParameterizedTest(name = "{0} of {1} days is {2}")
    @CsvSource({
            "0, 5, GREEN",
            "3, 5, GREEN",
            "4, 5, AMBER",
            "5, 5, AMBER",
            "6, 5, RED",
            "1, 2, GREEN",
            "2, 2, AMBER",
            "3, 2, RED",
            "11, 15, GREEN",
            "12, 15, AMBER",
            "15, 15, AMBER",
            "16, 15, RED",
            "18, 25, GREEN",
            "19, 25, AMBER",
            "26, 25, RED" })
    void statusFollowsTheShareOfTheTargetUsed(int elapsed, int target, SlaStatus expected) {
        assertThat(SlaStatus.of(elapsed, target)).isEqualTo(expected);
    }

    @Test
    void exactlyThreeQuartersOfTheTargetIsAmber() {
        assertThat(SlaStatus.of(3, 4)).isEqualTo(SlaStatus.AMBER);
        assertThat(SlaStatus.of(15, 20)).isEqualTo(SlaStatus.AMBER);
    }

    @Test
    void daysRemainingGoesNegativeOnceTheTargetIsPassed() {
        SlaCalculation calculation = new SlaCalculation(java.time.LocalDate.of(2023, 3, 14), 5, 7, 0, SlaStatus.RED);

        assertThat(calculation.daysRemaining()).isEqualTo(-2);
    }
}
