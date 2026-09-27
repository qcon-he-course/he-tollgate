/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: ApplicationReferenceGenerator.java 1088 2017-10-06 10:40:07Z gferrand $
 */
package com.halvern.tollgate.intake;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Generates application references in the form HX-A-YYYY-NNNNNN.
 * Implements BR-09.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Component
public class ApplicationReferenceGenerator {

    private static final String NEXT_VALUE = "SELECT nextval('application_ref_seq')";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Next reference.
     *
     * @return a new application reference for the current year
     */
    public String nextReference() {
        Long next = jdbcTemplate.queryForObject(NEXT_VALUE, Long.class);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        return "HX-A-" + year + "-" + String.format("%06d", next);
    }
}
