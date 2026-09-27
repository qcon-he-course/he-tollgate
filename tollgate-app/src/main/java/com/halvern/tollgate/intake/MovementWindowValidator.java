package com.halvern.tollgate.intake;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MovementWindowValidator implements ConstraintValidator<ValidMovementWindow, ApplicationForm> {

    @Override
    public boolean isValid(ApplicationForm form, ConstraintValidatorContext context) {
        Date first = LegacyDateUtil.parseDate(form.getFirstMoveDate());
        Date last = LegacyDateUtil.parseDate(form.getLastMoveDate());
        if (first == null || last == null) {
            return true;
        }
        LocalDate from = first.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate to = last.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String message = null;
        if (to.isBefore(from)) {
            message = "Last move date is before the first";
        } else if (to.isAfter(from.plusDays(13))) {
            message = "Movement dates can span at most 14 days";
        }
        if (message == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addPropertyNode("lastMoveDate").addConstraintViolation();
        return false;
    }
}
