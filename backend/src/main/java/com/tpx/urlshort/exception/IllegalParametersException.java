package com.tpx.urlshort.exception;

public class IllegalParametersException extends AppBaseException {
    public IllegalParametersException() {
    }

    public IllegalParametersException(String message) {
        super(message);
    }

    public IllegalParametersException(String message, Throwable cause) {
        super(message, cause);
    }
}
