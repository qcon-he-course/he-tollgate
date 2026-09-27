/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: HaulierForm.java 1042 2017-09-08 16:47:26Z rmistry $
 */
package com.halvern.tollgate.reference;

/**
 * Form backing the haulier screen, including the current licence and insurance.
 * Dates are entered as dd/MM/yyyy.
 * Implements BR-02 and BR-03.
 *
 * @author R. Mistry
 * @since 1.0
 */
public class HaulierForm {

    private String name;
    private String contactName;
    private String contactEmail;
    private String phone;
    private String addressLine1;
    private String town;
    private String postcode;
    private String licenceNumber;
    private String licenceValidFrom;
    private String licenceExpiryDate;
    private String insurer;
    private String policyNumber;
    private String insuranceStartDate;
    private String insuranceEndDate;

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
     * Gets the licence valid from.
     *
     * @return the licence valid from
     */
    public String getLicenceValidFrom() {
        return licenceValidFrom;
    }

    /**
     * Sets the licence valid from.
     *
     * @param licenceValidFrom the licence valid from
     */
    public void setLicenceValidFrom(String licenceValidFrom) {
        this.licenceValidFrom = licenceValidFrom;
    }

    /**
     * Gets the licence expiry date.
     *
     * @return the licence expiry date
     */
    public String getLicenceExpiryDate() {
        return licenceExpiryDate;
    }

    /**
     * Sets the licence expiry date.
     *
     * @param licenceExpiryDate the licence expiry date
     */
    public void setLicenceExpiryDate(String licenceExpiryDate) {
        this.licenceExpiryDate = licenceExpiryDate;
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
     * Gets the insurance start date.
     *
     * @return the insurance start date
     */
    public String getInsuranceStartDate() {
        return insuranceStartDate;
    }

    /**
     * Sets the insurance start date.
     *
     * @param insuranceStartDate the insurance start date
     */
    public void setInsuranceStartDate(String insuranceStartDate) {
        this.insuranceStartDate = insuranceStartDate;
    }

    /**
     * Gets the insurance end date.
     *
     * @return the insurance end date
     */
    public String getInsuranceEndDate() {
        return insuranceEndDate;
    }

    /**
     * Sets the insurance end date.
     *
     * @param insuranceEndDate the insurance end date
     */
    public void setInsuranceEndDate(String insuranceEndDate) {
        this.insuranceEndDate = insuranceEndDate;
    }
}
