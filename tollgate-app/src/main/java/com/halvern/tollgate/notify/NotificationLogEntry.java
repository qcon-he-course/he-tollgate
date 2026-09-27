/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: NotificationLogEntry.java 1153 2017-11-28 14:41:09Z rmistry $
 */
package com.halvern.tollgate.notify;

import java.util.Date;

/**
 * A record of an email sent, or that could not be sent, by Tollgate.
 * Implements BR-42.
 *
 * @author R. Mistry
 * @since 1.0
 */
public class NotificationLogEntry {

    /** The email was accepted by the mail server. */
    public static final String SENT = "SENT";

    /** The email could not be sent after all retries. */
    public static final String FAILED = "FAILED";

    private Long id;
    private String reference;
    private String recipient;
    private String template;
    private String subject;
    private String status;
    private String errorMessage;
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
     * Gets the recipient.
     *
     * @return the recipient
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Sets the recipient.
     *
     * @param recipient the recipient
     */
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /**
     * Gets the template.
     *
     * @return the template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the template.
     *
     * @param template the template
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * Gets the subject.
     *
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the subject.
     *
     * @param subject the subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
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
     * Gets the error message.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message.
     *
     * @param errorMessage the error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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
