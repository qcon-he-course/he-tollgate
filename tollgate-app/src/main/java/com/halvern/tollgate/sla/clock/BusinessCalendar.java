package com.halvern.tollgate.sla.clock;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.halvern.tollgate.reference.HolidayDao;

/**
 * Holidays from the holiday table, read on every call so that a holiday added by an admin counts straight away.
 */
@Component
public class BusinessCalendar {

    private final HolidayDao holidayDao;

    public BusinessCalendar(HolidayDao holidayDao) {
        this.holidayDao = holidayDao;
    }

    public Set<LocalDate> holidaysBetween(LocalDate from, LocalDate to) {
        return holidayDao.findBetween(java.sql.Date.valueOf(from), java.sql.Date.valueOf(to)).stream()
                .map(h -> new java.sql.Date(h.getHolidayDate().getTime()).toLocalDate())
                .collect(Collectors.toSet());
    }
}
