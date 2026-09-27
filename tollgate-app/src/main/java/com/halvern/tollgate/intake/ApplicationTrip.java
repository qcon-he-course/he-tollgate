/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: ApplicationTrip.java 1083 2017-10-04 10:52:03Z gferrand $
 */
package com.halvern.tollgate.intake;

import java.util.Date;

/**
 * One trip of the move, with its planned departure and, where the route
 * crosses an estuary bridge, its planned crossing.
 * Implements BR-10.
 *
 * @author G. Ferrand
 * @since 1.0
 */
public class ApplicationTrip {

    private Long id;
    private Long applicationId;
    private int tripNo;
    private Date departureAt;
    private Date crossingAt;

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
     * Gets the application id.
     *
     * @return the application id
     */
    public Long getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application id.
     *
     * @param applicationId the application id
     */
    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Gets the trip no.
     *
     * @return the trip no
     */
    public int getTripNo() {
        return tripNo;
    }

    /**
     * Sets the trip no.
     *
     * @param tripNo the trip no
     */
    public void setTripNo(int tripNo) {
        this.tripNo = tripNo;
    }

    /**
     * Gets the departure at.
     *
     * @return the departure at
     */
    public Date getDepartureAt() {
        return departureAt;
    }

    /**
     * Sets the departure at.
     *
     * @param departureAt the departure at
     */
    public void setDepartureAt(Date departureAt) {
        this.departureAt = departureAt;
    }

    /**
     * Gets the crossing at.
     *
     * @return the crossing at
     */
    public Date getCrossingAt() {
        return crossingAt;
    }

    /**
     * Sets the crossing at.
     *
     * @param crossingAt the crossing at
     */
    public void setCrossingAt(Date crossingAt) {
        this.crossingAt = crossingAt;
    }
}
