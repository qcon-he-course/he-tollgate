/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: PermitController.java 1179 2018-01-31 14:58:06Z gferrand $
 */
package com.halvern.tollgate.permitdoc;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Screens for approving applications and viewing permits.
 * Implements BR-30 and BR-36.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Controller
public class PermitController {

    private static final int LIST_SIZE = 100;


    @Autowired
    private PermitDao permitDao;

    /**
     * List permits, or go to the permit for an application.
     *
     * @param application an application reference, optional
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/permits", method = RequestMethod.GET)
    public String list(@RequestParam(value = "application", required = false) String application, Model model) {
        if (application != null) {
            Permit permit = permitDao.findByApplication(application);
            if (permit != null) {
                return "redirect:/permits/" + permit.getReference();
            }
        }
        model.addAttribute("permits", permitDao.findRecent(LIST_SIZE));
        return "permits/list";
    }

    /**
     * View a permit.
     *
     * @param reference the permit reference
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/permits/{reference}", method = RequestMethod.GET)
    public String view(@PathVariable("reference") String reference, Model model) {
        PermitReference ref = PermitReference.parse(reference);
        Permit permit = ref == null ? null : permitDao.findByReference(ref);
        if (permit == null) {
            return "redirect:/permits";
        }
        model.addAttribute("permit", permit);
        return "permits/view";
    }

    /**
     * Download the printed permit.
     *
     * @param reference the permit reference
     * @param response the response
     * @throws IOException if the document cannot be sent
     */
    @RequestMapping(value = "/permits/{reference}/document", method = RequestMethod.GET)
    public void document(@PathVariable("reference") String reference, HttpServletResponse response)
            throws IOException {
        PermitReference ref = PermitReference.parse(reference);
        Permit permit = ref == null ? null : permitDao.findByReference(ref);
        byte[] pdf = permit == null ? null : permitDao.findDocument(permit.getId().longValue());
        if (pdf == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(LegacyPermitPdfWriter.CONTENT_TYPE);
        response.setHeader("Content-Disposition", "inline; filename=\"" + ref + ".pdf\"");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
    }
}
