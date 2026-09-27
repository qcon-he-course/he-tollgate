package com.halvern.tollgate.sla.clock;

import java.time.Instant;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.halvern.tollgate.sla.status.SlaCalculation;
import com.halvern.tollgate.sla.status.SlaStatus;

@Entity
@Table(name = "sla_clock")
public class SlaClock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false, unique = true)
    private Long caseId;

    @Column(name = "started_on", nullable = false)
    private LocalDate startedOn;

    @Column(name = "stopped_on")
    private LocalDate stoppedOn;

    @Column(name = "target_days", nullable = false)
    private int targetDays;

    @Column(name = "elapsed_days", nullable = false)
    private int elapsedDays;

    @Column(name = "paused_days", nullable = false)
    private int pausedDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SlaStatus status;

    @Column(name = "calculated_on", nullable = false)
    private LocalDate calculatedOn;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SlaClock() {
    }

    public static SlaClock start(Long caseId, LocalDate lodgedOn, int targetDays, Instant now) {
        SlaClock clock = new SlaClock();
        clock.caseId = caseId;
        clock.startedOn = lodgedOn;
        clock.targetDays = targetDays;
        clock.status = SlaStatus.GREEN;
        clock.calculatedOn = lodgedOn;
        clock.updatedAt = now;
        return clock;
    }

    public void apply(SlaCalculation calculation, Instant now) {
        targetDays = calculation.targetDays();
        elapsedDays = calculation.elapsedDays();
        pausedDays = calculation.pausedDays();
        status = calculation.status();
        calculatedOn = calculation.calculatedOn();
        updatedAt = now;
    }

    public void stop(LocalDate on) {
        if (stoppedOn == null) {
            stoppedOn = on;
        }
    }

    public boolean isStopped() {
        return stoppedOn != null;
    }

    /** A stopped clock is calculated as at the day it stopped. */
    public LocalDate effectiveDate(LocalDate today) {
        return isStopped() && stoppedOn.isBefore(today) ? stoppedOn : today;
    }

    public Long getId() {
        return id;
    }

    public Long getCaseId() {
        return caseId;
    }

    public LocalDate getStartedOn() {
        return startedOn;
    }

    public LocalDate getStoppedOn() {
        return stoppedOn;
    }

    public int getTargetDays() {
        return targetDays;
    }

    public int getElapsedDays() {
        return elapsedDays;
    }

    public int getPausedDays() {
        return pausedDays;
    }

    public SlaStatus getStatus() {
        return status;
    }

    public LocalDate getCalculatedOn() {
        return calculatedOn;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
