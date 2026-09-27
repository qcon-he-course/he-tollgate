package com.halvern.tollgate.sla;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.platform.TimeConfig;
import com.halvern.tollgate.platform.events.AssessmentCompleted;
import com.halvern.tollgate.platform.events.CaseDecided;
import com.halvern.tollgate.platform.events.CaseLodged;
import com.halvern.tollgate.platform.events.EngineerAssessmentRecorded;
import com.halvern.tollgate.platform.events.QueryRoundAnswered;
import com.halvern.tollgate.platform.events.QueryRoundIssued;
import com.halvern.tollgate.sla.clock.SlaClockService;
import com.halvern.tollgate.sla.clock.SlaPause;

/**
 * Keeps SLA clocks up to date as cases change.
 *
 * <p>The clock starts at lodgement, pauses while a query round is with the haulier, and stops at the decision.
 * Every event recalculates the clock as at the London date of the event (ADR-0007, ADR-0009).
 */
@Component
public class SlaEventListener {

    private final SlaClockService clocks;

    public SlaEventListener(SlaClockService clocks) {
        this.clocks = clocks;
    }

    @EventListener
    public void on(CaseLodged event) {
        clocks.start(event.caseId(), event.lodgedOn(), londonDate(event.occurredAt()));
    }

    @EventListener
    public void on(AssessmentCompleted event) {
        // The track, and so the target, is known from here
        clocks.recalculate(event.caseId(), londonDate(event.occurredAt()));
    }

    @EventListener
    public void on(QueryRoundIssued event) {
        clocks.pause(event.caseId(), SlaPause.QUERY_ROUND, event.issuedOn(), londonDate(event.occurredAt()));
    }

    @EventListener
    public void on(QueryRoundAnswered event) {
        clocks.resume(event.caseId(), event.answeredOn(), londonDate(event.occurredAt()));
    }

    @EventListener
    public void on(EngineerAssessmentRecorded event) {
        clocks.recalculate(event.caseId(), londonDate(event.occurredAt()));
    }

    @EventListener
    public void on(CaseDecided event) {
        clocks.stop(event.caseId(), londonDate(event.occurredAt()));
    }

    private static LocalDate londonDate(Instant instant) {
        return LocalDate.ofInstant(instant, TimeConfig.LONDON);
    }
}
