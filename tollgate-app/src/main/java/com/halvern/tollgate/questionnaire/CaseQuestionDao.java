/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: CaseQuestionDao.java 1159 2017-12-01 14:37:50Z rmistry $
 */
package com.halvern.tollgate.questionnaire;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.halvern.tollgate.platform.DbTime;

/**
 * Data access for the questions copied to each lodged application, with their answers.
 * Implements BR-22.
 *
 * @author K. Oduya
 * @since 1.0
 */
@Repository
public class CaseQuestionDao {

    private static final Logger log = LoggerFactory.getLogger(CaseQuestionDao.class);

    // BR-22: the application keeps the questions as they were when it was lodged
    private static final String COPY_QUESTIONS =
            "INSERT INTO case_question (application_id, seq, question_text, mandatory) "
            + "SELECT ?, seq, question_text, mandatory "
            + "FROM question "
            + "WHERE question_set_id = ? "
            + "ORDER BY seq";

    private static final String CREATE_ANSWERS =
            "INSERT INTO case_answer (case_question_id, review_status) "
            + "SELECT id, 'PENDING' FROM case_question WHERE application_id = ?";

    private static final String SELECT_CASE_QUESTION =
            "SELECT cq.id, cq.application_id, cq.seq, cq.question_text, cq.mandatory, "
            + "       ca.answer_text, ca.answered_at, ca.review_status, ca.review_note, ca.reviewed_by, ca.reviewed_at "
            + "FROM case_question cq "
            + "JOIN case_answer ca ON ca.case_question_id = cq.id ";

    private static final String FIND_BY_APPLICATION =
            SELECT_CASE_QUESTION + "WHERE cq.application_id = ? ORDER BY cq.seq";

    private static final String FIND_BY_ID = SELECT_CASE_QUESTION + "WHERE cq.id = ?";

    private static final String FIND_APPLICATION =
            "SELECT a.id, a.reference, a.status, h.name AS haulier_name, lt.code AS load_type_code "
            + "FROM movement_application a "
            + "JOIN haulier h ON h.id = a.haulier_id "
            + "LEFT JOIN load_type lt ON lt.id = a.load_type_id "
            + "WHERE a.reference = ?";

    private static final String FIND_APPLICATION_BY_ID =
            "SELECT a.id, a.reference, a.status, h.name AS haulier_name, h.contact_email "
            + "FROM movement_application a "
            + "JOIN haulier h ON h.id = a.haulier_id "
            + "WHERE a.id = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<CaseQuestion> MAPPER = new RowMapper<CaseQuestion>() {
        public CaseQuestion mapRow(ResultSet rs, int rowNum) throws SQLException {
            CaseQuestion q = new CaseQuestion();
            q.setId(rs.getLong("id"));
            q.setApplicationId(rs.getLong("application_id"));
            q.setSeq(rs.getInt("seq"));
            q.setQuestionText(rs.getString("question_text"));
            q.setMandatory(rs.getBoolean("mandatory"));
            q.setAnswerText(rs.getString("answer_text"));
            q.setAnsweredAt(rs.getTimestamp("answered_at", DbTime.utc()));
            q.setReviewStatus(rs.getString("review_status"));
            q.setReviewNote(rs.getString("review_note"));
            q.setReviewedBy(rs.getString("reviewed_by"));
            q.setReviewedAt(rs.getTimestamp("reviewed_at", DbTime.utc()));
            return q;
        }
    };

    /**
     * Copy questions from the catalogue to an application, each with an empty answer.
     *
     * @param applicationId the application id
     * @param questionSetId the question set to copy
     * @return the number of questions copied
     */
    public int copyQuestions(long applicationId, long questionSetId) {
        int copied = jdbcTemplate.update(COPY_QUESTIONS, applicationId, questionSetId);
        jdbcTemplate.update(CREATE_ANSWERS, applicationId);
        log.info("Copied " + copied + " questions from set " + questionSetId + " to application " + applicationId);
        return copied;
    }

    /**
     * Find by application.
     *
     * @param applicationId the application id
     * @return the application's questions and answers in order
     */
    public List<CaseQuestion> findByApplication(long applicationId) {
        return jdbcTemplate.query(FIND_BY_APPLICATION, MAPPER, applicationId);
    }

    /**
     * Find by id.
     *
     * @param id the case question id
     * @return the question and answer, or null if not found
     */
    public CaseQuestion findById(long id) {
        List<CaseQuestion> list = jdbcTemplate.query(FIND_BY_ID, MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Find application. Returns the few application fields the questionnaire screens show.
     *
     * @param reference the application reference
     * @return id, reference, status, haulier_name and load_type_code, or null if not found
     */
    public Map<String, Object> findApplication(String reference) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(FIND_APPLICATION, reference);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Find application by id.
     *
     * @param applicationId the application id
     * @return id, reference, status, haulier_name and contact_email, or null if not found
     */
    public Map<String, Object> findApplicationById(long applicationId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(FIND_APPLICATION_BY_ID, applicationId);
        return rows.isEmpty() ? null : rows.get(0);
    }
}
