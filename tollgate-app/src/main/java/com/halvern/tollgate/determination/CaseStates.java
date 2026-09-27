package com.halvern.tollgate.determination;

/**
 * Case states. See the case lifecycle on Confluence (TG-101).
 */
public final class CaseStates {

    public static final String DRAFT = "DRAFT";
    public static final String LODGED = "LODGED";
    public static final String IN_REVIEW = "IN_REVIEW";
    public static final String AWAITING_HAULIER = "AWAITING_HAULIER";
    public static final String AWAITING_PANEL = "AWAITING_PANEL";
    public static final String DECIDED = "DECIDED";
    public static final String WITHDRAWN = "WITHDRAWN";
    public static final String CANCELLED = "CANCELLED";
    public static final String LAPSED = "LAPSED";
    public static final String CLOSED = "CLOSED";

    private CaseStates() {
    }
}
