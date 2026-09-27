/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: StructureClosure.java 1182 2018-02-06 14:40:18Z rmistry $
 */
package com.halvern.tollgate.permitdoc;

import java.util.Date;

/**
 * A closure of a structure, during which permits crossing it are suspended.
 * Implements BR-33.
 *
 * @author R. Mistry
 * @since 1.0
 */
public class StructureClosure {

    /** Closure in force. */
    public static final String ACTIVE = "ACTIVE";

    /** Closure ended; suspended permits have been reinstated. */
    public static final String ENDED = "ENDED";

    private Long id;
    private Long structureId;
    private String structureCode;
    private String structureName;
    private Date startDate;
    private Date endDate;
    private String reason;
    private String status;
    private String recordedBy;
    private Date recordedAt;
    private String endedBy;
    private Date endedAt;

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
     * Gets the structure id.
     *
     * @return the structure id
     */
    public Long getStructureId() {
        return structureId;
    }

    /**
     * Sets the structure id.
     *
     * @param structureId the structure id
     */
    public void setStructureId(Long structureId) {
        this.structureId = structureId;
    }

    /**
     * Gets the structure code.
     *
     * @return the structure code
     */
    public String getStructureCode() {
        return structureCode;
    }

    /**
     * Sets the structure code.
     *
     * @param structureCode the structure code
     */
    public void setStructureCode(String structureCode) {
        this.structureCode = structureCode;
    }

    /**
     * Gets the structure name.
     *
     * @return the structure name
     */
    public String getStructureName() {
        return structureName;
    }

    /**
     * Sets the structure name.
     *
     * @param structureName the structure name
     */
    public void setStructureName(String structureName) {
        this.structureName = structureName;
    }

    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date.
     *
     * @param startDate the start date
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the end date.
     *
     * @return the end date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date.
     *
     * @param endDate the end date
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the recorded by.
     *
     * @return the recorded by
     */
    public String getRecordedBy() {
        return recordedBy;
    }

    /**
     * Sets the recorded by.
     *
     * @param recordedBy the recorded by
     */
    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    /**
     * Gets the recorded at.
     *
     * @return the recorded at
     */
    public Date getRecordedAt() {
        return recordedAt;
    }

    /**
     * Sets the recorded at.
     *
     * @param recordedAt the recorded at
     */
    public void setRecordedAt(Date recordedAt) {
        this.recordedAt = recordedAt;
    }

    /**
     * Gets the ended by.
     *
     * @return the ended by
     */
    public String getEndedBy() {
        return endedBy;
    }

    /**
     * Sets the ended by.
     *
     * @param endedBy the ended by
     */
    public void setEndedBy(String endedBy) {
        this.endedBy = endedBy;
    }

    /**
     * Gets the ended at.
     *
     * @return the ended at
     */
    public Date getEndedAt() {
        return endedAt;
    }

    /**
     * Sets the ended at.
     *
     * @param endedAt the ended at
     */
    public void setEndedAt(Date endedAt) {
        this.endedAt = endedAt;
    }

    /**
     * Is active.
     *
     * @return true if the closure has not been ended
     */
    public boolean isActive() {
        return ACTIVE.equals(status);
    }
}
