package com.pianomastr64.usermanagement.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {
    private Enum<?>[] enumConstants;
    
    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        enumConstants = constraintAnnotation.enumClass().getEnumConstants();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null) {
            return true; // Allow null values
        }
        
        boolean valid = Arrays.stream(enumConstants)
            .anyMatch(enumConstant -> enumConstant.name().equalsIgnoreCase(value));
        
        if(!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Invalid value '%s'. Allowed values are: %s", value.toUpperCase(), Arrays.toString(enumConstants)))
                .addConstraintViolation();
        }
        
        return valid;
    }
}
