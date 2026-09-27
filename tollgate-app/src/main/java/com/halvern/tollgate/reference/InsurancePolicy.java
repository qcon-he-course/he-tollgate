/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: InsurancePolicy.java 958 2017-07-03 15:30:04Z rmistry $
 */
package com.halvern.tollgate.reference;

import java.util.Date;

/**
 * A haulier's insurance policy for abnormal load movements.
 * Implements BR-03.
 *
 * @author R. Mistry
 * @since 1.0
 */
public class InsurancePolicy {

    private Long id;
    private Long haulierId;
    private String insurer;
    private String policyNumber;
    private Date startDate;
    private Date endDate;

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
     * Gets the haulier id.
     *
     * @return the haulier id
     */
    public Long getHaulierId() {
        return haulierId;
    }

    /**
     * Sets the haulier id.
     *
     * @param haulierId the haulier id
     */
    public void setHaulierId(Long haulierId) {
        this.haulierId = haulierId;
    }

    /**
     * Gets the insurer.
     *
     * @return the insurer
     */
    public String getInsurer() {
        return insurer;
    }

    /**
     * Sets the insurer.
     *
     * @param insurer the insurer
     */
    public void setInsurer(String insurer) {
        this.insurer = insurer;
    }

    /**
     * Gets the policy number.
     *
     * @return the policy number
     */
    public String getPolicyNumber() {
        return policyNumber;
    }

    /**
     * Sets the policy number.
     *
     * @param policyNumber the policy number
     */
    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
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
}
