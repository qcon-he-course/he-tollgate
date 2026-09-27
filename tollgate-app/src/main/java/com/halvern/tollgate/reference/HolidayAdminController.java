/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: HolidayAdminController.java 1058 2017-09-18 11:54:21Z rmistry $
 */
package com.halvern.tollgate.reference;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.halvern.tollgate.platform.DateHelper;

/**
 * Admin screen for the holiday calendar.
 * Implements BR-14.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Controller
@RequestMapping("/admin/holidays")
public class HolidayAdminController {

    @Autowired
    private HolidayDao holidayDao;

    /**
     * List holidays for a year. Defaults to the current year.
     *
     * @param year the year
     * @param model the model
     * @return the view name
     */
    @RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "year", required = false) Integer year, Model model) {
        if (!model.containsAttribute("holidayForm")) {
            model.addAttribute("holidayForm", new HolidayForm());
        }
        populate(year, model);
        return "admin/holidays";
    }

    /**
     * Add holiday.
     *
     * @param form the form
     * @param errors the binding result
     * @param model the model
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(method = RequestMethod.POST)
    public String add(@ModelAttribute("holidayForm") HolidayForm form, BindingResult errors, Model model,
            RedirectAttributes redirect) {
        Date date = DateHelper.parseScreenDate(form.getHolidayDate());
        if (date == null) {
            errors.rejectValue("holidayDate", "invalid", "Enter the date as dd/mm/yyyy");
        } else if (holidayDao.isHoliday(date)) {
            errors.rejectValue("holidayDate", "duplicate", "That date is already a holiday");
        }
        if (StringUtils.isBlank(form.getDescription())) {
            errors.rejectValue("description", "required", "Description is required");
        }
        if (errors.hasErrors()) {
            populate(null, model);
            return "admin/holidays";
        }
        Holiday holiday = new Holiday();
        holiday.setHolidayDate(date);
        holiday.setDescription(form.getDescription().trim());
        holidayDao.insert(holiday);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        redirect.addFlashAttribute("message", "Holiday added.");
        return "redirect:/admin/holidays?year=" + cal.get(Calendar.YEAR);
    }

    /**
     * Delete holiday.
     *
     * @param id the holiday id
     * @param year the year being viewed
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/{id}/delete", method = RequestMethod.POST)
    public String delete(@PathVariable("id") long id, @RequestParam(value = "year", required = false) Integer year,
            RedirectAttributes redirect) {
        holidayDao.delete(id);
        redirect.addFlashAttribute("message", "Holiday removed.");
        return "redirect:/admin/holidays" + (year == null ? "" : "?year=" + year);
    }

    private void populate(Integer year, Model model) {
        int y;
        if (year == null) {
            y = Calendar.getInstance().get(Calendar.YEAR);
        } else {
            y = year.intValue();
        }
        model.addAttribute("year", Integer.valueOf(y));
        model.addAttribute("holidays", holidayDao.findByYear(y));
    }
}
