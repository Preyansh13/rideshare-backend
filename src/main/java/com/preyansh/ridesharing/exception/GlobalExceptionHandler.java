package com.preyansh.ridesharing.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.ResponseEntity;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        var error = ex.getBindingResult()
                .getFieldErrors()
                .get(0);

        return ResponseEntity
                .badRequest()
                .body(error.getField() + ": " + error.getDefaultMessage());
    }
}
