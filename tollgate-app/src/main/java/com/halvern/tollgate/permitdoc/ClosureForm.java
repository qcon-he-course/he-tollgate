/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: ClosureForm.java 1183 2018-02-06 16:12:30Z rmistry $
 */
package com.halvern.tollgate.permitdoc;

/**
 * Form backing the record closure screen. Dates are entered as dd/MM/yyyy.
 * Implements BR-33.
 *
 * @author R. Mistry
 * @since 1.0
 */
public class ClosureForm {

    private String structureId;
    private String startDate;
    private String endDate;
    private String reason;

    /**
     * Gets the structure id.
     *
     * @return the structure id
     */
    public String getStructureId() {
        return structureId;
    }

    /**
     * Sets the structure id.
     *
     * @param structureId the structure id
     */
    public void setStructureId(String structureId) {
        this.structureId = structureId;
    }

    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date.
     *
     * @param startDate the start date
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the end date.
     *
     * @return the end date
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date.
     *
     * @param endDate the end date
     */
    public void setEndDate(String endDate) {
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
}
