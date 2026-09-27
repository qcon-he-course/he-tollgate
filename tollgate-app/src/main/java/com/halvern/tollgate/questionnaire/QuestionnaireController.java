/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: QuestionnaireController.java 1141 2017-11-16 11:39:58Z koduya $
 */
package com.halvern.tollgate.questionnaire;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * Screens for entering the haulier's questionnaire answers and for the officer's review.
 * Hauliers send their answers by email; a permit officer enters them.
 * Implements BR-23 and BR-24.
 *
 * @author K. Oduya
 * @since 1.0
 */
@Controller
@RequestMapping("/applications/{reference}")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireManager questionnaireManager;

    @Autowired
    private CaseQuestionDao caseQuestionDao;

    /**
     * Show the answer entry screen.
     *
     * @param reference the application reference
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/questionnaire", method = RequestMethod.GET)
    public String answers(@PathVariable("reference") String reference, Model model) {
        Map<String, Object> app = caseQuestionDao.findApplication(reference);
        if (app == null || !"SUBMITTED".equals(app.get("status"))) {
            return "redirect:/applications/" + reference;
        }
        List<CaseQuestion> questions = questionnaireManager.findQuestions(((Number) app.get("id")).longValue());
        AnswerForm form = new AnswerForm();
        List<Long> ids = new ArrayList<Long>();
        List<String> answers = new ArrayList<String>();
        for (CaseQuestion q : questions) {
            ids.add(q.getId());
            answers.add(q.getAnswerText());
        }
        form.setCaseQuestionIds(ids);
        form.setAnswers(answers);
        model.addAttribute("app", app);
        model.addAttribute("questions", questions);
        model.addAttribute("answerForm", form);
        return "questionnaire/answers";
    }

    /**
     * Show the review screen.
     *
     * @param reference the application reference
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/review", method = RequestMethod.GET)
    public String review(@PathVariable("reference") String reference, Model model) {
        Map<String, Object> app = caseQuestionDao.findApplication(reference);
        if (app == null) {
            return "redirect:/applications";
        }
        model.addAttribute("app", app);
        model.addAttribute("questions", questionnaireManager.findQuestions(((Number) app.get("id")).longValue()));
        return "questionnaire/review";
    }
}
