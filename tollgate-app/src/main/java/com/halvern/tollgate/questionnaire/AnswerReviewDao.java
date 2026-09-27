/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: AnswerReviewDao.java 1137 2017-11-15 10:48:26Z koduya $
 */
package com.halvern.tollgate.questionnaire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.halvern.tollgate.platform.DbTime;

/**
 * Data access for the officer's review of answers: accept, or return to the haulier with a note.
 * Implements BR-24.
 *
 * @author K. Oduya
 * @since 1.0
 */
@Repository
public class AnswerReviewDao {

    private static final Logger log = LoggerFactory.getLogger(AnswerReviewDao.class);

    private static final String REVIEW =
            "UPDATE case_answer "
            + "SET review_status = ?, review_note = ?, reviewed_by = ?, reviewed_at = " + DbTime.NOW_UTC + " "
            + "WHERE case_question_id = ? "
            + "AND answer_text IS NOT NULL "
            + "AND review_status = 'PENDING'";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Accept an answer.
     *
     * @param caseQuestionId the case question id
     * @param username the reviewing officer
     * @param note an optional note
     * @return true if the answer was waiting for review and is now accepted
     */
    public boolean accept(long caseQuestionId, String username, String note) {
        int rows = jdbcTemplate.update(REVIEW, CaseQuestion.ACCEPTED, note, username, caseQuestionId);
        log.info("Answer " + caseQuestionId + " accepted by " + username);
        return rows == 1;
    }

    /**
     * Return an answer to the haulier.
     *
     * @param caseQuestionId the case question id
     * @param username the reviewing officer
     * @param note the reason, shown to the haulier
     * @return true if the answer was waiting for review and is now returned
     */
    public boolean returnAnswer(long caseQuestionId, String username, String note) {
        int rows = jdbcTemplate.update(REVIEW, CaseQuestion.RETURNED, note, username, caseQuestionId);
        log.info("Answer " + caseQuestionId + " returned by " + username + ": " + note);
        return rows == 1;
    }
}
