/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: OperatorLicence.java 957 2017-07-03 10:48:22Z rmistry $
 */
package com.halvern.tollgate.reference;

import java.util.Date;

/**
 * A haulier's operator licence.
 * Implements BR-02.
 *
 * @author R. Mistry
 * @since 1.0
 */
public class OperatorLicence {

    private Long id;
    private Long haulierId;
    private String licenceNumber;
    private Date validFrom;
    private Date expiryDate;

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
     * Gets the licence number.
     *
     * @return the licence number
     */
    public String getLicenceNumber() {
        return licenceNumber;
    }

    /**
     * Sets the licence number.
     *
     * @param licenceNumber the licence number
     */
    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    /**
     * Gets the valid from.
     *
     * @return the valid from
     */
    public Date getValidFrom() {
        return validFrom;
    }

    /**
     * Sets the valid from.
     *
     * @param validFrom the valid from
     */
    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * Gets the expiry date.
     *
     * @return the expiry date
     */
    public Date getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the expiry date.
     *
     * @param expiryDate the expiry date
     */
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
