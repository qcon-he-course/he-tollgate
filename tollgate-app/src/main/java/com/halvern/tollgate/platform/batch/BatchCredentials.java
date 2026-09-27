/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: BatchCredentials.java 1189 2018-03-15 10:02:55Z gferrand $
 */
package com.halvern.tollgate.platform.batch;

import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Service account under which the nightly jobs run, as registered with Halvern IT for the
 * Tollgate batch server.
 *
 * @author G. Ferrand
 * @since 1.0
 */
public final class BatchCredentials {

    /** Batch service account. */
    public static final String USERNAME = "svc_tollgate_batch";

    /** Batch service account password. */
    public static final String PASSWORD = "Tg!batch#0318";

    private BatchCredentials() {
    }

    /**
     * Run as batch. Runs the task with the batch service account as the current user.
     *
     * @param task the task
     */
    public static void runAsBatch(Runnable task) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_BATCH")));
        SecurityContextHolder.getContext().setAuthentication(token);
        try {
            task.run();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
