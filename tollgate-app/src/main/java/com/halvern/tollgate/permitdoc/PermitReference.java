/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: PermitReference.java 1168 2018-01-19 15:26:03Z gferrand $
 */
package com.halvern.tollgate.permitdoc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A permit reference, HX-P-YYYY-NNNNN. The number runs from 1 in each calendar year without gaps.
 * Implements BR-31.
 *
 * @author G. Ferrand
 * @since 1.0
 */
public final class PermitReference {

    private static final Pattern FORMAT = Pattern.compile("^HX-P-(\\d{4})-(\\d{5})$");

    private final int year;

    private final int number;

    /**
     * Creates a permit reference.
     *
     * @param year the year
     * @param number the number within the year
     */
    public PermitReference(int year, int number) {
        if (number < 1 || number > 99999) {
            throw new IllegalArgumentException("Permit number out of range: " + number);
        }
        this.year = year;
        this.number = number;
    }

    /**
     * Parse.
     *
     * @param value the reference, HX-P-YYYY-NNNNN
     * @return the permit reference, or null if the value is not a permit reference
     */
    public static PermitReference parse(String value) {
        if (value == null) {
            return null;
        }
        Matcher m = FORMAT.matcher(value.trim());
        if (!m.matches()) {
            return null;
        }
        return new PermitReference(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
    }

    /**
     * Gets the year.
     *
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * Gets the number.
     *
     * @return the number within the year
     */
    public int getNumber() {
        return number;
    }

    /**
     * To string.
     *
     * @return the reference as printed, HX-P-YYYY-NNNNN
     */
    public String toString() {
        return "HX-P-" + year + "-" + String.format("%05d", Integer.valueOf(number));
    }

    /**
     * Equals.
     *
     * @param o the other object
     * @return true if the references are the same
     */
    public boolean equals(Object o) {
        if (!(o instanceof PermitReference)) {
            return false;
        }
        PermitReference other = (PermitReference) o;
        return year == other.year && number == other.number;
    }

    /**
     * Hash code.
     *
     * @return the hash code
     */
    public int hashCode() {
        return year * 100000 + number;
    }
}
