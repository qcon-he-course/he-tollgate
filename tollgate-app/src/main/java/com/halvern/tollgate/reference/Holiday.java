/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: Holiday.java 952 2017-06-27 16:21:10Z rmistry $
 */
package com.halvern.tollgate.reference;

import java.util.Date;

/**
 * A non-working day in the Halvern holiday calendar.
 * Implements BR-14.
 *
 * @author R. Mistry
 * @since 1.0
 */
public class Holiday {

    private Long id;
    private Date holidayDate;
    private String description;

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
     * Gets the holiday date.
     *
     * @return the holiday date
     */
    public Date getHolidayDate() {
        return holidayDate;
    }

    /**
     * Sets the holiday date.
     *
     * @param holidayDate the holiday date
     */
    public void setHolidayDate(Date holidayDate) {
        this.holidayDate = holidayDate;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
