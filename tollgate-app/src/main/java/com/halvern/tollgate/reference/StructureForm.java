/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: StructureForm.java 1036 2017-09-05 14:20:11Z gferrand $
 */
package com.halvern.tollgate.reference;

/**
 * Form backing the structure edit screen.
 * Implements BR-05.
 *
 * @author G. Ferrand
 * @since 1.0
 */
public class StructureForm {

    private String name;
    private String massRatingT;
    private String clearanceM;

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
     * Gets the mass rating t.
     *
     * @return the mass rating t
     */
    public String getMassRatingT() {
        return massRatingT;
    }

    /**
     * Sets the mass rating t.
     *
     * @param massRatingT the mass rating t
     */
    public void setMassRatingT(String massRatingT) {
        this.massRatingT = massRatingT;
    }

    /**
     * Gets the clearance m.
     *
     * @return the clearance m
     */
    public String getClearanceM() {
        return clearanceM;
    }

    /**
     * Sets the clearance m.
     *
     * @param clearanceM the clearance m
     */
    public void setClearanceM(String clearanceM) {
        this.clearanceM = clearanceM;
    }
}
