/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: Structure.java 938 2017-06-19 11:42:08Z gferrand $
 */
package com.halvern.tollgate.reference;

import java.math.BigDecimal;

/**
 * A structure on the Halvern network: a carrying structure or an overbridge.
 * Implements BR-05.
 *
 * @author G. Ferrand
 * @since 1.0
 */
public class Structure {

    private Long id;
    private String code;
    private String name;
    private String structureType;
    private Integer massRatingT;
    private BigDecimal clearanceM;
    private boolean estuary;

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
     * Gets the code.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the code.
     *
     * @param code the code
     */
    public void setCode(String code) {
        this.code = code;
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
     * Gets the structure type.
     *
     * @return the structure type
     */
    public String getStructureType() {
        return structureType;
    }

    /**
     * Sets the structure type.
     *
     * @param structureType the structure type
     */
    public void setStructureType(String structureType) {
        this.structureType = structureType;
    }

    /**
     * Gets the mass rating t.
     *
     * @return the mass rating t
     */
    public Integer getMassRatingT() {
        return massRatingT;
    }

    /**
     * Sets the mass rating t.
     *
     * @param massRatingT the mass rating t
     */
    public void setMassRatingT(Integer massRatingT) {
        this.massRatingT = massRatingT;
    }

    /**
     * Gets the clearance m.
     *
     * @return the clearance m
     */
    public BigDecimal getClearanceM() {
        return clearanceM;
    }

    /**
     * Sets the clearance m.
     *
     * @param clearanceM the clearance m
     */
    public void setClearanceM(BigDecimal clearanceM) {
        this.clearanceM = clearanceM;
    }

    /**
     * Gets the estuary.
     *
     * @return the estuary
     */
    public boolean isEstuary() {
        return estuary;
    }

    /**
     * Sets the estuary.
     *
     * @param estuary the estuary
     */
    public void setEstuary(boolean estuary) {
        this.estuary = estuary;
    }

    /**
     * Is carrying.
     *
     * @return true if the road passes over this structure
     */
    public boolean isCarrying() {
        return TYPE_CARRYING.equals(structureType);
    }

    /** Structure type for a structure the road passes over. */
    public static final String TYPE_CARRYING = "CARRYING";

    /** Structure type for a structure passing over the road. */
    public static final String TYPE_OVERBRIDGE = "OVERBRIDGE";
}
