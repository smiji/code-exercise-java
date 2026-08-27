package com.tpx.urlshort.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String MESSAGE_FAILURE_ITEM_MISSING = "Alias not found";
    private static final String MESSAGE_FAILURE_ALIAS_ALREADY_TAKEN = "Invalid input or alias already taken";


    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleItemNotFound(ItemNotFoundException itemNotFoundException) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), MESSAGE_FAILURE_ITEM_MISSING, itemNotFoundException.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AliasAlreadyPresentException.class)
    public ResponseEntity<ErrorResponse> handleAliasAlreadyPresentException(AliasAlreadyPresentException aliasAlreadyPresentException) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), MESSAGE_FAILURE_ALIAS_ALREADY_TAKEN, aliasAlreadyPresentException.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}

