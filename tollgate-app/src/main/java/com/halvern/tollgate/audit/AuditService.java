/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: AuditService.java 1152 2017-11-27 15:18:40Z rmistry $
 */
package com.halvern.tollgate.audit;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Records changes of state of applications and permits in the audit log. Call it in the same
 * transaction as the change, so the entry and the change are saved together.
 * Implements BR-45 and BR-46.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditDao auditDao;

    /**
     * Record an application state change.
     *
     * @param reference the application reference
     * @param oldState the previous state, or null
     * @param newState the new state
     * @param actor the user, or SYSTEM
     * @param reason the reason, or null
     */
    public void recordApplication(String reference, String oldState, String newState, String actor, String reason) {
        record(reference, AuditEntry.APPLICATION, oldState, newState, actor, reason);
    }

    /**
     * Record a permit state change.
     *
     * @param reference the permit reference
     * @param oldState the previous state, or null for a new permit
     * @param newState the new state
     * @param actor the user, or SYSTEM
     * @param reason the reason, or null
     */
    public void recordPermit(String reference, String oldState, String newState, String actor, String reason) {
        record(reference, AuditEntry.PERMIT, oldState, newState, actor, reason);
    }

    public void recordCase(String reference, String oldState, String newState, String actor, String reason) {
        record(reference, AuditEntry.CASE, oldState, newState, actor, reason);
    }

    /**
     * Find history.
     *
     * @param applicationReference the application reference
     * @return the application's and its permit's history, oldest first
     */
    public List<AuditEntry> findHistory(String applicationReference) {
        return auditDao.findForApplication(applicationReference);
    }

    private void record(String reference, String entityType, String oldState, String newState, String actor,
            String reason) {
        AuditEntry entry = new AuditEntry();
        entry.setReference(reference);
        entry.setEntityType(entityType);
        entry.setOldState(oldState);
        entry.setNewState(newState);
        entry.setActor(actor);
        entry.setReason(reason);
        auditDao.insert(entry);
        log.info("Audit: " + entityType + " " + reference + " " + oldState + " -> " + newState + " by " + actor);
    }
}
