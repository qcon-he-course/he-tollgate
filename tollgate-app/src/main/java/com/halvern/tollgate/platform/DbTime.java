/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: DbTime.java 1012 2017-08-14 17:22:31Z rmistry $
 */
package com.halvern.tollgate.platform;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Timestamps are stored in UTC (BR-45). Use {@link #utc()} when reading or writing timestamp columns
 * through JDBC so the server's time zone does not leak into the data.
 *
 * @author R. Mistry
 * @since 1.0
 */
public final class DbTime {

    /** SQL expression for the current time in UTC, for use in insert and update statements. */
    public static final String NOW_UTC = "(now() AT TIME ZONE 'UTC')";

    private DbTime() {
    }

    /**
     * Utc.
     *
     * @return a new calendar in UTC for JDBC timestamp conversion
     */
    public static Calendar utc() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }
}
