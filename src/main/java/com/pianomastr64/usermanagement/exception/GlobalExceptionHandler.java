package com.pianomastr64.usermanagement.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handle(ConstraintViolationException exception) {
        Map<String, String> errors = new HashMap<>();
        
        exception.getConstraintViolations().forEach(violation ->
            errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handle(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        
        exception.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));
        
        return ResponseEntity.badRequest().body(errors);
    }
    
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, String>> handle(DuplicateKeyException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", exception.getMessage()));
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handle() {
        return ResponseEntity.badRequest().body("Malformed JSON request");
    }
    
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, String>> handle(DuplicateEmailException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", exception.getMessage()));
    }
    
    // Catch all for any other exceptions for development purposes
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception exception) throws Exception {
        // Don't handle Spring Security exceptions here because this method catches them first,
        // and they are handled by the SecurityConfig class
        if(exception.getClass().getPackageName().startsWith("org.springframework.security")) {
            throw exception;
        }
        
        return ResponseEntity.internalServerError().body("An unexpected error occurred");
    }
}
