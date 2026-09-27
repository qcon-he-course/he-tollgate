/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: CaseQuestion.java 1129 2017-11-09 15:40:21Z koduya $
 */
package com.halvern.tollgate.questionnaire;

import java.util.Date;

/**
 * A question copied to a lodged application, with the haulier's answer and the
 * officer's review.
 * Implements BR-22 and BR-23.
 *
 * @author K. Oduya
 * @since 1.0
 */
public class CaseQuestion {

    /** Not yet reviewed. */
    public static final String PENDING = "PENDING";

    /** Accepted by the officer. */
    public static final String ACCEPTED = "ACCEPTED";

    /** Returned to the haulier with a note. */
    public static final String RETURNED = "RETURNED";

    private Long id;
    private Long applicationId;
    private int seq;
    private String questionText;
    private boolean mandatory;
    private String answerText;
    private Date answeredAt;
    private String reviewStatus;
    private String reviewNote;
    private String reviewedBy;
    private Date reviewedAt;

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
     * Gets the seq.
     *
     * @return the seq
     */
    public int getSeq() {
        return seq;
    }

    /**
     * Sets the seq.
     *
     * @param seq the seq
     */
    public void setSeq(int seq) {
        this.seq = seq;
    }

    /**
     * Gets the question text.
     *
     * @return the question text
     */
    public String getQuestionText() {
        return questionText;
    }

    /**
     * Sets the question text.
     *
     * @param questionText the question text
     */
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    /**
     * Gets the mandatory.
     *
     * @return the mandatory
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Sets the mandatory.
     *
     * @param mandatory the mandatory
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * Gets the answer text.
     *
     * @return the answer text
     */
    public String getAnswerText() {
        return answerText;
    }

    /**
     * Sets the answer text.
     *
     * @param answerText the answer text
     */
    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    /**
     * Gets the answered at.
     *
     * @return the answered at
     */
    public Date getAnsweredAt() {
        return answeredAt;
    }

    /**
     * Sets the answered at.
     *
     * @param answeredAt the answered at
     */
    public void setAnsweredAt(Date answeredAt) {
        this.answeredAt = answeredAt;
    }

    /**
     * Gets the review status.
     *
     * @return the review status
     */
    public String getReviewStatus() {
        return reviewStatus;
    }

    /**
     * Sets the review status.
     *
     * @param reviewStatus the review status
     */
    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    /**
     * Gets the review note.
     *
     * @return the review note
     */
    public String getReviewNote() {
        return reviewNote;
    }

    /**
     * Sets the review note.
     *
     * @param reviewNote the review note
     */
    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    /**
     * Gets the reviewed by.
     *
     * @return the reviewed by
     */
    public String getReviewedBy() {
        return reviewedBy;
    }

    /**
     * Sets the reviewed by.
     *
     * @param reviewedBy the reviewed by
     */
    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    /**
     * Gets the reviewed at.
     *
     * @return the reviewed at
     */
    public Date getReviewedAt() {
        return reviewedAt;
    }

    /**
     * Sets the reviewed at.
     *
     * @param reviewedAt the reviewed at
     */
    public void setReviewedAt(Date reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    /**
     * Is answered.
     *
     * @return true if an answer has been entered
     */
    public boolean isAnswered() {
        return answerText != null && answerText.trim().length() > 0;
    }

    /**
     * Is editable.
     *
     * @return true if the answer can still be changed
     */
    public boolean isEditable() {
        return !ACCEPTED.equals(reviewStatus);
    }
}
