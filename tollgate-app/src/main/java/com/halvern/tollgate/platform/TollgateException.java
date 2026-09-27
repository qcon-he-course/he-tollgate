/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: TollgateException.java 1079 2017-10-02 09:12:55Z gferrand $
 */
package com.halvern.tollgate.platform;

/**
 * Checked exception for business errors in Tollgate. Each error carries a code from {@link ErrorCodes}.
 *
 * @author G. Ferrand
 * @since 1.0
 */
public class TollgateException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int errorCode;

    /**
     * Creates a Tollgate exception.
     *
     * @param errorCode the error code
     * @param message the message
     */
    public TollgateException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Creates a Tollgate exception with a cause.
     *
     * @param errorCode the error code
     * @param message the message
     * @param cause the cause
     */
    public TollgateException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Gets the error code.
     *
     * @return the error code
     */
    public int getErrorCode() {
        return errorCode;
    }
}
