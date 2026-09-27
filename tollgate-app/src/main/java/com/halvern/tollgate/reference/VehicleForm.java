/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: VehicleForm.java 1043 2017-09-08 17:05:12Z rmistry $
 */
package com.halvern.tollgate.reference;

/**
 * Form backing the vehicle screen.
 * Implements BR-02.
 *
 * @author R. Mistry
 * @since 1.0
 */
public class VehicleForm {

    private String registration;
    private String category;

    /**
     * Gets the registration.
     *
     * @return the registration
     */
    public String getRegistration() {
        return registration;
    }

    /**
     * Sets the registration.
     *
     * @param registration the registration
     */
    public void setRegistration(String registration) {
        this.registration = registration;
    }

    /**
     * Gets the category.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category.
     *
     * @param category the category
     */
    public void setCategory(String category) {
        this.category = category;
    }
}
