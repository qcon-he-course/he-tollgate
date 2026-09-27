package com.halvern.tollgate.intake;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * A UN dangerous goods number: four digits. Blank is allowed.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UnNumberValidator.class)
public @interface UnNumber {

    String message() default "UN number must be four digits";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
