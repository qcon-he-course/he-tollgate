/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: AnswerForm.java 1133 2017-11-13 11:57:09Z koduya $
 */
package com.halvern.tollgate.questionnaire;

import java.util.List;

/**
 * Form backing the answer entry screen. Answers are posted in the same order as the
 * question ids.
 * Implements BR-23.
 *
 * @author K. Oduya
 * @since 1.0
 */
public class AnswerForm {

    private List<Long> caseQuestionIds;
    private List<String> answers;

    /**
     * Gets the case question ids.
     *
     * @return the case question ids
     */
    public List<Long> getCaseQuestionIds() {
        return caseQuestionIds;
    }

    /**
     * Sets the case question ids.
     *
     * @param caseQuestionIds the case question ids
     */
    public void setCaseQuestionIds(List<Long> caseQuestionIds) {
        this.caseQuestionIds = caseQuestionIds;
    }

    /**
     * Gets the answers.
     *
     * @return the answers
     */
    public List<String> getAnswers() {
        return answers;
    }

    /**
     * Sets the answers.
     *
     * @param answers the answers
     */
    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }
}
