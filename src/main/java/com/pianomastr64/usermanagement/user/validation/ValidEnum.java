package com.pianomastr64.usermanagement.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom annotation to validate that a field is a valid enum value.
 * This annotation can be used on fields in DTOs or entities to ensure that the value
 * provided is one of the defined constants in the specified enum class.
 */

@Documented
@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {
    String message() default "Invalid value for enum";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    Class<? extends Enum<?>> enumClass();
}
