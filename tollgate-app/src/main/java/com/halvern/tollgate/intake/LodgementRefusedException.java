/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: LodgementRefusedException.java 1107 2017-10-23 10:19:02Z gferrand $
 */
package com.halvern.tollgate.intake;

import java.util.List;

import com.halvern.tollgate.platform.ErrorCodes;
import com.halvern.tollgate.platform.TollgateException;

/**
 * Thrown when an application cannot be lodged. The application stays a draft.
 * Implements BR-11 and BR-12.
 *
 * @author G. Ferrand
 * @since 1.0
 */
public class LodgementRefusedException extends TollgateException {

    private static final long serialVersionUID = 1L;

    private final List<CheckFailure> failures;

    /**
     * Creates the exception.
     *
     * @param reference the application reference
     * @param failures the checks that failed
     */
    public LodgementRefusedException(String reference, List<CheckFailure> failures) {
        super(ErrorCodes.LODGEMENT_REFUSED, "Application " + reference + " cannot be lodged: " + failures);
        this.failures = failures;
    }

    /**
     * Gets the failures.
     *
     * @return the checks that failed
     */
    public List<CheckFailure> getFailures() {
        return failures;
    }
}
