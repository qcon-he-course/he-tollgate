/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: HolidayForm.java 1038 2017-09-06 10:02:49Z rmistry $
 */
package com.halvern.tollgate.reference;

/**
 * Form backing the add holiday screen. Date is entered as dd/MM/yyyy.
 * Implements BR-14.
 *
 * @author R. Mistry
 * @since 1.0
 */
public class HolidayForm {

    private String holidayDate;
    private String description;

    /**
     * Gets the holiday date.
     *
     * @return the holiday date
     */
    public String getHolidayDate() {
        return holidayDate;
    }

    /**
     * Sets the holiday date.
     *
     * @param holidayDate the holiday date
     */
    public void setHolidayDate(String holidayDate) {
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
