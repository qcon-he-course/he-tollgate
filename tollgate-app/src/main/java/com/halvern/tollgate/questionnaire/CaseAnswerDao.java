/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: CaseAnswerDao.java 1134 2017-11-13 14:05:52Z koduya $
 */
package com.halvern.tollgate.questionnaire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.halvern.tollgate.platform.DbTime;

/**
 * Data access for recording the haulier's answers.
 * Implements BR-23.
 *
 * @author K. Oduya
 * @since 1.0
 */
@Repository
public class CaseAnswerDao {

    private static final Logger log = LoggerFactory.getLogger(CaseAnswerDao.class);

    // A new or revised answer goes back for review. The last review note is kept so the officer
    // can see why it was returned.
    private static final String SAVE_ANSWER =
            "UPDATE case_answer "
            + "SET answer_text = ?, answered_at = " + DbTime.NOW_UTC + ", review_status = 'PENDING', "
            + "    reviewed_by = NULL, reviewed_at = NULL "
            + "WHERE case_question_id = ? AND review_status <> 'ACCEPTED'";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Save answer. Accepted answers are not changed.
     *
     * @param caseQuestionId the case question id
     * @param answerText the answer
     * @return true if the answer was saved
     */
    public boolean saveAnswer(long caseQuestionId, String answerText) {
        int rows = jdbcTemplate.update(SAVE_ANSWER, answerText, caseQuestionId);
        log.debug("Saved answer for case question " + caseQuestionId + " (" + rows + " row)");
        return rows == 1;
    }
}
