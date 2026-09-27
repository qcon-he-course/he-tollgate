/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: Haulier.java 957 2017-07-03 10:48:22Z rmistry $
 */
package com.halvern.tollgate.reference;

import java.util.Date;

/**
 * A haulage company holding an operator licence.
 * Implements BR-02.
 *
 * @author R. Mistry
 * @since 1.0
 */
public class Haulier {

    private Long id;
    private String name;
    private String contactName;
    private String contactEmail;
    private String phone;
    private String addressLine1;
    private String town;
    private String postcode;
    private Date createdAt;

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
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the contact name.
     *
     * @return the contact name
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * Sets the contact name.
     *
     * @param contactName the contact name
     */
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    /**
     * Gets the contact email.
     *
     * @return the contact email
     */
    public String getContactEmail() {
        return contactEmail;
    }

    /**
     * Sets the contact email.
     *
     * @param contactEmail the contact email
     */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    /**
     * Gets the phone.
     *
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone.
     *
     * @param phone the phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the address line1.
     *
     * @return the address line1
     */
    public String getAddressLine1() {
        return addressLine1;
    }

    /**
     * Sets the address line1.
     *
     * @param addressLine1 the address line1
     */
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    /**
     * Gets the town.
     *
     * @return the town
     */
    public String getTown() {
        return town;
    }

    /**
     * Sets the town.
     *
     * @param town the town
     */
    public void setTown(String town) {
        this.town = town;
    }

    /**
     * Gets the postcode.
     *
     * @return the postcode
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Sets the postcode.
     *
     * @param postcode the postcode
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
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
}
