package com.tpx.urlshort.exception;

public class AppBaseException extends RuntimeException {
    public AppBaseException() {
    }

    public AppBaseException(String message) {
        super(message);
    }

    public AppBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
