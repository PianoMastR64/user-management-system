package com.pianomastr64.usermanagement.user.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

//import com.pianomastr64.user.validation.NotBlankIfPresent.List;

@Documented
@Constraint(validatedBy = NotBlankIfPresentValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
//@Repeatable(List.class)
public @interface NotBlankIfPresent {
    
    String message() default "must contain at least one non-whitespace character";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
//    @Documented
//    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
//    @Retention(RUNTIME)
//    @interface List {
//        NotBlankIfPresent[] value();
//    }
}
