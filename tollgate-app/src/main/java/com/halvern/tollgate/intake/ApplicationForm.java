/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: ApplicationForm.java 1086 2017-10-05 16:44:10Z gferrand $
 */
package com.halvern.tollgate.intake;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

/**
 * Form backing the application screen used by permit officers to enter an
 * application on a haulier's behalf. Dates are entered as dd/MM/yyyy.
 * Implements BR-01 and BR-09.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@ValidMovementWindow
public class ApplicationForm {

    /** BR-10: at most ten trips per application. */
    public static final int MAX_TRIPS = 10;

    private String haulierId;
    private String vehicleRegistration;
    private String loadTypeId;
    @Size(max = 1000, message = "Description must be 1000 characters or fewer")
    private String loadDescription;
    private String grossMassKg;
    private String widthM;
    private String lengthM;
    private String heightM;
    private boolean hazardous;
    @UnNumber
    private String unNumber;
    private String routeId;
    private String firstMoveDate;
    private String lastMoveDate;
    private List<TripForm> trips;

    /**
     * Gets the haulier id.
     *
     * @return the haulier id
     */
    public String getHaulierId() {
        return haulierId;
    }

    /**
     * Sets the haulier id.
     *
     * @param haulierId the haulier id
     */
    public void setHaulierId(String haulierId) {
        this.haulierId = haulierId;
    }

    /**
     * Gets the vehicle registration.
     *
     * @return the vehicle registration
     */
    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    /**
     * Sets the vehicle registration.
     *
     * @param vehicleRegistration the vehicle registration
     */
    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    /**
     * Gets the load type id.
     *
     * @return the load type id
     */
    public String getLoadTypeId() {
        return loadTypeId;
    }

    /**
     * Sets the load type id.
     *
     * @param loadTypeId the load type id
     */
    public void setLoadTypeId(String loadTypeId) {
        this.loadTypeId = loadTypeId;
    }

    /**
     * Gets the load description.
     *
     * @return the load description
     */
    public String getLoadDescription() {
        return loadDescription;
    }

    /**
     * Sets the load description.
     *
     * @param loadDescription the load description
     */
    public void setLoadDescription(String loadDescription) {
        this.loadDescription = loadDescription;
    }

    /**
     * Gets the gross mass kg.
     *
     * @return the gross mass kg
     */
    public String getGrossMassKg() {
        return grossMassKg;
    }

    /**
     * Sets the gross mass kg.
     *
     * @param grossMassKg the gross mass kg
     */
    public void setGrossMassKg(String grossMassKg) {
        this.grossMassKg = grossMassKg;
    }

    /**
     * Gets the width m.
     *
     * @return the width m
     */
    public String getWidthM() {
        return widthM;
    }

    /**
     * Sets the width m.
     *
     * @param widthM the width m
     */
    public void setWidthM(String widthM) {
        this.widthM = widthM;
    }

    /**
     * Gets the length m.
     *
     * @return the length m
     */
    public String getLengthM() {
        return lengthM;
    }

    /**
     * Sets the length m.
     *
     * @param lengthM the length m
     */
    public void setLengthM(String lengthM) {
        this.lengthM = lengthM;
    }

    /**
     * Gets the height m.
     *
     * @return the height m
     */
    public String getHeightM() {
        return heightM;
    }

    /**
     * Sets the height m.
     *
     * @param heightM the height m
     */
    public void setHeightM(String heightM) {
        this.heightM = heightM;
    }

    /**
     * Gets the hazardous.
     *
     * @return the hazardous
     */
    public boolean isHazardous() {
        return hazardous;
    }

    /**
     * Sets the hazardous.
     *
     * @param hazardous the hazardous
     */
    public void setHazardous(boolean hazardous) {
        this.hazardous = hazardous;
    }

    /**
     * Gets the un number.
     *
     * @return the un number
     */
    public String getUnNumber() {
        return unNumber;
    }

    /**
     * Sets the un number.
     *
     * @param unNumber the un number
     */
    public void setUnNumber(String unNumber) {
        this.unNumber = unNumber;
    }

    /**
     * Gets the route id.
     *
     * @return the route id
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * Sets the route id.
     *
     * @param routeId the route id
     */
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    /**
     * Gets the first move date.
     *
     * @return the first move date
     */
    public String getFirstMoveDate() {
        return firstMoveDate;
    }

    /**
     * Sets the first move date.
     *
     * @param firstMoveDate the first move date
     */
    public void setFirstMoveDate(String firstMoveDate) {
        this.firstMoveDate = firstMoveDate;
    }

    /**
     * Gets the last move date.
     *
     * @return the last move date
     */
    public String getLastMoveDate() {
        return lastMoveDate;
    }

    /**
     * Sets the last move date.
     *
     * @param lastMoveDate the last move date
     */
    public void setLastMoveDate(String lastMoveDate) {
        this.lastMoveDate = lastMoveDate;
    }

    /**
     * Gets the trips.
     *
     * @return the trips
     */
    public List<TripForm> getTrips() {
        return trips;
    }

    /**
     * Sets the trips.
     *
     * @param trips the trips
     */
    public void setTrips(List<TripForm> trips) {
        this.trips = trips;
    }

    /**
     * Pad trips. Makes sure the form has a row for every possible trip.
     */
    public void padTrips() {
        if (trips == null) {
            trips = new ArrayList<TripForm>();
        }
        while (trips.size() < MAX_TRIPS) {
            trips.add(new TripForm());
        }
    }
}
