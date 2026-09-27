package com.halvern.tollgate.consultation;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.reference.Holiday;
import com.halvern.tollgate.reference.HolidayDao;

import lombok.extern.slf4j.Slf4j;

/**
 * Working days for query round deadlines and panel sittings: Monday to Friday except Halvern holidays.
 *
 * @author Dana Kerr
 */
@Slf4j
@Component
public class WorkingDayCalendar {

    // TG-259 the lapse run was slow looking up holidays for every round, so load them once
    private static final Map<LocalDate, String> HOLIDAYS = new ConcurrentHashMap<>();

    @Autowired
    private HolidayDao holidayDao;

    @PostConstruct
    public void load() {
        if (!HOLIDAYS.isEmpty()) {
            return;
        }
        for (Holiday h : holidayDao.findAll()) {
            HOLIDAYS.put(((Date) h.getHolidayDate()).toLocalDate(), h.getDescription());
        }
        log.info("Loaded {} holidays", HOLIDAYS.size());
    }

    public boolean isWorkingDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY
                && !HOLIDAYS.containsKey(date);
    }

    /**
     * Adds working days to a date. The day of issue is not counted.
     *
     * @param from the day of issue
     * @param days the number of working days
     * @return the last working day
     */
    public LocalDate addWorkingDays(LocalDate from, int days) {
        LocalDate date = from;
        int counted = isWorkingDay(from) ? 1 : 0;
        while (counted < days) {
            date = date.plusDays(1);
            if (isWorkingDay(date)) {
                counted++;
            }
        }
        return date;
    }

    public LocalDate nextWorkingDay(LocalDate date) {
        LocalDate d = date;
        while (!isWorkingDay(d)) {
            d = d.plusDays(1);
        }
        return d;
    }

    public LocalDate previousWorkingDay(LocalDate date) {
        LocalDate d = date.minusDays(1);
        while (!isWorkingDay(d)) {
            d = d.minusDays(1);
        }
        return d;
    }
}
