/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: Attachment.java 1091 2017-10-10 14:08:44Z koduya $
 */
package com.halvern.tollgate.intake;

import java.util.Date;

/**
 * A PDF document attached to an application.
 * Implements BR-13.
 *
 * @author K. Oduya
 * @since 1.0
 */
public class Attachment {

    /** Attachment types (BR-13). */
    public static final String[] TYPES = {"ESCORT_PLAN", "METHOD_STATEMENT", "OTHER"};

    private Long id;
    private Long applicationId;
    private String fileName;
    private String attachmentType;
    private long sizeBytes;
    private String storagePath;
    private String uploadedBy;
    private Date uploadedAt;

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
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name.
     *
     * @param fileName the file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the attachment type.
     *
     * @return the attachment type
     */
    public String getAttachmentType() {
        return attachmentType;
    }

    /**
     * Sets the attachment type.
     *
     * @param attachmentType the attachment type
     */
    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    /**
     * Gets the size bytes.
     *
     * @return the size bytes
     */
    public long getSizeBytes() {
        return sizeBytes;
    }

    /**
     * Sets the size bytes.
     *
     * @param sizeBytes the size bytes
     */
    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    /**
     * Gets the storage path.
     *
     * @return the storage path
     */
    public String getStoragePath() {
        return storagePath;
    }

    /**
     * Sets the storage path.
     *
     * @param storagePath the storage path
     */
    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * Gets the uploaded by.
     *
     * @return the uploaded by
     */
    public String getUploadedBy() {
        return uploadedBy;
    }

    /**
     * Sets the uploaded by.
     *
     * @param uploadedBy the uploaded by
     */
    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    /**
     * Gets the uploaded at.
     *
     * @return the uploaded at
     */
    public Date getUploadedAt() {
        return uploadedAt;
    }

    /**
     * Sets the uploaded at.
     *
     * @param uploadedAt the uploaded at
     */
    public void setUploadedAt(Date uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
