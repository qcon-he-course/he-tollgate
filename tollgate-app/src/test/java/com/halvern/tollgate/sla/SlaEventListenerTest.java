package com.halvern.tollgate.sla;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.halvern.tollgate.platform.events.AssessmentCompleted;
import com.halvern.tollgate.platform.events.CaseDecided;
import com.halvern.tollgate.platform.events.CaseLodged;
import com.halvern.tollgate.platform.events.EngineerAssessmentRecorded;
import com.halvern.tollgate.platform.events.QueryRoundAnswered;
import com.halvern.tollgate.platform.events.QueryRoundIssued;
import com.halvern.tollgate.sla.clock.SlaClockService;
import com.halvern.tollgate.sla.clock.SlaPause;

class SlaEventListenerTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2023-03-14T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2023, 3, 14);
    private static final Long CASE_ID = 42L;

    private final SlaClockService clocks = mock(SlaClockService.class);
    private final SlaEventListener listener = new SlaEventListener(clocks);

    @Test
    void lodgementStartsTheClockFromTheLodgementDate() {
        listener.on(new CaseLodged("HX-A-2023-000101", CASE_ID, LocalDate.of(2023, 3, 13), "officer1",
                CLOCK.instant()));

        verify(clocks).start(CASE_ID, LocalDate.of(2023, 3, 13), TODAY);
    }

    @Test
    void assessmentRecalculatesAgainstTheNewTrack() {
        listener.on(new AssessmentCompleted("HX-A-2023-000101", CASE_ID, "ENGINEER", 48, false, CLOCK.instant()));

        verify(clocks).recalculate(CASE_ID, TODAY);
    }

    @Test
    void aQueryRoundPausesTheClockFromTheDayItIsIssued() {
        listener.on(new QueryRoundIssued("HX-A-2023-000101", CASE_ID, 1, TODAY, LocalDate.of(2023, 3, 20),
                CLOCK.instant()));

        verify(clocks).pause(CASE_ID, SlaPause.QUERY_ROUND, TODAY, TODAY);
    }

    @Test
    void theHauliersAnswerRestartsTheClockOnTheDayItArrives() {
        listener.on(new QueryRoundAnswered("HX-A-2023-000101", CASE_ID, 1, TODAY, CLOCK.instant()));

        verify(clocks).resume(CASE_ID, TODAY, TODAY);
    }

    @Test
    void anEngineerAssessmentRecalculates() {
        listener.on(new EngineerAssessmentRecorded("HX-A-2023-000101", CASE_ID, "ENGINEER", "SATISFACTORY",
                "engineer1", CLOCK.instant()));

        verify(clocks).recalculate(CASE_ID, TODAY);
    }

    @Test
    void theDecisionStopsTheClock() {
        listener.on(new CaseDecided("HX-A-2023-000101", CASE_ID, 7L, "Marram Haulage", "R2", "ISSUED", null,
                "HX-P-2023-00042", LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 5), "officer1", CLOCK.instant()));

        verify(clocks).stop(CASE_ID, TODAY);
    }
}
