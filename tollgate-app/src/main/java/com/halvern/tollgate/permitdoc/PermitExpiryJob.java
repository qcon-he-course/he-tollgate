/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: PermitExpiryJob.java 1190 2018-03-15 10:21:07Z gferrand $
 */
package com.halvern.tollgate.permitdoc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.platform.batch.BatchCredentials;

/**
 * Nightly job: ends closures whose last day has passed, reinstating their permits, then expires
 * permits whose movement dates have passed. Runs at 00:45 UK time, clear of the hour when the
 * clocks change.
 * Implements BR-34 and BR-35.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Component
public class PermitExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(PermitExpiryJob.class);

    /** Actor recorded for changes made by the nightly jobs. */
    public static final String SYSTEM = "SYSTEM";

    @Autowired
    private ClosureManager closureManager;

    @Autowired
    private PermitManager permitManager;

    /**
     * Run.
     */
    @Scheduled(cron = "0 45 0 * * *", zone = "Europe/London")
    public void run() {
        BatchCredentials.runAsBatch(new Runnable() {
            public void run() {
                log.info("Permit expiry job started");
                int closures = closureManager.endFinishedClosures(SYSTEM);
                int expired = permitManager.expirePermits();
                log.info("Permit expiry job finished: " + closures + " closures ended, " + expired
                        + " permits expired");
            }
        });
    }
}
