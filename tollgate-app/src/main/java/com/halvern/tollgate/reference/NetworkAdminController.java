/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: NetworkAdminController.java 1055 2017-09-15 15:12:37Z gferrand $
 */
package com.halvern.tollgate.reference;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Admin screens for the network: structures, routes and load types.
 * Implements BR-05, BR-06 and BR-08.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Controller
@RequestMapping("/admin")
public class NetworkAdminController {

    private static final Logger log = LoggerFactory.getLogger(NetworkAdminController.class);

    @Autowired
    private StructureDao structureDao;

    @Autowired
    private RouteDao routeDao;

    @Autowired
    private LoadTypeDao loadTypeDao;

    /**
     * List structures.
     *
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/structures", method = RequestMethod.GET)
    public String listStructures(Model model) {
        model.addAttribute("structures", structureDao.findAll());
        return "admin/structures";
    }

    /**
     * Edit structure.
     *
     * @param id the structure id
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/structures/{id}", method = RequestMethod.GET)
    public String editStructure(@PathVariable("id") long id, Model model) {
        Structure structure = structureDao.findById(id);
        if (structure == null) {
            return "redirect:/admin/structures";
        }
        StructureForm form = new StructureForm();
        form.setName(structure.getName());
        form.setMassRatingT(structure.getMassRatingT() == null ? "" : structure.getMassRatingT().toString());
        form.setClearanceM(structure.getClearanceM() == null ? "" : structure.getClearanceM().toPlainString());
        model.addAttribute("structure", structure);
        model.addAttribute("structureForm", form);
        return "admin/structure-edit";
    }

    /**
     * Save structure.
     *
     * @param id the structure id
     * @param form the form
     * @param errors the binding result
     * @param model the model
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/structures/{id}", method = RequestMethod.POST)
    public String saveStructure(@PathVariable("id") long id, @ModelAttribute("structureForm") StructureForm form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        Structure structure = structureDao.findById(id);
        if (structure == null) {
            return "redirect:/admin/structures";
        }
        if (StringUtils.isBlank(form.getName())) {
            errors.rejectValue("name", "required", "Name is required");
        }
        Integer rating = null;
        BigDecimal clearance = null;
        if (structure.isCarrying()) {
            // BR-05: a carrying structure must have a mass rating
            try {
                rating = Integer.valueOf(form.getMassRatingT().trim());
                if (rating.intValue() <= 0) {
                    errors.rejectValue("massRatingT", "invalid", "Mass rating must be greater than zero");
                }
            } catch (Exception e) {
                errors.rejectValue("massRatingT", "invalid", "Mass rating must be a whole number of tonnes");
            }
        } else {
            // BR-05: an overbridge must have a clearance
            try {
                clearance = new BigDecimal(form.getClearanceM().trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
                if (clearance.signum() <= 0) {
                    errors.rejectValue("clearanceM", "invalid", "Clearance must be greater than zero");
                }
            } catch (Exception e) {
                errors.rejectValue("clearanceM", "invalid", "Clearance must be in metres, for example 5.30");
            }
        }
        if (errors.hasErrors()) {
            model.addAttribute("structure", structure);
            return "admin/structure-edit";
        }
        structure.setName(form.getName().trim());
        structure.setMassRatingT(rating);
        structure.setClearanceM(clearance);
        structureDao.update(structure);
        log.info("Structure " + structure.getCode() + " saved");
        redirect.addFlashAttribute("message", "Structure " + structure.getCode() + " saved.");
        return "redirect:/admin/structures";
    }

    /**
     * List routes.
     *
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/routes", method = RequestMethod.GET)
    public String listRoutes(Model model) {
        model.addAttribute("routes", routeDao.findAll());
        return "admin/routes";
    }

    /**
     * View route.
     *
     * @param id the route id
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/routes/{id}", method = RequestMethod.GET)
    public String viewRoute(@PathVariable("id") long id, Model model) {
        Route route = routeDao.findById(id);
        if (route == null) {
            return "redirect:/admin/routes";
        }
        model.addAttribute("route", route);
        model.addAttribute("minimumClearance", routeDao.findMinimumClearance(id));
        return "admin/route-view";
    }

    /**
     * Save route description.
     *
     * @param id the route id
     * @param description the description
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/routes/{id}", method = RequestMethod.POST)
    public String saveRoute(@PathVariable("id") long id, @RequestParam("description") String description,
            RedirectAttributes redirect) {
        routeDao.updateDescription(id, StringUtils.trimToNull(description));
        redirect.addFlashAttribute("message", "Route saved.");
        return "redirect:/admin/routes/" + id;
    }

    /**
     * List load types.
     *
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/load-types", method = RequestMethod.GET)
    public String listLoadTypes(Model model) {
        model.addAttribute("loadTypes", loadTypeDao.findAll());
        return "admin/load-types";
    }

    /**
     * Save load type description.
     *
     * @param id the load type id
     * @param description the description
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/load-types/{id}", method = RequestMethod.POST)
    public String saveLoadType(@PathVariable("id") long id, @RequestParam("description") String description,
            RedirectAttributes redirect) {
        if (StringUtils.isBlank(description)) {
            redirect.addFlashAttribute("error", "Description is required.");
        } else {
            loadTypeDao.updateDescription(id, description.trim());
            redirect.addFlashAttribute("message", "Load type saved.");
        }
        return "redirect:/admin/load-types";
    }
}
