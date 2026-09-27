package com.halvern.tollgate.platform;

import lombok.Getter;

/**
 * Unchecked business rule failure. The code is shown to the user and logged.
 *
 * @author Priya Venkat
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code) {
        super(code);
        this.code = code;
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}
