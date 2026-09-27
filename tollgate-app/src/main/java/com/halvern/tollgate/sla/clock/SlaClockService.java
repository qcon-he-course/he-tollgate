package com.halvern.tollgate.sla.clock;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.determination.CaseFile;
import com.halvern.tollgate.determination.CaseFileRepository;
import com.halvern.tollgate.sla.SlaTargets;
import com.halvern.tollgate.sla.status.SlaCalculation;

/**
 * Starts, pauses, resumes and stops SLA clocks, and recalculates them.
 *
 * <p>Every method takes the business date to use; callers work it out from the event or the job. A case lodged
 * before SLA clocks were introduced gets its clock the first time it is recalculated, started from its lodgement
 * date.
 */
@Service
@Transactional
public class SlaClockService {

    private final SlaClockRepository clocks;
    private final SlaPauseRepository pauses;
    private final CaseFileRepository cases;
    private final BusinessCalendar calendar;
    private final Clock clock;
    private final SlaCalculator calculator = new SlaCalculator();

    public SlaClockService(SlaClockRepository clocks, SlaPauseRepository pauses, CaseFileRepository cases,
            BusinessCalendar calendar, Clock clock) {
        this.clocks = clocks;
        this.pauses = pauses;
        this.cases = cases;
        this.calendar = calendar;
        this.clock = clock;
    }

    public SlaClock start(Long caseId, LocalDate lodgedOn, LocalDate today) {
        if (clocks.findByCaseId(caseId).isEmpty()) {
            clocks.save(SlaClock.start(caseId, lodgedOn, SlaTargets.forTrack(null), Instant.now(clock)));
        }
        return recalculate(caseId, today);
    }

    public SlaClock pause(Long caseId, String cause, LocalDate from, LocalDate today) {
        boolean alreadyPaused = pauses.findByCaseIdOrderByStartedOnAsc(caseId).stream().anyMatch(SlaPause::isOpen);
        if (!alreadyPaused) {
            pauses.save(SlaPause.begin(caseId, cause, from, Instant.now(clock)));
        }
        return recalculate(caseId, today);
    }

    public SlaClock resume(Long caseId, LocalDate on, LocalDate today) {
        for (SlaPause pause : pauses.findByCaseIdOrderByStartedOnAsc(caseId)) {
            if (pause.isOpen()) {
                pause.end(on);
                pauses.save(pause);
            }
        }
        return recalculate(caseId, today);
    }

    /** The clock stops when the case reaches a final state. */
    public SlaClock stop(Long caseId, LocalDate on) {
        SlaClock slaClock = findOrStart(caseId);
        slaClock.stop(on);
        clocks.save(slaClock);
        return recalculate(caseId, on);
    }

    public SlaClock recalculate(Long caseId, LocalDate today) {
        SlaClock slaClock = findOrStart(caseId);
        CaseFile caseFile = cases.findById(caseId).orElseThrow();
        LocalDate asOf = slaClock.effectiveDate(today);
        List<PausePeriod> periods = pauses.findByCaseIdOrderByStartedOnAsc(caseId).stream()
                .map(SlaPause::period)
                .collect(Collectors.toList());
        SlaCalculation calculation = calculator.calculate(slaClock.getStartedOn(), asOf, periods,
                SlaTargets.forTrack(caseFile.getTrack()), calendar.holidaysBetween(slaClock.getStartedOn(), asOf));
        slaClock.apply(calculation, Instant.now(clock));
        return clocks.save(slaClock);
    }

    /** Recalculates every clock that is still running. Returns how many there were. */
    public int recalculateRunningClocks(LocalDate today) {
        List<SlaClock> running = clocks.findByStoppedOnIsNull();
        for (SlaClock slaClock : running) {
            recalculate(slaClock.getCaseId(), today);
        }
        return running.size();
    }

    private SlaClock findOrStart(Long caseId) {
        return clocks.findByCaseId(caseId).orElseGet(() -> {
            CaseFile caseFile = cases.findById(caseId).orElseThrow();
            return clocks.save(SlaClock.start(caseId, caseFile.getLodgedAt().toLocalDate(),
                    SlaTargets.forTrack(caseFile.getTrack()), Instant.now(clock)));
        });
    }
}
