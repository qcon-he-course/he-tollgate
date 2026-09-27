/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: Permit.java 1171 2018-01-23 10:04:52Z gferrand $
 */
package com.halvern.tollgate.permitdoc;

import java.util.Date;

/**
 * A movement permit issued for an approved application.
 * Implements BR-30.
 *
 * @author G. Ferrand
 * @since 1.0
 */
public class Permit {

    /** Permit in force. */
    public static final String ACTIVE = "ACTIVE";

    /** Suspended by a structure closure (BR-33). */
    public static final String SUSPENDED = "SUSPENDED";

    /** Revoked by Halvern. */
    public static final String REVOKED = "REVOKED";

    /** The movement window has passed (BR-35). */
    public static final String EXPIRED = "EXPIRED";

    private Long id;
    private String reference;
    private Long applicationId;
    private String applicationReference;
    private Long haulierId;
    private String haulierName;
    private String routeCode;
    private Date firstMoveDate;
    private Date lastMoveDate;
    private String status;
    private String issuedBy;
    private Date issuedAt;
    private String statusReason;
    private Date statusChangedAt;
    private Long suspendedByClosureId;

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
     * Gets the application reference.
     *
     * @return the application reference
     */
    public String getApplicationReference() {
        return applicationReference;
    }

    /**
     * Sets the application reference.
     *
     * @param applicationReference the application reference
     */
    public void setApplicationReference(String applicationReference) {
        this.applicationReference = applicationReference;
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
     * Gets the issued by.
     *
     * @return the issued by
     */
    public String getIssuedBy() {
        return issuedBy;
    }

    /**
     * Sets the issued by.
     *
     * @param issuedBy the issued by
     */
    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    /**
     * Gets the issued at.
     *
     * @return the issued at
     */
    public Date getIssuedAt() {
        return issuedAt;
    }

    /**
     * Sets the issued at.
     *
     * @param issuedAt the issued at
     */
    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    /**
     * Gets the status reason.
     *
     * @return the status reason
     */
    public String getStatusReason() {
        return statusReason;
    }

    /**
     * Sets the status reason.
     *
     * @param statusReason the status reason
     */
    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    /**
     * Gets the status changed at.
     *
     * @return the status changed at
     */
    public Date getStatusChangedAt() {
        return statusChangedAt;
    }

    /**
     * Sets the status changed at.
     *
     * @param statusChangedAt the status changed at
     */
    public void setStatusChangedAt(Date statusChangedAt) {
        this.statusChangedAt = statusChangedAt;
    }

    /**
     * Gets the suspended by closure id.
     *
     * @return the suspended by closure id
     */
    public Long getSuspendedByClosureId() {
        return suspendedByClosureId;
    }

    /**
     * Sets the suspended by closure id.
     *
     * @param suspendedByClosureId the suspended by closure id
     */
    public void setSuspendedByClosureId(Long suspendedByClosureId) {
        this.suspendedByClosureId = suspendedByClosureId;
    }

    // TG-181 permit versions: each version has its own movement window
    private int version;
    private Date windowStart;
    private Date windowEnd;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(Date windowStart) {
        this.windowStart = windowStart;
    }

    public Date getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(Date windowEnd) {
        this.windowEnd = windowEnd;
    }
}
