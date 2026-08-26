package com.tpx.urlshort.exception;

public class AliasAlreadyPresentException extends AppBaseException {
    public AliasAlreadyPresentException() {
    }

    public AliasAlreadyPresentException(String message) {
        super(message);
    }

    public AliasAlreadyPresentException(String message, Throwable cause) {
        super(message, cause);
    }
}
