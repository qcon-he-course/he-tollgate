package com.halvern.tollgate.intake;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UnNumberValidator implements ConstraintValidator<UnNumber, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.trim().isEmpty() || value.trim().matches("\\d{4}");
    }
}
