/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: DateHelper.java 1181 2018-02-05 09:27:14Z rmistry $
 */
package com.halvern.tollgate.platform;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * Date helper for screens. Halvern staff enter dates as dd/MM/yyyy.
 *
 * @author R. Mistry
 * @since 1.0
 */
public final class DateHelper {

    /** Date pattern used on all screens. */
    public static final String SCREEN_DATE_PATTERN = "dd/MM/yyyy";

    private DateHelper() {
    }

    /**
     * Parse screen date.
     *
     * @param value the value entered, dd/MM/yyyy
     * @return the date, or null if the value is blank or not a valid date
     */
    public static Date parseScreenDate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(SCREEN_DATE_PATTERN);
        format.setLenient(false);
        try {
            return format.parse(value.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Format screen date.
     *
     * @param date the date
     * @return the date as dd/MM/yyyy, or an empty string if null
     */
    public static String formatScreenDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(SCREEN_DATE_PATTERN).format(date);
    }

    /**
     * Today.
     *
     * @return today's date at midnight. Halvern servers run on UK time.
     */
    public static Date today() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
