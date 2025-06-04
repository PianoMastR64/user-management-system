package com.pianomastr64.usermanagement.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {
    private Enum<?>[] enumConstants;
    private String allowedValues;
    
    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        enumConstants = constraintAnnotation.enumClass().getEnumConstants();
        allowedValues = Arrays.stream(enumConstants)
            .map(e -> e.name().toLowerCase())
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null || value.isEmpty()) {
            return true; // Allow null or empty values
        }
        
        boolean valid = Arrays.stream(enumConstants)
            .anyMatch(enumConstant -> enumConstant.name().equalsIgnoreCase(value));
        
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("Invalid value '%s'. Allowed values are: %s", value, allowedValues))
                .addConstraintViolation();
        }
        
        return valid;
    }
}
