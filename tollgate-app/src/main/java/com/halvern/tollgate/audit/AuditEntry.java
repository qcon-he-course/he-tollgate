/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: AuditEntry.java 1150 2017-11-27 10:06:31Z rmistry $
 */
package com.halvern.tollgate.audit;

import java.util.Date;

/**
 * An entry in the audit log: a change of state of an application or permit.
 * Implements BR-45.
 *
 * @author R. Mistry
 * @since 1.0
 */
public class AuditEntry {

    /** Entity type for applications. */
    public static final String APPLICATION = "APPLICATION";

    public static final String CASE = "CASE";

    /** Entity type for permits. */
    public static final String PERMIT = "PERMIT";

    /** Actor recorded for changes made by the system rather than a user. */
    public static final String SYSTEM = "SYSTEM";

    private Long id;
    private String reference;
    private String entityType;
    private String oldState;
    private String newState;
    private String actor;
    private Date createdAt;
    private String reason;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the reference.
     *
     * @return the reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * Sets the reference.
     *
     * @param reference the reference
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Gets the entity type.
     *
     * @return the entity type
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Sets the entity type.
     *
     * @param entityType the entity type
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    /**
     * Gets the old state.
     *
     * @return the old state
     */
    public String getOldState() {
        return oldState;
    }

    /**
     * Sets the old state.
     *
     * @param oldState the old state
     */
    public void setOldState(String oldState) {
        this.oldState = oldState;
    }

    /**
     * Gets the new state.
     *
     * @return the new state
     */
    public String getNewState() {
        return newState;
    }

    /**
     * Sets the new state.
     *
     * @param newState the new state
     */
    public void setNewState(String newState) {
        this.newState = newState;
    }

    /**
     * Gets the actor.
     *
     * @return the actor
     */
    public String getActor() {
        return actor;
    }

    /**
     * Sets the actor.
     *
     * @param actor the actor
     */
    public void setActor(String actor) {
        this.actor = actor;
    }

    /**
     * Gets the created at.
     *
     * @return the created at
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     *
     * @param createdAt the created at
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the reason.
     *
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason.
     *
     * @param reason the reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
}
