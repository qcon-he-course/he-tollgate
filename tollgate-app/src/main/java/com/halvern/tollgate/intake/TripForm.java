/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: TripForm.java 1086 2017-10-05 16:44:10Z gferrand $
 */
package com.halvern.tollgate.intake;

/**
 * One trip row on the application screen. Times are entered as dd/MM/yyyy HH:mm.
 * Implements BR-10.
 *
 * @author G. Ferrand
 * @since 1.0
 */
public class TripForm {

    private String departure;
    private String crossing;

    /**
     * Gets the departure.
     *
     * @return the departure
     */
    public String getDeparture() {
        return departure;
    }

    /**
     * Sets the departure.
     *
     * @param departure the departure
     */
    public void setDeparture(String departure) {
        this.departure = departure;
    }

    /**
     * Gets the crossing.
     *
     * @return the crossing
     */
    public String getCrossing() {
        return crossing;
    }

    /**
     * Sets the crossing.
     *
     * @param crossing the crossing
     */
    public void setCrossing(String crossing) {
        this.crossing = crossing;
    }
}
