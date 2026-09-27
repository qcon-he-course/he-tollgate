/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: UserForm.java 1047 2017-09-12 11:31:58Z koduya $
 */
package com.halvern.tollgate.reference;

import java.util.List;

/**
 * Form backing the user admin screen.
 * Implements BR-40.
 *
 * @author K. Oduya
 * @since 1.0
 */
public class UserForm {

    private String username;
    private String fullName;
    private String email;
    private boolean enabled;
    private List<String> roles;
    private String newPassword;

    // TG-171
    private boolean panelChair;

    public boolean isPanelChair() {
        return panelChair;
    }

    public void setPanelChair(boolean panelChair) {
        this.panelChair = panelChair;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the full name.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name.
     *
     * @param fullName the full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email the email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the enabled.
     *
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled.
     *
     * @param enabled the enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the roles.
     *
     * @return the roles
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * Sets the roles.
     *
     * @param roles the roles
     */
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /**
     * Gets the new password.
     *
     * @return the new password
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Sets the new password.
     *
     * @param newPassword the new password
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
