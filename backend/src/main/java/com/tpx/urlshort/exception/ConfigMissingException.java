package com.tpx.urlshort.exception;

public class ConfigMissingException extends AppBaseException {

    public ConfigMissingException() {
    }

    public ConfigMissingException(String message) {
        super(message);
    }

    public ConfigMissingException(String message, Throwable cause) {
        super(message, cause);
    }

}
