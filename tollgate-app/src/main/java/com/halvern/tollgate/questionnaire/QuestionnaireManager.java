/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: QuestionnaireManager.java 1160 2017-12-01 16:52:13Z rmistry $
 */
package com.halvern.tollgate.questionnaire;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.notify.MailNotifier;
import com.halvern.tollgate.platform.ErrorCodes;
import com.halvern.tollgate.platform.TollgateException;

/**
 * Manages the questionnaire for each application: copying the questions at lodgement, recording
 * answers and the officer's review.
 * Implements BR-21 to BR-24.
 *
 * @author K. Oduya
 * @since 1.0
 */
@Service
public class QuestionnaireManager {

    private static final Logger log = LoggerFactory.getLogger(QuestionnaireManager.class);

    /** Matches case_answer.answer_text. */
    public static final int MAX_ANSWER_LENGTH = 2000;

    @Autowired
    private QuestionSetDao questionSetDao;

    @Autowired
    private CaseQuestionDao caseQuestionDao;

    @Autowired
    private CaseAnswerDao caseAnswerDao;

    @Autowired
    private AnswerReviewDao answerReviewDao;

    @Autowired
    private MailNotifier mailNotifier;

    /**
     * Copy the current questions for the load type to a newly lodged application.
     *
     * @param applicationId the application id
     * @param loadTypeId the load type id
     * @return the number of questions copied
     */
    public int copyQuestions(long applicationId, long loadTypeId) {
        QuestionSet set = questionSetDao.findActiveForLoadType(loadTypeId);
        if (set == null) {
            log.warn("No active question set for load type " + loadTypeId + "; application " + applicationId
                    + " has no questionnaire");
            return 0;
        }
        return caseQuestionDao.copyQuestions(applicationId, set.getId().longValue());
    }

    /**
     * Find questions.
     *
     * @param applicationId the application id
     * @return the application's questions with answers
     */
    public List<CaseQuestion> findQuestions(long applicationId) {
        return caseQuestionDao.findByApplication(applicationId);
    }

    /**
     * Save answers. Blank answers are skipped; accepted answers cannot be changed.
     *
     * @param applicationId the application id
     * @param form the answers
     * @return the number of answers saved
     * @throws TollgateException if an answer does not belong to the application or cannot be changed
     */
    @Transactional(rollbackFor = TollgateException.class)
    public int saveAnswers(long applicationId, AnswerForm form) throws TollgateException {
        int saved = 0;
        List<Long> ids = form.getCaseQuestionIds();
        List<String> answers = form.getAnswers();
        if (ids == null || answers == null) {
            return 0;
        }
        for (int i = 0; i < ids.size() && i < answers.size(); i++) {
            String answer = StringUtils.trimToNull(answers.get(i));
            if (answer == null) {
                continue;
            }
            CaseQuestion question = findForApplication(applicationId, ids.get(i).longValue());
            if (!question.isEditable()) {
                continue;
            }
            if (StringUtils.equals(answer, question.getAnswerText())) {
                continue;
            }
            if (answer.length() > MAX_ANSWER_LENGTH) {
                throw new TollgateException(ErrorCodes.ANSWER_TOO_LONG,
                        "Answer " + question.getSeq() + " is longer than " + MAX_ANSWER_LENGTH + " characters");
            }
            if (!caseAnswerDao.saveAnswer(question.getId().longValue(), answer)) {
                throw new TollgateException(ErrorCodes.ANSWER_NOT_EDITABLE,
                        "Answer " + question.getSeq() + " has been accepted and cannot be changed");
            }
            saved++;
        }
        log.info("Saved " + saved + " answers for application " + applicationId);
        return saved;
    }

    /**
     * Accept an answer.
     *
     * @param applicationId the application id
     * @param caseQuestionId the case question id
     * @param username the reviewing officer
     * @param note an optional note
     * @throws TollgateException if the answer is not waiting for review
     */
    public void accept(long applicationId, long caseQuestionId, String username, String note)
            throws TollgateException {
        CaseQuestion question = findForApplication(applicationId, caseQuestionId);
        if (!answerReviewDao.accept(question.getId().longValue(), username, StringUtils.trimToNull(note))) {
            throw new TollgateException(ErrorCodes.ANSWER_NOT_REVIEWABLE,
                    "Answer " + question.getSeq() + " is not waiting for review");
        }
    }

    /**
     * Return an answer to the haulier. A note is required.
     *
     * @param applicationId the application id
     * @param caseQuestionId the case question id
     * @param username the reviewing officer
     * @param note the reason for returning the answer
     * @throws TollgateException if there is no note or the answer is not waiting for review
     */
    @Transactional(rollbackFor = TollgateException.class)
    public void returnAnswer(long applicationId, long caseQuestionId, String username, String note)
            throws TollgateException {
        CaseQuestion question = findForApplication(applicationId, caseQuestionId);
        if (StringUtils.isBlank(note)) {
            throw new TollgateException(ErrorCodes.REVIEW_NOTE_REQUIRED,
                    "Enter a note for the haulier when returning an answer");
        }
        if (!answerReviewDao.returnAnswer(question.getId().longValue(), username, note.trim())) {
            throw new TollgateException(ErrorCodes.ANSWER_NOT_REVIEWABLE,
                    "Answer " + question.getSeq() + " is not waiting for review");
        }
        Map<String, Object> app = caseQuestionDao.findApplicationById(applicationId);
        Map<String, String> values = new HashMap<String, String>();
        values.put("reference", (String) app.get("reference"));
        values.put("haulierName", (String) app.get("haulier_name"));
        values.put("question", question.getQuestionText());
        values.put("note", note.trim());
        mailNotifier.send("answer-returned", (String) app.get("contact_email"), values, (String) app.get("reference"));
    }

    private CaseQuestion findForApplication(long applicationId, long caseQuestionId) throws TollgateException {
        CaseQuestion question = caseQuestionDao.findById(caseQuestionId);
        if (question == null || question.getApplicationId().longValue() != applicationId) {
            throw new TollgateException(ErrorCodes.QUESTION_NOT_FOUND, "Question not found");
        }
        return question;
    }
}
