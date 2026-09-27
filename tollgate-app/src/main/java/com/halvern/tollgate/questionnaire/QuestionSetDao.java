/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: QuestionSetDao.java 1126 2017-11-08 09:33:47Z koduya $
 */
package com.halvern.tollgate.questionnaire;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Data access for the question catalogue.
 * Implements BR-21.
 *
 * @author K. Oduya
 * @since 1.0
 */
@Repository
public class QuestionSetDao {

    private static final Logger log = LoggerFactory.getLogger(QuestionSetDao.class);

    private static final String SELECT_SET =
            "SELECT qs.id, qs.load_type_id, lt.code AS load_type_code, qs.version, qs.active "
            + "FROM question_set qs "
            + "JOIN load_type lt ON lt.id = qs.load_type_id ";

    private static final String FIND_ALL = SELECT_SET + "ORDER BY lt.code, qs.version";

    private static final String FIND_BY_ID = SELECT_SET + "WHERE qs.id = ?";

    private static final String FIND_ACTIVE_FOR_LOAD_TYPE =
            SELECT_SET
            + "WHERE qs.load_type_id = ? AND qs.active = TRUE "
            + "ORDER BY qs.version DESC "
            + "LIMIT 1";

    private static final String FIND_QUESTIONS =
            "SELECT id, question_set_id, seq, question_text, mandatory "
            + "FROM question "
            + "WHERE question_set_id = ? "
            + "ORDER BY seq";

    private static final String UPDATE_QUESTION =
            "UPDATE question SET question_text = ?, mandatory = ? WHERE id = ? AND question_set_id = ?";

    private static final String INSERT_QUESTION =
            "INSERT INTO question (question_set_id, seq, question_text, mandatory) "
            + "SELECT ?, COALESCE(MAX(seq), 0) + 1, ?, ? FROM question WHERE question_set_id = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<QuestionSet> SET_MAPPER = new RowMapper<QuestionSet>() {
        public QuestionSet mapRow(ResultSet rs, int rowNum) throws SQLException {
            QuestionSet qs = new QuestionSet();
            qs.setId(rs.getLong("id"));
            qs.setLoadTypeId(rs.getLong("load_type_id"));
            qs.setLoadTypeCode(rs.getString("load_type_code"));
            qs.setVersion(rs.getInt("version"));
            qs.setActive(rs.getBoolean("active"));
            return qs;
        }
    };

    private static final RowMapper<Question> QUESTION_MAPPER = new RowMapper<Question>() {
        public Question mapRow(ResultSet rs, int rowNum) throws SQLException {
            Question q = new Question();
            q.setId(rs.getLong("id"));
            q.setQuestionSetId(rs.getLong("question_set_id"));
            q.setSeq(rs.getInt("seq"));
            q.setQuestionText(rs.getString("question_text"));
            q.setMandatory(rs.getBoolean("mandatory"));
            return q;
        }
    };

    /**
     * Find all. Questions are loaded.
     *
     * @return all question sets
     */
    public List<QuestionSet> findAll() {
        List<QuestionSet> sets = jdbcTemplate.query(FIND_ALL, SET_MAPPER);
        for (QuestionSet qs : sets) {
            qs.setQuestions(findQuestions(qs.getId().longValue()));
        }
        return sets;
    }

    /**
     * Find by id. Questions are loaded.
     *
     * @param id the id
     * @return the question set, or null if not found
     */
    public QuestionSet findById(long id) {
        List<QuestionSet> list = jdbcTemplate.query(FIND_BY_ID, SET_MAPPER, id);
        if (list.isEmpty()) {
            return null;
        }
        QuestionSet qs = list.get(0);
        qs.setQuestions(findQuestions(id));
        return qs;
    }

    /**
     * Find active for load type. Questions are loaded.
     *
     * @param loadTypeId the load type id
     * @return the active question set, or null if the load type has none
     */
    public QuestionSet findActiveForLoadType(long loadTypeId) {
        List<QuestionSet> list = jdbcTemplate.query(FIND_ACTIVE_FOR_LOAD_TYPE, SET_MAPPER, loadTypeId);
        if (list.isEmpty()) {
            return null;
        }
        QuestionSet qs = list.get(0);
        qs.setQuestions(findQuestions(qs.getId().longValue()));
        return qs;
    }

    /**
     * Find questions.
     *
     * @param questionSetId the question set id
     * @return the questions in order
     */
    public List<Question> findQuestions(long questionSetId) {
        return jdbcTemplate.query(FIND_QUESTIONS, QUESTION_MAPPER, questionSetId);
    }

    /**
     * Update question.
     *
     * @param question the question
     */
    public void updateQuestion(Question question) {
        jdbcTemplate.update(UPDATE_QUESTION, question.getQuestionText(), question.isMandatory(), question.getId(),
                question.getQuestionSetId());
        log.info("Updated question " + question.getId());
    }

    /**
     * Add question. The question is added at the end of the set.
     *
     * @param questionSetId the question set id
     * @param text the question text
     * @param mandatory whether an answer is required
     */
    public void addQuestion(long questionSetId, String text, boolean mandatory) {
        jdbcTemplate.update(INSERT_QUESTION, questionSetId, text, mandatory, questionSetId);
        log.info("Added question to set " + questionSetId);
    }
}
