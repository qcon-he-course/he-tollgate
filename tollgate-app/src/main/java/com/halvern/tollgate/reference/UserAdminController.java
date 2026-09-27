/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: UserAdminController.java 1066 2017-09-25 10:27:44Z koduya $
 */
package com.halvern.tollgate.reference;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin screens for staff users and roles.
 * Implements BR-40 and BR-41.
 *
 * @author K. Oduya
 * @since 1.0
 */
@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    /** BR-41: minimum password length. */
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Autowired
    private UserDao userDao;

    /**
     * List users.
     *
     * @param model the model
     * @return the view name
     */
    @RequestMapping(method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("users", userDao.findAll());
        return "admin/users";
    }

    /**
     * New user.
     *
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newUser(Model model) {
        UserForm form = new UserForm();
        form.setEnabled(true);
        form.setRoles(new ArrayList<String>());
        model.addAttribute("userForm", form);
        model.addAttribute("allRoles", UserDao.ROLES);
        return "admin/user-edit";
    }

    /**
     * Create user.
     *
     * @param form the form
     * @param errors the binding result
     * @param model the model
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String create(@ModelAttribute("userForm") UserForm form, BindingResult errors, Model model,
            RedirectAttributes redirect) {
        if (StringUtils.isBlank(form.getUsername()) || !StringUtils.isAlphanumeric(form.getUsername().trim())) {
            errors.rejectValue("username", "invalid", "Username must be letters and digits only");
        } else if (userDao.findByUsername(form.getUsername().trim()) != null) {
            errors.rejectValue("username", "duplicate", "That username is taken");
        }
        if (StringUtils.length(form.getNewPassword()) < MIN_PASSWORD_LENGTH) {
            errors.rejectValue("newPassword", "short", "Password must be at least 8 characters");
        }
        validateCommon(form, errors);
        if (errors.hasErrors()) {
            model.addAttribute("allRoles", UserDao.ROLES);
            return "admin/user-edit";
        }
        StaffUser user = new StaffUser();
        user.setUsername(form.getUsername().trim());
        user.setPasswordHash(new BCryptPasswordEncoder().encode(form.getNewPassword()));
        user.setFullName(form.getFullName().trim());
        user.setEmail(StringUtils.trimToNull(form.getEmail()));
        user.setEnabled(form.isEnabled());
        long id = userDao.insert(user);
        userDao.setRoles(id, form.getRoles());
        redirect.addFlashAttribute("message", "User " + user.getUsername() + " created.");
        return "redirect:/admin/users";
    }

    /**
     * Edit user.
     *
     * @param id the user id
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String edit(@PathVariable("id") long id, Model model) {
        StaffUser user = userDao.findById(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        UserForm form = new UserForm();
        form.setUsername(user.getUsername());
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setEnabled(user.isEnabled());
        form.setRoles(user.getRoles());
        form.setPanelChair(user.isPanelChair());
        model.addAttribute("user", user);
        model.addAttribute("userForm", form);
        model.addAttribute("allRoles", UserDao.ROLES);
        return "admin/user-edit";
    }

    /**
     * Save user. A password is only changed when a new one is entered.
     *
     * @param id the user id
     * @param form the form
     * @param errors the binding result
     * @param model the model
     * @param redirect the redirect attributes
     * @return the view name
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public String save(@PathVariable("id") long id, @ModelAttribute("userForm") UserForm form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        StaffUser user = userDao.findById(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        if (StringUtils.isNotEmpty(form.getNewPassword())
                && form.getNewPassword().length() < MIN_PASSWORD_LENGTH) {
            errors.rejectValue("newPassword", "short", "Password must be at least 8 characters");
        }
        validateCommon(form, errors);
        if (errors.hasErrors()) {
            form.setUsername(user.getUsername());
            model.addAttribute("user", user);
            model.addAttribute("allRoles", UserDao.ROLES);
            return "admin/user-edit";
        }
        user.setFullName(form.getFullName().trim());
        user.setEmail(StringUtils.trimToNull(form.getEmail()));
        user.setEnabled(form.isEnabled());
        user.setPanelChair(form.isPanelChair() && form.getRoles().contains("OFFICER"));
        userDao.update(user);
        userDao.setRoles(id, form.getRoles());
        if (StringUtils.isNotEmpty(form.getNewPassword())) {
            userDao.updatePassword(id, new BCryptPasswordEncoder().encode(form.getNewPassword()));
        }
        redirect.addFlashAttribute("message", "User " + user.getUsername() + " saved.");
        return "redirect:/admin/users";
    }

    private void validateCommon(UserForm form, BindingResult errors) {
        if (StringUtils.isBlank(form.getFullName())) {
            errors.rejectValue("fullName", "required", "Full name is required");
        }
        if (form.getRoles() == null) {
            form.setRoles(new ArrayList<String>());
        }
        List<String> roles = form.getRoles();
        if (roles.isEmpty()) {
            errors.rejectValue("roles", "required", "Choose at least one role");
        }
        for (int i = 0; i < roles.size(); i++) {
            if (!ArrayUtils.contains(UserDao.ROLES, roles.get(i))) {
                errors.rejectValue("roles", "invalid", "Unknown role " + roles.get(i));
            }
        }
    }
}
