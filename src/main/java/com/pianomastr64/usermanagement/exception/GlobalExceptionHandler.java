package com.pianomastr64.usermanagement.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    @Override
    @Nullable
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
            .sorted(Comparator.comparing(fieldError -> fieldError.getField().toLowerCase()))
            .map(fieldError -> new ValidationError(
                fieldError.getField(),
                fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "invalid",
                fieldError.getRejectedValue() != null ? fieldError.getRejectedValue() : "null",
                fieldError.getCode() != null ? fieldError.getCode() : "unknown"
            ))
            .toList();
        
        ProblemDetail exBody = ex.getBody();
        exBody.setProperty("errors", errors);
        exBody.setDetail("Validation failed. One or more fields are invalid.");
        
        //No need to set body here since it's already set in the exception
        return handleExceptionInternal(ex, null, headers, status, request);
    }
    
    
    @ExceptionHandler(ConstraintViolationException.class)
    @Nullable
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<ValidationError> errors = ex.getConstraintViolations().stream()
            .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString().toLowerCase()))
            .map(violation -> new ValidationError(
                violation.getPropertyPath().toString(),
                violation.getMessage(),
                violation.getInvalidValue(),
                violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName()
            ))
            .toList();
        
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail body = createProblemDetail(
            ex, status, "Validation failed. One or more properties are invalid.",
            null, null, request);
        body.setProperty("errors", errors);
        
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }
    
    @ExceptionHandler(DuplicateKeyException.class)
    @Nullable
    public ResponseEntity<Object> handleDuplicateKey(DuplicateKeyException ex, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail body = createProblemDetail(
            ex, status, "A resource with the same key already exists.",
            null, null, request);
        
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }
    
    @ExceptionHandler(DuplicateEmailException.class)
    @Nullable
    public ResponseEntity<Object> handleDuplicateEmail(DuplicateEmailException ex, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail body = createProblemDetail(
            ex, status, ex.getMessage(),
            null, null, request);
        
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }
    
    // Catch all for any other exceptions for development purposes
    @ExceptionHandler(Exception.class)
    @Nullable
    public ResponseEntity<Object> handleOtherException(Exception ex, WebRequest request) throws Exception {
        // Don't handle Spring Security exceptions here because this method catches them first,
        // and they are handled by the SecurityConfig class
        if(ex.getClass().getPackageName().startsWith("org.springframework.security")) {
            throw ex;
        }
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemDetail body = createProblemDetail(
            ex, status, "An unexpected error occurred.",
            null, null, request);
        
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }
}