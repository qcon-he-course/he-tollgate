/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: HaulierAdminController.java 1063 2017-09-21 16:33:09Z rmistry $
 */
package com.halvern.tollgate.reference;

import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
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

/**
 * Admin screens for hauliers, their licence, insurance and vehicles.
 * Implements BR-02 and BR-03.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Controller
@RequestMapping("/admin")
public class HaulierAdminController {

    @Autowired
    private HaulierDao haulierDao;

    @Autowired
    private VehicleDao vehicleDao;

    /**
     * List hauliers.
     *
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/hauliers", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("hauliers", haulierDao.findAll());
        return "admin/hauliers";
    }

    /**
     * New haulier.
     *
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/hauliers/new", method = RequestMethod.GET)
    public String newHaulier(Model model) {
        model.addAttribute("haulierForm", new HaulierForm());
        return "admin/haulier-edit";
    }

    /**
     * Create haulier.
     *
     * @param form the form
     * @param errors the binding result
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/hauliers/new", method = RequestMethod.POST)
    public String create(@ModelAttribute("haulierForm") HaulierForm form, BindingResult errors,
            RedirectAttributes redirect) {
        validate(form, errors);
        if (errors.hasErrors()) {
            return "admin/haulier-edit";
        }
        Haulier haulier = new Haulier();
        copyDetails(form, haulier);
        long id = haulierDao.insert(haulier);

        OperatorLicence licence = new OperatorLicence();
        licence.setHaulierId(Long.valueOf(id));
        copyLicence(form, licence);
        haulierDao.saveLicence(licence);

        InsurancePolicy policy = new InsurancePolicy();
        policy.setHaulierId(Long.valueOf(id));
        copyInsurance(form, policy);
        haulierDao.saveInsurance(policy);

        redirect.addFlashAttribute("message", "Haulier " + haulier.getName() + " created.");
        return "redirect:/admin/hauliers/" + id;
    }

    /**
     * Edit haulier.
     *
     * @param id the haulier id
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/hauliers/{id}", method = RequestMethod.GET)
    public String edit(@PathVariable("id") long id, Model model) {
        Haulier haulier = haulierDao.findById(id);
        if (haulier == null) {
            return "redirect:/admin/hauliers";
        }
        HaulierForm form = new HaulierForm();
        form.setName(haulier.getName());
        form.setContactName(haulier.getContactName());
        form.setContactEmail(haulier.getContactEmail());
        form.setPhone(haulier.getPhone());
        form.setAddressLine1(haulier.getAddressLine1());
        form.setTown(haulier.getTown());
        form.setPostcode(haulier.getPostcode());
        OperatorLicence licence = haulierDao.findLicence(id);
        if (licence != null) {
            form.setLicenceNumber(licence.getLicenceNumber());
            form.setLicenceValidFrom(DateHelper.formatScreenDate(licence.getValidFrom()));
            form.setLicenceExpiryDate(DateHelper.formatScreenDate(licence.getExpiryDate()));
        }
        InsurancePolicy policy = haulierDao.findInsurance(id);
        if (policy != null) {
            form.setInsurer(policy.getInsurer());
            form.setPolicyNumber(policy.getPolicyNumber());
            form.setInsuranceStartDate(DateHelper.formatScreenDate(policy.getStartDate()));
            form.setInsuranceEndDate(DateHelper.formatScreenDate(policy.getEndDate()));
        }
        model.addAttribute("haulier", haulier);
        model.addAttribute("haulierForm", form);
        model.addAttribute("vehicles", vehicleDao.findByHaulier(id));
        return "admin/haulier-edit";
    }

    /**
     * Save haulier.
     *
     * @param id the haulier id
     * @param form the form
     * @param errors the binding result
     * @param model the model
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/hauliers/{id}", method = RequestMethod.POST)
    public String save(@PathVariable("id") long id, @ModelAttribute("haulierForm") HaulierForm form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        Haulier haulier = haulierDao.findById(id);
        if (haulier == null) {
            return "redirect:/admin/hauliers";
        }
        validate(form, errors);
        if (errors.hasErrors()) {
            model.addAttribute("haulier", haulier);
            model.addAttribute("vehicles", vehicleDao.findByHaulier(id));
            return "admin/haulier-edit";
        }
        copyDetails(form, haulier);
        haulierDao.update(haulier);

        OperatorLicence licence = haulierDao.findLicence(id);
        if (licence == null) {
            licence = new OperatorLicence();
            licence.setHaulierId(Long.valueOf(id));
        }
        copyLicence(form, licence);
        haulierDao.saveLicence(licence);

        InsurancePolicy policy = haulierDao.findInsurance(id);
        if (policy == null) {
            policy = new InsurancePolicy();
            policy.setHaulierId(Long.valueOf(id));
        }
        copyInsurance(form, policy);
        haulierDao.saveInsurance(policy);

        redirect.addFlashAttribute("message", "Haulier saved.");
        return "redirect:/admin/hauliers/" + id;
    }

    /**
     * New vehicle.
     *
     * @param id the haulier id
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/hauliers/{id}/vehicles/new", method = RequestMethod.GET)
    public String newVehicle(@PathVariable("id") long id, Model model) {
        Haulier haulier = haulierDao.findById(id);
        if (haulier == null) {
            return "redirect:/admin/hauliers";
        }
        model.addAttribute("haulier", haulier);
        model.addAttribute("vehicleForm", new VehicleForm());
        model.addAttribute("categories", VehicleDao.CATEGORIES);
        return "admin/vehicle-edit";
    }

    /**
     * Create vehicle.
     *
     * @param id the haulier id
     * @param form the form
     * @param errors the binding result
     * @param model the model
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/hauliers/{id}/vehicles/new", method = RequestMethod.POST)
    public String createVehicle(@PathVariable("id") long id, @ModelAttribute("vehicleForm") VehicleForm form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        Haulier haulier = haulierDao.findById(id);
        if (haulier == null) {
            return "redirect:/admin/hauliers";
        }
        validateVehicle(form, null, errors);
        if (errors.hasErrors()) {
            model.addAttribute("haulier", haulier);
            model.addAttribute("categories", VehicleDao.CATEGORIES);
            return "admin/vehicle-edit";
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setHaulierId(Long.valueOf(id));
        vehicle.setRegistration(normaliseRegistration(form.getRegistration()));
        vehicle.setCategory(form.getCategory());
        vehicleDao.insert(vehicle);
        redirect.addFlashAttribute("message", "Vehicle " + vehicle.getRegistration() + " added.");
        return "redirect:/admin/hauliers/" + id;
    }

    /**
     * Edit vehicle.
     *
     * @param vehicleId the vehicle id
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/vehicles/{vehicleId}", method = RequestMethod.GET)
    public String editVehicle(@PathVariable("vehicleId") long vehicleId, Model model) {
        Vehicle vehicle = vehicleDao.findById(vehicleId);
        if (vehicle == null) {
            return "redirect:/admin/hauliers";
        }
        VehicleForm form = new VehicleForm();
        form.setRegistration(vehicle.getRegistration());
        form.setCategory(vehicle.getCategory());
        model.addAttribute("haulier", haulierDao.findById(vehicle.getHaulierId().longValue()));
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("vehicleForm", form);
        model.addAttribute("categories", VehicleDao.CATEGORIES);
        return "admin/vehicle-edit";
    }

    /**
     * Save vehicle.
     *
     * @param vehicleId the vehicle id
     * @param form the form
     * @param errors the binding result
     * @param model the model
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/vehicles/{vehicleId}", method = RequestMethod.POST)
    public String saveVehicle(@PathVariable("vehicleId") long vehicleId,
            @ModelAttribute("vehicleForm") VehicleForm form, BindingResult errors, Model model,
            RedirectAttributes redirect) {
        Vehicle vehicle = vehicleDao.findById(vehicleId);
        if (vehicle == null) {
            return "redirect:/admin/hauliers";
        }
        validateVehicle(form, vehicle, errors);
        if (errors.hasErrors()) {
            model.addAttribute("haulier", haulierDao.findById(vehicle.getHaulierId().longValue()));
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("categories", VehicleDao.CATEGORIES);
            return "admin/vehicle-edit";
        }
        vehicle.setRegistration(normaliseRegistration(form.getRegistration()));
        vehicle.setCategory(form.getCategory());
        vehicleDao.update(vehicle);
        redirect.addFlashAttribute("message", "Vehicle saved.");
        return "redirect:/admin/hauliers/" + vehicle.getHaulierId();
    }

    private void validate(HaulierForm form, BindingResult errors) {
        if (StringUtils.isBlank(form.getName())) {
            errors.rejectValue("name", "required", "Name is required");
        }
        if (StringUtils.isBlank(form.getContactEmail()) || form.getContactEmail().indexOf('@') < 1) {
            errors.rejectValue("contactEmail", "invalid", "A valid contact email is required");
        }
        if (StringUtils.isBlank(form.getLicenceNumber())) {
            errors.rejectValue("licenceNumber", "required", "Licence number is required");
        }
        Date validFrom = DateHelper.parseScreenDate(form.getLicenceValidFrom());
        Date expiry = DateHelper.parseScreenDate(form.getLicenceExpiryDate());
        if (validFrom == null) {
            errors.rejectValue("licenceValidFrom", "invalid", "Enter the date as dd/mm/yyyy");
        }
        if (expiry == null) {
            errors.rejectValue("licenceExpiryDate", "invalid", "Enter the date as dd/mm/yyyy");
        }
        if (validFrom != null && expiry != null && expiry.before(validFrom)) {
            errors.rejectValue("licenceExpiryDate", "order", "Expiry must be after the start date");
        }
        if (StringUtils.isBlank(form.getInsurer())) {
            errors.rejectValue("insurer", "required", "Insurer is required");
        }
        if (StringUtils.isBlank(form.getPolicyNumber())) {
            errors.rejectValue("policyNumber", "required", "Policy number is required");
        }
        Date start = DateHelper.parseScreenDate(form.getInsuranceStartDate());
        Date end = DateHelper.parseScreenDate(form.getInsuranceEndDate());
        if (start == null) {
            errors.rejectValue("insuranceStartDate", "invalid", "Enter the date as dd/mm/yyyy");
        }
        if (end == null) {
            errors.rejectValue("insuranceEndDate", "invalid", "Enter the date as dd/mm/yyyy");
        }
        if (start != null && end != null && end.before(start)) {
            errors.rejectValue("insuranceEndDate", "order", "End date must be after the start date");
        }
    }

    private void validateVehicle(VehicleForm form, Vehicle current, BindingResult errors) {
        String reg = normaliseRegistration(form.getRegistration());
        if (StringUtils.isBlank(reg) || reg.length() > 10) {
            errors.rejectValue("registration", "invalid", "Enter a registration of up to 10 characters");
        } else {
            Vehicle existing = vehicleDao.findByRegistration(reg);
            if (existing != null && (current == null || !existing.getId().equals(current.getId()))) {
                errors.rejectValue("registration", "duplicate", "That registration is already on file");
            }
        }
        if (!ArrayUtils.contains(VehicleDao.CATEGORIES, form.getCategory())) {
            errors.rejectValue("category", "invalid", "Choose a category");
        }
    }

    private String normaliseRegistration(String registration) {
        return StringUtils.upperCase(StringUtils.trimToEmpty(registration));
    }

    private void copyDetails(HaulierForm form, Haulier haulier) {
        haulier.setName(form.getName().trim());
        haulier.setContactName(StringUtils.trimToNull(form.getContactName()));
        haulier.setContactEmail(form.getContactEmail().trim());
        haulier.setPhone(StringUtils.trimToNull(form.getPhone()));
        haulier.setAddressLine1(StringUtils.trimToNull(form.getAddressLine1()));
        haulier.setTown(StringUtils.trimToNull(form.getTown()));
        haulier.setPostcode(StringUtils.upperCase(StringUtils.trimToNull(form.getPostcode())));
    }

    private void copyLicence(HaulierForm form, OperatorLicence licence) {
        licence.setLicenceNumber(form.getLicenceNumber().trim());
        licence.setValidFrom(DateHelper.parseScreenDate(form.getLicenceValidFrom()));
        licence.setExpiryDate(DateHelper.parseScreenDate(form.getLicenceExpiryDate()));
    }

    private void copyInsurance(HaulierForm form, InsurancePolicy policy) {
        policy.setInsurer(form.getInsurer().trim());
        policy.setPolicyNumber(form.getPolicyNumber().trim());
        policy.setStartDate(DateHelper.parseScreenDate(form.getInsuranceStartDate()));
        policy.setEndDate(DateHelper.parseScreenDate(form.getInsuranceEndDate()));
    }
}
