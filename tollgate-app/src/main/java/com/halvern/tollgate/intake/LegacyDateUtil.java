/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: LegacyDateUtil.java 1084 2017-10-04 15:26:18Z gferrand $
 */
package com.halvern.tollgate.intake;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * Parses and formats dates in the formats used by Halvern's legacy permit spreadsheet, which the
 * emailed application forms and the import file still follow: dd/MM/yyyy and dd/MM/yyyy HH:mm.
 *
 * @author G. Ferrand
 * @since 1.0
 */
public final class LegacyDateUtil {

    /** Spreadsheet date format, dd/MM/yyyy. */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    /** Spreadsheet date and time format, dd/MM/yyyy HH:mm. */
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    static {
        DATE_FORMAT.setLenient(false);
        DATE_TIME_FORMAT.setLenient(false);
    }

    private LegacyDateUtil() {
    }

    /**
     * Parse date.
     *
     * @param value the value, dd/MM/yyyy
     * @return the date, or null if blank or invalid
     */
    public static Date parseDate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return DATE_FORMAT.parse(value.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Parse date time.
     *
     * @param value the value, dd/MM/yyyy HH:mm
     * @return the date and time, or null if blank or invalid
     */
    public static Date parseDateTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return DATE_TIME_FORMAT.parse(value.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Format date.
     *
     * @param date the date
     * @return dd/MM/yyyy, or an empty string if null
     */
    public static String formatDate(Date date) {
        return date == null ? "" : DATE_FORMAT.format(date);
    }

    /**
     * Format date time.
     *
     * @param date the date and time
     * @return dd/MM/yyyy HH:mm, or an empty string if null
     */
    public static String formatDateTime(Date date) {
        return date == null ? "" : DATE_TIME_FORMAT.format(date);
    }
}
