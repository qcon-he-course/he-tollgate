/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: PermitReferenceGenerator.java 1169 2018-01-22 11:48:27Z gferrand $
 */
package com.halvern.tollgate.permitdoc;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Generates permit references. Must be called inside the transaction that saves the permit: the
 * counter row stays locked until that transaction ends, and a rollback gives the number back.
 * Implements BR-31.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Component
public class PermitReferenceGenerator {

    private static final Logger log = LoggerFactory.getLogger(PermitReferenceGenerator.class);

    private static final String CREATE_YEAR =
            "INSERT INTO permit_ref_counter (ref_year, last_value) VALUES (?, 0) "
            + "ON CONFLICT (ref_year) DO NOTHING";

    private static final String LOCK_COUNTER =
            "SELECT last_value FROM permit_ref_counter WHERE ref_year = ? FOR UPDATE";

    private static final String UPDATE_COUNTER =
            "UPDATE permit_ref_counter SET last_value = ? WHERE ref_year = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Next reference for the current year.
     *
     * @return the next permit reference
     */
    public synchronized PermitReference nextReference() {
        // BR-31 gap-free numbering required by audit
        int year = Calendar.getInstance().get(Calendar.YEAR);
        jdbcTemplate.update(CREATE_YEAR, year);
        Integer last = jdbcTemplate.queryForObject(LOCK_COUNTER, Integer.class, year);
        int next = last.intValue() + 1;
        jdbcTemplate.update(UPDATE_COUNTER, next, year);
        PermitReference reference = new PermitReference(year, next);
        log.info("Allocated permit reference " + reference);
        return reference;
    }
}
