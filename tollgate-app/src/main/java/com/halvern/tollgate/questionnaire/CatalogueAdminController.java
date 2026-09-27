/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: CatalogueAdminController.java 1146 2017-11-20 15:02:17Z koduya $
 */
package com.halvern.tollgate.questionnaire;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin screens for the question catalogue. Changes apply to applications lodged afterwards;
 * lodged applications keep their own copy of the questions (BR-22).
 * Implements BR-21.
 *
 * @author K. Oduya
 * @since 1.0
 */
@Controller
@RequestMapping("/admin/questions")
public class CatalogueAdminController {

    private static final int MAX_QUESTION_LENGTH = 500;

    @Autowired
    private QuestionSetDao questionSetDao;

    /**
     * List question sets.
     *
     * @param model the model
     * @return the view name
     */
    @RequestMapping(method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("questionSets", questionSetDao.findAll());
        return "admin/question-sets";
    }

    /**
     * Show a question set.
     *
     * @param setId the question set id
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/{setId}", method = RequestMethod.GET)
    public String show(@PathVariable("setId") long setId, Model model) {
        QuestionSet set = questionSetDao.findById(setId);
        if (set == null) {
            return "redirect:/admin/questions";
        }
        model.addAttribute("questionSet", set);
        return "admin/question-set";
    }

    /**
     * Save a question.
     *
     * @param setId the question set id
     * @param questionId the question id
     * @param text the question text
     * @param mandatory whether an answer is required
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/{setId}/{questionId}", method = RequestMethod.POST)
    public String save(@PathVariable("setId") long setId, @PathVariable("questionId") long questionId,
            @RequestParam("questionText") String text,
            @RequestParam(value = "mandatory", required = false) String mandatory, RedirectAttributes redirect) {
        if (StringUtils.isBlank(text) || text.trim().length() > MAX_QUESTION_LENGTH) {
            redirect.addFlashAttribute("error", "Question text is required, up to 500 characters.");
        } else {
            Question q = new Question();
            q.setId(Long.valueOf(questionId));
            q.setQuestionSetId(Long.valueOf(setId));
            q.setQuestionText(text.trim());
            q.setMandatory(mandatory != null);
            questionSetDao.updateQuestion(q);
            redirect.addFlashAttribute("message", "Question saved.");
        }
        return "redirect:/admin/questions/" + setId;
    }

    /**
     * Add a question.
     *
     * @param setId the question set id
     * @param text the question text
     * @param mandatory whether an answer is required
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/{setId}/add", method = RequestMethod.POST)
    public String add(@PathVariable("setId") long setId, @RequestParam("questionText") String text,
            @RequestParam(value = "mandatory", required = false) String mandatory, RedirectAttributes redirect) {
        if (StringUtils.isBlank(text) || text.trim().length() > MAX_QUESTION_LENGTH) {
            redirect.addFlashAttribute("error", "Question text is required, up to 500 characters.");
        } else {
            questionSetDao.addQuestion(setId, text.trim(), mandatory != null);
            redirect.addFlashAttribute("message", "Question added.");
        }
        return "redirect:/admin/questions/" + setId;
    }
}
