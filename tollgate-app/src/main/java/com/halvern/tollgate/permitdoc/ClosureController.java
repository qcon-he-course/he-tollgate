/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: ClosureController.java 1186 2018-02-09 11:21:35Z rmistry $
 */
package com.halvern.tollgate.permitdoc;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.halvern.tollgate.platform.DateHelper;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.reference.Structure;
import com.halvern.tollgate.reference.StructureDao;

/**
 * Screens for recording and ending structure closures.
 * Implements BR-33 and BR-34.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Controller
@RequestMapping("/closures")
public class ClosureController {

    private static final int LIST_SIZE = 50;

    @Autowired
    private ClosureManager closureManager;

    @Autowired
    private ClosureDao closureDao;

    @Autowired
    private StructureDao structureDao;

    /**
     * List closures.
     *
     * @param model the model
     * @return the view name
     */
    @RequestMapping(method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("closures", closureDao.findRecent(LIST_SIZE));
        return "closures/list";
    }

    /**
     * New closure.
     *
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newClosure(Model model) {
        model.addAttribute("closureForm", new ClosureForm());
        model.addAttribute("structures", structureDao.findAll());
        return "closures/new";
    }

    /**
     * Record a closure.
     *
     * @param form the form
     * @param errors the binding result
     * @param model the model
     * @param principal the user recording the closure
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String record(@ModelAttribute("closureForm") ClosureForm form, BindingResult errors, Model model,
            Principal principal, RedirectAttributes redirect) {
        Structure structure = null;
        if (StringUtils.isNumeric(form.getStructureId()) && StringUtils.isNotEmpty(form.getStructureId())) {
            structure = structureDao.findById(Long.parseLong(form.getStructureId()));
        }
        if (structure == null) {
            errors.rejectValue("structureId", "required", "Choose the structure");
        }
        Date start = DateHelper.parseScreenDate(form.getStartDate());
        Date end = DateHelper.parseScreenDate(form.getEndDate());
        if (start == null) {
            errors.rejectValue("startDate", "invalid", "Enter the date as dd/mm/yyyy");
        }
        if (end == null) {
            errors.rejectValue("endDate", "invalid", "Enter the date as dd/mm/yyyy");
        } else if (start != null && end.before(start)) {
            errors.rejectValue("endDate", "order", "The closure ends before it starts");
        }
        if (StringUtils.isBlank(form.getReason())) {
            errors.rejectValue("reason", "required", "Give the reason for the closure");
        }
        if (errors.hasErrors()) {
            model.addAttribute("structures", structureDao.findAll());
            return "closures/new";
        }
        StructureClosure closure = new StructureClosure();
        closure.setStructureId(structure.getId());
        closure.setStartDate(start);
        closure.setEndDate(end);
        closure.setReason(form.getReason().trim());
        closure.setRecordedBy(principal.getName());
        try {
            List<Permit> suspended = closureManager.recordClosure(closure);
            redirect.addFlashAttribute("message", "Closure of " + structure.getCode() + " recorded. "
                    + suspended.size() + " permits suspended.");
        } catch (TollgateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/closures";
    }

    /**
     * End a closure.
     *
     * @param id the closure id
     * @param principal the user ending the closure
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/{id}/end", method = RequestMethod.POST)
    public String end(@PathVariable("id") long id, Principal principal, RedirectAttributes redirect) {
        try {
            List<Permit> permits = closureManager.endClosure(id, principal.getName());
            redirect.addFlashAttribute("message", "Closure ended. " + permits.size() + " permits updated.");
        } catch (TollgateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/closures";
    }
}
