/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: QuestionSet.java 1124 2017-11-07 10:12:38Z koduya $
 */
package com.halvern.tollgate.questionnaire;

import java.util.List;

/**
 * The set of questions asked for a load type.
 * Implements BR-21.
 *
 * @author K. Oduya
 * @since 1.0
 */
public class QuestionSet {

    private Long id;
    private Long loadTypeId;
    private String loadTypeCode;
    private int version;
    private boolean active;
    private List<Question> questions;

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
     * Gets the version.
     *
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version the version
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active.
     *
     * @param active the active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets the questions.
     *
     * @return the questions
     */
    public List<Question> getQuestions() {
        return questions;
    }

    /**
     * Sets the questions.
     *
     * @param questions the questions
     */
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
