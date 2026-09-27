/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: Question.java 1124 2017-11-07 10:12:38Z koduya $
 */
package com.halvern.tollgate.questionnaire;

/**
 * A question in the catalogue.
 * Implements BR-21.
 *
 * @author K. Oduya
 * @since 1.0
 */
public class Question {

    private Long id;
    private Long questionSetId;
    private int seq;
    private String questionText;
    private boolean mandatory;

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
     * Gets the question set id.
     *
     * @return the question set id
     */
    public Long getQuestionSetId() {
        return questionSetId;
    }

    /**
     * Sets the question set id.
     *
     * @param questionSetId the question set id
     */
    public void setQuestionSetId(Long questionSetId) {
        this.questionSetId = questionSetId;
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
}
