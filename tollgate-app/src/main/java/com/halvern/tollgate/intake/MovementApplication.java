/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: MovementApplication.java 1082 2017-10-04 10:31:26Z gferrand $
 */
package com.halvern.tollgate.intake;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * An application to move an abnormal load across the Halvern network.
 * Before lodgement the application is a draft.
 * Implements BR-09.
 *
 * @author G. Ferrand
 * @since 1.0
 */
public class MovementApplication {

    /** Draft, not yet lodged. */
    public static final String STATUS_DRAFT = "DRAFT";

    /** Lodged with Halvern. */
    public static final String STATUS_SUBMITTED = "SUBMITTED";

    /** Approved; a permit has been issued. */
    public static final String STATUS_APPROVED = "APPROVED";

    /** Rejected by the permit office. */
    public static final String STATUS_REJECTED = "REJECTED";

    private Long id;
    private String reference;
    private Long haulierId;
    private String haulierName;
    private Long vehicleId;
    private String vehicleRegistration;
    private String vehicleCategory;
    private Long loadTypeId;
    private String loadTypeCode;
    private String loadDescription;
    private Integer grossMassKg;
    private BigDecimal widthM;
    private BigDecimal lengthM;
    private BigDecimal heightM;
    private boolean hazardous;
    private String unNumber;
    private Long routeId;
    private String routeCode;
    private Date firstMoveDate;
    private Date lastMoveDate;
    private String status;
    private String createdBy;
    private Date createdAt;
    private String lodgedBy;
    private Date lodgedAt;
    private List<ApplicationTrip> trips;
    private List<Attachment> attachments;

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
     * Gets the reference.
     *
     * @return the reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * Sets the reference.
     *
     * @param reference the reference
     */
    public void setReference(String reference) {
        this.reference = reference;
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
     * Gets the haulier name.
     *
     * @return the haulier name
     */
    public String getHaulierName() {
        return haulierName;
    }

    /**
     * Sets the haulier name.
     *
     * @param haulierName the haulier name
     */
    public void setHaulierName(String haulierName) {
        this.haulierName = haulierName;
    }

    /**
     * Gets the vehicle id.
     *
     * @return the vehicle id
     */
    public Long getVehicleId() {
        return vehicleId;
    }

    /**
     * Sets the vehicle id.
     *
     * @param vehicleId the vehicle id
     */
    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
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
     * Gets the vehicle category.
     *
     * @return the vehicle category
     */
    public String getVehicleCategory() {
        return vehicleCategory;
    }

    /**
     * Sets the vehicle category.
     *
     * @param vehicleCategory the vehicle category
     */
    public void setVehicleCategory(String vehicleCategory) {
        this.vehicleCategory = vehicleCategory;
    }

    /**
     * Gets the load type id.
     *
     * @return the load type id
     */
    public Long getLoadTypeId() {
        return loadTypeId;
    }

    /**
     * Sets the load type id.
     *
     * @param loadTypeId the load type id
     */
    public void setLoadTypeId(Long loadTypeId) {
        this.loadTypeId = loadTypeId;
    }

    /**
     * Gets the load type code.
     *
     * @return the load type code
     */
    public String getLoadTypeCode() {
        return loadTypeCode;
    }

    /**
     * Sets the load type code.
     *
     * @param loadTypeCode the load type code
     */
    public void setLoadTypeCode(String loadTypeCode) {
        this.loadTypeCode = loadTypeCode;
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
    public Integer getGrossMassKg() {
        return grossMassKg;
    }

    /**
     * Sets the gross mass kg.
     *
     * @param grossMassKg the gross mass kg
     */
    public void setGrossMassKg(Integer grossMassKg) {
        this.grossMassKg = grossMassKg;
    }

    /**
     * Gets the width m.
     *
     * @return the width m
     */
    public BigDecimal getWidthM() {
        return widthM;
    }

    /**
     * Sets the width m.
     *
     * @param widthM the width m
     */
    public void setWidthM(BigDecimal widthM) {
        this.widthM = widthM;
    }

    /**
     * Gets the length m.
     *
     * @return the length m
     */
    public BigDecimal getLengthM() {
        return lengthM;
    }

    /**
     * Sets the length m.
     *
     * @param lengthM the length m
     */
    public void setLengthM(BigDecimal lengthM) {
        this.lengthM = lengthM;
    }

    /**
     * Gets the height m.
     *
     * @return the height m
     */
    public BigDecimal getHeightM() {
        return heightM;
    }

    /**
     * Sets the height m.
     *
     * @param heightM the height m
     */
    public void setHeightM(BigDecimal heightM) {
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
    public Long getRouteId() {
        return routeId;
    }

    /**
     * Sets the route id.
     *
     * @param routeId the route id
     */
    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    /**
     * Gets the route code.
     *
     * @return the route code
     */
    public String getRouteCode() {
        return routeCode;
    }

    /**
     * Sets the route code.
     *
     * @param routeCode the route code
     */
    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    /**
     * Gets the first move date.
     *
     * @return the first move date
     */
    public Date getFirstMoveDate() {
        return firstMoveDate;
    }

    /**
     * Sets the first move date.
     *
     * @param firstMoveDate the first move date
     */
    public void setFirstMoveDate(Date firstMoveDate) {
        this.firstMoveDate = firstMoveDate;
    }

    /**
     * Gets the last move date.
     *
     * @return the last move date
     */
    public Date getLastMoveDate() {
        return lastMoveDate;
    }

    /**
     * Sets the last move date.
     *
     * @param lastMoveDate the last move date
     */
    public void setLastMoveDate(Date lastMoveDate) {
        this.lastMoveDate = lastMoveDate;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the created by.
     *
     * @return the created by
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the created by.
     *
     * @param createdBy the created by
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    /**
     * Gets the lodged by.
     *
     * @return the lodged by
     */
    public String getLodgedBy() {
        return lodgedBy;
    }

    /**
     * Sets the lodged by.
     *
     * @param lodgedBy the lodged by
     */
    public void setLodgedBy(String lodgedBy) {
        this.lodgedBy = lodgedBy;
    }

    /**
     * Gets the lodged at.
     *
     * @return the lodged at
     */
    public Date getLodgedAt() {
        return lodgedAt;
    }

    /**
     * Sets the lodged at.
     *
     * @param lodgedAt the lodged at
     */
    public void setLodgedAt(Date lodgedAt) {
        this.lodgedAt = lodgedAt;
    }

    /**
     * Gets the trips.
     *
     * @return the trips
     */
    public List<ApplicationTrip> getTrips() {
        return trips;
    }

    /**
     * Sets the trips.
     *
     * @param trips the trips
     */
    public void setTrips(List<ApplicationTrip> trips) {
        this.trips = trips;
    }

    /**
     * Gets the attachments.
     *
     * @return the attachments
     */
    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * Sets the attachments.
     *
     * @param attachments the attachments
     */
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * Is draft.
     *
     * @return true if the application has not been lodged
     */
    public boolean isDraft() {
        return STATUS_DRAFT.equals(status);
    }

    /**
     * Gets the last departure.
     *
     * @return the latest planned departure of any trip, or null if there are no trips
     */
    public Date getLastDeparture() {
        Date last = null;
        if (trips != null) {
            for (int i = 0; i < trips.size(); i++) {
                Date d = trips.get(i).getDepartureAt();
                if (d != null && (last == null || d.after(last))) {
                    last = d;
                }
            }
        }
        return last;
    }
}
