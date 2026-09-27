package com.halvern.tollgate.intake;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * First and last move dates in order and no more than 14 days apart, counting both (TG-232).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MovementWindowValidator.class)
public @interface ValidMovementWindow {

    String message() default "Movement dates can span at most 14 days";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
