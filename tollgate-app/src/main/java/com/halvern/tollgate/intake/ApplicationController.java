/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: ApplicationController.java 1161 2017-12-04 10:26:37Z rmistry $
 */
package com.halvern.tollgate.intake;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.halvern.tollgate.audit.AuditService;
import com.halvern.tollgate.notify.NotificationLogDao;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.reference.HaulierDao;
import com.halvern.tollgate.reference.LoadTypeDao;
import com.halvern.tollgate.reference.RouteDao;
import com.halvern.tollgate.reference.Vehicle;
import com.halvern.tollgate.reference.VehicleDao;

/**
 * Screens for permit officers to enter and lodge applications on behalf of hauliers who send
 * their forms by email.
 * Implements BR-01, BR-09, BR-10 and BR-13.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Controller
@RequestMapping("/applications")
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    private static final int LIST_SIZE = 100;

    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private HaulierDao haulierDao;

    @Autowired
    private VehicleDao vehicleDao;

    @Autowired
    private LoadTypeDao loadTypeDao;

    @Autowired
    private RouteDao routeDao;

    @Autowired
    private AttachmentStore attachmentStore;

    @Autowired
    private AuditService auditService;

    @Autowired
    private NotificationLogDao notificationLogDao;

    /**
     * List.
     *
     * @param model the model
     * @return the view name
     */
    @RequestMapping(method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("applications", applicationDao.findRecent(LIST_SIZE));
        return "applications/list";
    }

    /**
     * New application.
     *
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newApplication(Model model) {
        ApplicationForm form = new ApplicationForm();
        form.padTrips();
        model.addAttribute("applicationForm", form);
        addReferenceData(model);
        return "applications/edit";
    }

    /**
     * Create draft.
     *
     * @param form the form
     * @param errors the binding result
     * @param model the model
     * @param principal the officer
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String create(@Valid @ModelAttribute("applicationForm") ApplicationForm form, BindingResult errors, Model model,
            Principal principal, RedirectAttributes redirect) {
        MovementApplication app = new MovementApplication();
        bind(form, app, errors);
        if (errors.hasErrors()) {
            form.padTrips();
            addReferenceData(model);
            return "applications/edit";
        }
        String reference = applicationManager.createDraft(app, principal.getName());
        redirect.addFlashAttribute("message", "Draft " + reference + " saved.");
        return "redirect:/applications/" + reference;
    }

    /**
     * View.
     *
     * @param reference the application reference
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/{reference}", method = RequestMethod.GET)
    public String view(@PathVariable("reference") String reference, Model model) {
        MovementApplication app = applicationDao.findByReference(reference);
        if (app == null) {
            return "redirect:/applications";
        }
        model.addAttribute("app", app);
        model.addAttribute("attachmentTypes", Attachment.TYPES);
        model.addAttribute("history", auditService.findHistory(reference));
        model.addAttribute("notifications", notificationLogDao.findForApplication(reference));
        return "applications/view";
    }

    /**
     * Edit draft.
     *
     * @param reference the application reference
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/{reference}/edit", method = RequestMethod.GET)
    public String edit(@PathVariable("reference") String reference, Model model) {
        MovementApplication app = applicationDao.findByReference(reference);
        if (app == null || !app.isDraft()) {
            return "redirect:/applications/" + reference;
        }
        ApplicationForm form = new ApplicationForm();
        form.setHaulierId(String.valueOf(app.getHaulierId()));
        form.setVehicleRegistration(app.getVehicleRegistration());
        form.setLoadTypeId(app.getLoadTypeId() == null ? "" : app.getLoadTypeId().toString());
        form.setLoadDescription(app.getLoadDescription());
        form.setGrossMassKg(app.getGrossMassKg() == null ? "" : app.getGrossMassKg().toString());
        form.setWidthM(app.getWidthM() == null ? "" : app.getWidthM().toPlainString());
        form.setLengthM(app.getLengthM() == null ? "" : app.getLengthM().toPlainString());
        form.setHeightM(app.getHeightM() == null ? "" : app.getHeightM().toPlainString());
        form.setHazardous(app.isHazardous());
        form.setUnNumber(app.getUnNumber());
        form.setRouteId(app.getRouteId() == null ? "" : app.getRouteId().toString());
        form.setFirstMoveDate(LegacyDateUtil.formatDate(app.getFirstMoveDate()));
        form.setLastMoveDate(LegacyDateUtil.formatDate(app.getLastMoveDate()));
        List<TripForm> trips = new ArrayList<TripForm>();
        for (ApplicationTrip trip : app.getTrips()) {
            TripForm tf = new TripForm();
            tf.setDeparture(LegacyDateUtil.formatDateTime(trip.getDepartureAt()));
            tf.setCrossing(LegacyDateUtil.formatDateTime(trip.getCrossingAt()));
            trips.add(tf);
        }
        form.setTrips(trips);
        form.padTrips();
        model.addAttribute("app", app);
        model.addAttribute("applicationForm", form);
        addReferenceData(model);
        return "applications/edit";
    }

    /**
     * Save draft.
     *
     * @param reference the application reference
     * @param form the form
     * @param errors the binding result
     * @param model the model
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/{reference}/edit", method = RequestMethod.POST)
    public String save(@PathVariable("reference") String reference,
            @Valid @ModelAttribute("applicationForm") ApplicationForm form, BindingResult errors, Model model,
            RedirectAttributes redirect) {
        MovementApplication app = applicationDao.findByReference(reference);
        if (app == null) {
            return "redirect:/applications";
        }
        bind(form, app, errors);
        if (errors.hasErrors()) {
            form.padTrips();
            model.addAttribute("app", app);
            addReferenceData(model);
            return "applications/edit";
        }
        try {
            applicationManager.updateDraft(app);
            redirect.addFlashAttribute("message", "Draft saved.");
        } catch (TollgateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/applications/" + reference;
    }

    /**
     * Upload attachment.
     *
     * @param reference the application reference
     * @param type the attachment type
     * @param file the uploaded file
     * @param principal the officer
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/{reference}/attachments", method = RequestMethod.POST)
    public String upload(@PathVariable("reference") String reference, @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file, Principal principal, RedirectAttributes redirect) {
        if (file == null || file.isEmpty()) {
            redirect.addFlashAttribute("error", "Choose a file to attach.");
            return "redirect:/applications/" + reference;
        }
        try {
            applicationManager.addAttachment(reference, file.getOriginalFilename(), type, file.getSize(),
                    file.getInputStream(), principal.getName());
            redirect.addFlashAttribute("message", "Attachment added.");
        } catch (TollgateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            log.error("Upload failed for " + reference, e);
            redirect.addFlashAttribute("error", "The upload failed. Please try again.");
        }
        return "redirect:/applications/" + reference;
    }

    /**
     * Download attachment.
     *
     * @param reference the application reference
     * @param attachmentId the attachment id
     * @param response the response
     * @throws IOException if the file cannot be sent
     */
    @RequestMapping(value = "/{reference}/attachments/{attachmentId}", method = RequestMethod.GET)
    public void download(@PathVariable("reference") String reference,
            @PathVariable("attachmentId") long attachmentId, HttpServletResponse response) throws IOException {
        MovementApplication app = applicationDao.findByReference(reference);
        Attachment attachment = applicationDao.findAttachment(attachmentId);
        if (app == null || attachment == null || !attachment.getApplicationId().equals(app.getId())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        InputStream in;
        try {
            in = attachmentStore.open(attachment.getStoragePath());
        } catch (TollgateException e) {
            log.error("Attachment " + attachmentId + " missing from store", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + attachment.getFileName() + "\"");
        OutputStream out = response.getOutputStream();
        FileCopyUtils.copy(in, out);
    }

    private void addReferenceData(Model model) {
        model.addAttribute("hauliers", haulierDao.findAll());
        model.addAttribute("loadTypes", loadTypeDao.findAll());
        model.addAttribute("routes", routeDao.findAll());
    }

    /**
     * Copies the form into the application, rejecting fields that are not valid. Fields may be left
     * blank on a draft; they are checked for completeness when the application is lodged.
     */
    private void bind(ApplicationForm form, MovementApplication app, BindingResult errors) {
        Long haulierId = parseId(form.getHaulierId());
        if (haulierId == null || haulierDao.findById(haulierId.longValue()) == null) {
            errors.rejectValue("haulierId", "required", "Choose the haulier");
        }
        app.setHaulierId(haulierId);

        app.setVehicleId(null);
        if (StringUtils.isNotBlank(form.getVehicleRegistration())) {
            Vehicle vehicle = vehicleDao.findByRegistration(form.getVehicleRegistration().trim().toUpperCase());
            if (vehicle == null) {
                errors.rejectValue("vehicleRegistration", "unknown", "That vehicle is not registered");
            } else if (haulierId != null && !vehicle.getHaulierId().equals(haulierId)) {
                errors.rejectValue("vehicleRegistration", "haulier", "That vehicle belongs to another haulier");
            } else {
                app.setVehicleId(vehicle.getId());
            }
        }

        app.setLoadTypeId(parseId(form.getLoadTypeId()));
        app.setRouteId(parseId(form.getRouteId()));

        app.setLoadDescription(StringUtils.trimToNull(form.getLoadDescription()));

        app.setGrossMassKg(null);
        if (StringUtils.isNotBlank(form.getGrossMassKg())) {
            try {
                Integer mass = Integer.valueOf(form.getGrossMassKg().trim());
                if (mass.intValue() <= 0) {
                    errors.rejectValue("grossMassKg", "invalid", "Gross mass must be greater than zero");
                }
                app.setGrossMassKg(mass);
            } catch (NumberFormatException e) {
                errors.rejectValue("grossMassKg", "invalid", "Gross mass must be a whole number of kilograms");
            }
        }
        app.setWidthM(parseMetres(form.getWidthM(), "widthM", "Width", errors));
        app.setLengthM(parseMetres(form.getLengthM(), "lengthM", "Length", errors));
        app.setHeightM(parseMetres(form.getHeightM(), "heightM", "Height", errors));

        app.setHazardous(form.isHazardous());
        app.setUnNumber(null);
        if (form.isHazardous()) {
            // TG-234 format is checked by @UnNumber
            if (StringUtils.isBlank(form.getUnNumber())) {
                errors.rejectValue("unNumber", "required", "A hazardous load needs a four digit UN number");
            } else {
                app.setUnNumber(form.getUnNumber().trim());
            }
        }

        Date first = parseDate(form.getFirstMoveDate(), "firstMoveDate", errors);
        Date last = parseDate(form.getLastMoveDate(), "lastMoveDate", errors);
        app.setFirstMoveDate(first);
        app.setLastMoveDate(last);

        List<ApplicationTrip> trips = new ArrayList<ApplicationTrip>();
        List<TripForm> rows = form.getTrips() == null ? new ArrayList<TripForm>() : form.getTrips();
        for (int i = 0; i < rows.size() && i < ApplicationForm.MAX_TRIPS; i++) {
            TripForm row = rows.get(i);
            if (StringUtils.isBlank(row.getDeparture()) && StringUtils.isBlank(row.getCrossing())) {
                continue;
            }
            Date departure = LegacyDateUtil.parseDateTime(row.getDeparture());
            if (departure == null) {
                errors.rejectValue("trips[" + i + "].departure", "invalid", "Enter the departure as dd/mm/yyyy hh:mm");
            }
            Date crossing = null;
            if (StringUtils.isNotBlank(row.getCrossing())) {
                crossing = LegacyDateUtil.parseDateTime(row.getCrossing());
                if (crossing == null) {
                    errors.rejectValue("trips[" + i + "].crossing", "invalid", "Enter the crossing as dd/mm/yyyy hh:mm");
                } else if (departure != null && crossing.before(departure)) {
                    errors.rejectValue("trips[" + i + "].crossing", "order", "Crossing is before departure");
                }
            }
            ApplicationTrip trip = new ApplicationTrip();
            trip.setTripNo(trips.size() + 1);
            trip.setDepartureAt(departure);
            trip.setCrossingAt(crossing);
            trips.add(trip);
        }
        app.setTrips(trips);
    }

    private Long parseId(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Date parseDate(String value, String field, BindingResult errors) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Date date = LegacyDateUtil.parseDate(value);
        if (date == null) {
            errors.rejectValue(field, "invalid", "Enter the date as dd/mm/yyyy");
        }
        return date;
    }

    private BigDecimal parseMetres(String value, String field, String label, BindingResult errors) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            BigDecimal metres = new BigDecimal(value.trim());
            if (metres.signum() <= 0 || metres.scale() > 2) {
                errors.rejectValue(field, "invalid", label + " must be in metres to two decimal places");
                return null;
            }
            return metres.setScale(2);
        } catch (NumberFormatException e) {
            errors.rejectValue(field, "invalid", label + " must be in metres, for example 4.25");
            return null;
        }
    }
}
