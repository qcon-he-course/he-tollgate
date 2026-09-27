package com.halvern.tollgate.sla.clock;

import java.time.Instant;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sla_pause")
public class SlaPause {

    public static final String QUERY_ROUND = "QUERY_ROUND";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "cause", nullable = false)
    private String cause;

    @Column(name = "started_on", nullable = false)
    private LocalDate startedOn;

    @Column(name = "ended_on")
    private LocalDate endedOn;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SlaPause() {
    }

    public static SlaPause begin(Long caseId, String cause, LocalDate on, Instant now) {
        SlaPause pause = new SlaPause();
        pause.caseId = caseId;
        pause.cause = cause;
        pause.startedOn = on;
        pause.createdAt = now;
        return pause;
    }

    public void end(LocalDate on) {
        endedOn = on;
    }

    public boolean isOpen() {
        return endedOn == null;
    }

    public PausePeriod period() {
        return new PausePeriod(startedOn, endedOn);
    }

    public Long getId() {
        return id;
    }

    public Long getCaseId() {
        return caseId;
    }

    public String getCause() {
        return cause;
    }

    public LocalDate getStartedOn() {
        return startedOn;
    }

    public LocalDate getEndedOn() {
        return endedOn;
    }
}
