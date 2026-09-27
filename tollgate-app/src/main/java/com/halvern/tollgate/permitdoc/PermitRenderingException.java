package com.halvern.tollgate.permitdoc;

public class PermitRenderingException extends RuntimeException {

    public PermitRenderingException(String message) {
        super(message);
    }

    public PermitRenderingException(String message, Throwable cause) {
        super(message, cause);
    }
}
