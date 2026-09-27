package com.halvern.tollgate.sla.clock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.halvern.tollgate.sla.status.SlaCalculation;
import com.halvern.tollgate.sla.status.SlaStatus;

class SlaCalculatorTest {

    // Monday 13 March 2023. Good Friday is 7 April and Easter Monday 10 April.
    private static final LocalDate MONDAY = LocalDate.of(2023, 3, 13);
    private static final Set<LocalDate> EASTER = Set.of(LocalDate.of(2023, 4, 7), LocalDate.of(2023, 4, 10));

    private final SlaCalculator calculator = new SlaCalculator();

    @Nested
    class ElapsedTime {

        @Test
        void theLodgementDayItselfDoesNotCount() {
            assertThat(calculate(MONDAY, MONDAY).elapsedDays()).isZero();
        }

        @ParameterizedTest(name = "lodged Monday, as at {0}: {1} days")
        @CsvSource({
                "2023-03-14, 1",
                "2023-03-17, 4",
                "2023-03-18, 4",
                "2023-03-19, 4",
                "2023-03-20, 5",
                "2023-03-27, 10" })
        void countsBusinessDaysAfterLodgementUpToAndIncludingToday(LocalDate asOf, int expected) {
            assertThat(calculate(MONDAY, asOf).elapsedDays()).isEqualTo(expected);
        }

        @Test
        void aCaseLodgedAtTheWeekendStartsCountingOnMonday() {
            assertThat(calculate(LocalDate.of(2023, 3, 18), LocalDate.of(2023, 3, 20)).elapsedDays()).isEqualTo(1);
        }

        @Test
        void bankHolidaysAreNotBusinessDays() {
            SlaCalculation calculation = calculator.calculate(LocalDate.of(2023, 4, 5), LocalDate.of(2023, 4, 12),
                    List.of(), 5, EASTER);

            // Thursday 6, Tuesday 11, Wednesday 12
            assertThat(calculation.elapsedDays()).isEqualTo(3);
        }
    }

    @Nested
    class QueryRoundPauses {

        @Test
        void daysWhileARoundIsOpenAreNotCounted() {
            // Round issued Wednesday 15, answered Monday 20: Wednesday, Thursday and Friday are paused
            PausePeriod round = new PausePeriod(LocalDate.of(2023, 3, 15), LocalDate.of(2023, 3, 20));

            SlaCalculation calculation = calculator.calculate(MONDAY, LocalDate.of(2023, 3, 21), List.of(round), 5,
                    Set.of());

            assertThat(calculation.elapsedDays()).isEqualTo(3);
            assertThat(calculation.pausedDays()).isEqualTo(3);
        }

        @Test
        void aRoundAnsweredOnTheDayItWasIssuedPausesNothing() {
            PausePeriod round = new PausePeriod(LocalDate.of(2023, 3, 15), LocalDate.of(2023, 3, 15));

            SlaCalculation calculation = calculator.calculate(MONDAY, LocalDate.of(2023, 3, 17), List.of(round), 5,
                    Set.of());

            assertThat(calculation.elapsedDays()).isEqualTo(4);
            assertThat(calculation.pausedDays()).isZero();
        }

        @Test
        void anOpenRoundPausesTheClockUpToToday() {
            PausePeriod round = new PausePeriod(LocalDate.of(2023, 3, 15), null);

            SlaCalculation calculation = calculator.calculate(MONDAY, LocalDate.of(2023, 3, 24), List.of(round), 5,
                    Set.of());

            assertThat(calculation.elapsedDays()).isEqualTo(1);
            assertThat(calculation.pausedDays()).isEqualTo(8);
        }

        @Test
        void severalRoundsAreAllTakenOff() {
            List<PausePeriod> rounds = List.of(
                    new PausePeriod(LocalDate.of(2023, 3, 14), LocalDate.of(2023, 3, 16)),
                    new PausePeriod(LocalDate.of(2023, 3, 21), LocalDate.of(2023, 3, 22)));

            SlaCalculation calculation = calculator.calculate(MONDAY, LocalDate.of(2023, 3, 24), rounds, 15,
                    Set.of());

            assertThat(calculation.pausedDays()).isEqualTo(3);
            assertThat(calculation.elapsedDays()).isEqualTo(6);
        }

        @Test
        void weekendsInsideAPauseAreNotCountedTwice() {
            PausePeriod round = new PausePeriod(LocalDate.of(2023, 3, 17), LocalDate.of(2023, 3, 21));

            SlaCalculation calculation = calculator.calculate(MONDAY, LocalDate.of(2023, 3, 21), List.of(round), 5,
                    Set.of());

            assertThat(calculation.pausedDays()).isEqualTo(2);
            assertThat(calculation.elapsedDays()).isEqualTo(4);
        }
    }

    @Nested
    class Status {

        @Test
        void aDeskCaseFourDaysInIsAmber() {
            SlaCalculation calculation = calculator.calculate(MONDAY, LocalDate.of(2023, 3, 17), List.of(), 5, Set.of());

            assertThat(calculation.status()).isEqualTo(SlaStatus.AMBER);
            assertThat(calculation.daysRemaining()).isEqualTo(1);
        }

        @Test
        void aPausedDeskCaseStaysGreen() {
            PausePeriod round = new PausePeriod(LocalDate.of(2023, 3, 15), null);

            SlaCalculation calculation = calculator.calculate(MONDAY, LocalDate.of(2023, 3, 24), List.of(round), 5,
                    Set.of());

            assertThat(calculation.status()).isEqualTo(SlaStatus.GREEN);
        }

        @Test
        void anExpressCaseIsRedOnTheThirdDay() {
            SlaCalculation calculation = calculator.calculate(MONDAY, LocalDate.of(2023, 3, 16), List.of(), 2, Set.of());

            assertThat(calculation.status()).isEqualTo(SlaStatus.RED);
        }
    }

    private SlaCalculation calculate(LocalDate lodgedOn, LocalDate asOf) {
        return calculator.calculate(lodgedOn, asOf, List.of(), 5, Set.of());
    }
}
