/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: ErrorCodes.java 1177 2018-01-29 16:40:03Z gferrand $
 */
package com.halvern.tollgate.platform;

/**
 * Error codes used by {@link TollgateException}. Codes are grouped by area and are quoted
 * to the Halvern service desk, so existing codes must not be renumbered.
 *
 * @author R. Mistry
 * @since 1.0
 */
public final class ErrorCodes {

    // 1000 - applications
    public static final int APPLICATION_NOT_FOUND = 1001;
    public static final int APPLICATION_NOT_DRAFT = 1002;
    public static final int LODGEMENT_REFUSED = 1003;
    public static final int APPLICATION_INCOMPLETE = 1004;

    // 1010 - attachments
    public static final int ATTACHMENT_LIMIT = 1011;
    public static final int ATTACHMENT_NOT_PDF = 1012;
    public static final int ATTACHMENT_TOO_LARGE = 1013;
    public static final int ATTACHMENT_STORE_FAILED = 1014;
    public static final int ATTACHMENT_NOT_FOUND = 1015;

    // 1100 - operator licence (BR-11)
    public static final int LICENCE_MISSING = 1101;
    public static final int LICENCE_NOT_YET_VALID = 1102;
    public static final int LICENCE_EXPIRES = 1103;

    // TG-231 insurance
    public static final int INSURANCE_MISSING = 1111;
    public static final int INSURANCE_NOT_COVERED = 1112;

    // 1200 - dimensions (BR-12)
    public static final int MASS_OVER_LIMIT = 1201;
    public static final int WIDTH_OVER_LIMIT = 1202;
    public static final int LENGTH_OVER_LIMIT = 1203;
    public static final int HEIGHT_OVER_LIMIT = 1204;
    public static final int HEIGHT_OVER_CLEARANCE = 1205;

    // 1300 - trips (BR-10)
    public static final int TRIP_OUTSIDE_WINDOW = 1301;
    public static final int CROSSING_TIME_MISSING = 1302;

    // 1400 - questionnaire (BR-22 to BR-24)
    public static final int QUESTION_NOT_FOUND = 1401;
    public static final int ANSWER_NOT_EDITABLE = 1402;
    public static final int ANSWER_TOO_LONG = 1403;
    public static final int REVIEW_NOTE_REQUIRED = 1404;
    public static final int ANSWER_NOT_REVIEWABLE = 1405;
    public static final int APPLICATION_NOT_LODGED = 1406;

    // 1500 - permits and closures (BR-30 to BR-36)
    public static final int PERMIT_NOT_FOUND = 1501;
    public static final int ANSWERS_NOT_ACCEPTED = 1502;
    public static final int PERMIT_DOCUMENT_FAILED = 1503;
    public static final int CLOSURE_NOT_FOUND = 1504;
    public static final int CLOSURE_DATES_INVALID = 1505;

    // 1600 - notifications (BR-42)
    public static final int MAIL_FAILED = 1601;

    private ErrorCodes() {
    }
}
