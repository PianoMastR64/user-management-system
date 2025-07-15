package com.pianomastr64.usermanagement.exception;

/**
 * Represents a single validation failure for both
 * {@code jakarta.validation.ConstraintViolationException} and
 * {@code org.springframework.web.bind.MethodArgumentNotValidException}.
 *
 * @param field         Name or path of the invalid property
 * @param message       The validation error message
 * @param rejectedValue The stringified invalid value (null-safe representation)
 * @param constraint    The constraint or code violated
 */
public record ValidationError (
    String field,
    String message,
    Object rejectedValue,
    String constraint
) {}