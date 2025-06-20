package com.pianomastr64.usermanagement.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handle(ConstraintViolationException exception) {
        System.err.println("ConstraintViolationException occurred:");
        System.err.println("\t" + exception.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        
        exception.getConstraintViolations().forEach(violation ->
            errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handle(MethodArgumentNotValidException exception) {
        System.err.println("MethodArgumentNotValidException occurred:");
        System.err.println("\t" + exception.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        
        exception.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));
        
        return ResponseEntity.badRequest().body(errors);
    }
    
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, String>> handle(DuplicateKeyException exception) {
        System.err.println("DuplicateKeyException occurred:");
        System.err.println("\t" + exception.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", exception.getMessage()));
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handle(HttpMessageNotReadableException exception) {
        System.err.println("HttpMessageNotReadableException occurred");
        System.err.println(exception.getMessage());
        return ResponseEntity.badRequest().body("Malformed JSON request");
    }
    
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, String>> handle(DuplicateEmailException exception) {
        System.err.println("DuplicateEmailException occurred:");
        System.err.println("\t" + exception.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", exception.getMessage()));
    }
    
    // Catch all for any other exceptions for development purposes
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception exception) throws Exception {
        // Don't handle Spring Security exceptions here because this method otherwise catches them first
        if(exception instanceof AccessDeniedException ||
            exception instanceof AuthenticationException
        ) {
            throw exception;
        }
        
        System.err.println("Unhandled " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
        exception.printStackTrace();
        return ResponseEntity.internalServerError().body("An unexpected error occurred");
    }
}
