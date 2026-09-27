/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: CheckFailure.java 1097 2017-10-17 11:20:35Z rmistry $
 */
package com.halvern.tollgate.intake;

/**
 * A failed lodgement check.
 * Implements BR-11 and BR-12.
 *
 * @author R. Mistry
 * @since 1.0
 */
public class CheckFailure {

    private int code;
    private String message;

    /**
     * Gets the code.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Sets the code.
     *
     * @param code the code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Creates a check failure.
     *
     * @param code the error code
     * @param message the message shown to the officer
     */
    public CheckFailure(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * To string.
     *
     * @return the code and message
     */
    public String toString() {
        return code + ": " + message;
    }
}
