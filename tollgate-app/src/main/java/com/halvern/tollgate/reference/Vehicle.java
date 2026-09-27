/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: Vehicle.java 960 2017-07-04 11:02:47Z rmistry $
 */
package com.halvern.tollgate.reference;

/**
 * A vehicle registered to a haulier.
 * Implements BR-02.
 *
 * @author R. Mistry
 * @since 1.0
 */
public class Vehicle {

    private Long id;
    private Long haulierId;
    private String registration;
    private String category;

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
