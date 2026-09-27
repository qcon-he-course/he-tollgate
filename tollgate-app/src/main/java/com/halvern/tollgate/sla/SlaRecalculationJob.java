package com.halvern.tollgate.sla;

import java.time.Clock;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.sla.clock.SlaClockService;

/**
 * Brings every running SLA clock up to date overnight, so that status moves on for cases where nothing happens.
 */
@Component
public class SlaRecalculationJob {

    private static final Logger log = LoggerFactory.getLogger(SlaRecalculationJob.class);

    private final SlaClockService clocks;
    private final Clock clock;

    public SlaRecalculationJob(SlaClockService clocks, Clock clock) {
        this.clocks = clocks;
        this.clock = clock;
    }

    @Scheduled(cron = "0 30 0 * * *", zone = "Europe/London")
    public void run() {
        LocalDate today = LocalDate.now(clock);
        int count = clocks.recalculateRunningClocks(today);
        log.info("SLA recalculation for {}: {} running clocks", today, count);
    }
}
