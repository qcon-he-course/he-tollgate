package com.halvern.tollgate.intake;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.halvern.tollgate.platform.ErrorCodes;
import com.halvern.tollgate.platform.config.TollgateProperties;
import com.halvern.tollgate.reference.Holiday;
import com.halvern.tollgate.reference.HolidayDao;
import com.halvern.tollgate.reference.HaulierDao;
import com.halvern.tollgate.reference.InsurancePolicy;

import lombok.extern.slf4j.Slf4j;

/**
 * Lodgement pre-checks: hard stops that keep the application a draft, and warnings recorded on the
 * case (TG-231). Wraps the original licence and dimension checks.
 *
 * @author Dana Kerr
 */
@Slf4j
@Service
public class PreCheckService {

    private final int shortNoticeDays;

    @Autowired
    private LicenceCheck licenceCheck;

    @Autowired
    private DimensionCheck dimensionCheck;

    @Autowired
    private HaulierDao haulierDao;

    @Autowired
    private HolidayDao holidayDao;

    public PreCheckService(TollgateProperties properties) {
        // TG-236 read once instead of on every lodgement
        this.shortNoticeDays = properties.getIntake().getShortNoticeDays();
    }

    public PreCheckResult check(MovementApplication app) {
        PreCheckResult result = new PreCheckResult();
        licenceCheck.check(app, result.getFailures());
        checkInsurance(app, result);
        dimensionCheck.check(app, result.getFailures());

        LocalDate today = LocalDate.now();
        LocalDate firstMove = toLocalDate(app.getFirstMoveDate());
        int notice = businessDaysAfter(today, firstMove);
        if (notice < shortNoticeDays) {
            result.getWarnings().add(PreCheckResult.SHORT_NOTICE);
        }

        // TG-238 escort plan expected for loads over 4 m wide
        if (app.getWidthM().compareTo(new BigDecimal("4.00")) > 0 && !hasEscortPlan(app)) {
            result.getWarnings().add(PreCheckResult.NO_ESCORT_PLAN);
        }
        log.info("Pre-checks for {}: {} hard stops, warnings {}", app.getReference(), result.getFailures().size(),
                result.getWarnings());
        return result;
    }

    private void checkInsurance(MovementApplication app, PreCheckResult result) {
        InsurancePolicy policy = haulierDao.findInsurance(app.getHaulierId());
        if (policy == null) {
            result.getFailures().add(new CheckFailure(ErrorCodes.INSURANCE_MISSING,
                    "No insurance policy is on file for " + app.getHaulierName()));
            return;
        }
        LocalDate start = toLocalDate(policy.getStartDate());
        LocalDate end = toLocalDate(policy.getEndDate());
        LocalDate first = toLocalDate(app.getFirstMoveDate());
        LocalDate last = toLocalDate(app.getLastMoveDate());
        if (start.isAfter(first) || end.isBefore(last)) {
            result.getFailures().add(new CheckFailure(ErrorCodes.INSURANCE_NOT_COVERED,
                    "Insurance policy " + policy.getPolicyNumber() + " does not cover every day of the move"));
        }
    }

    /**
     * Business days after from, up to and including to.
     */
    int businessDaysAfter(LocalDate from, LocalDate to) {
        Set<LocalDate> holidays = new HashSet<>();
        for (Holiday h : holidayDao.findBetween(Date.valueOf(from), Date.valueOf(to))) {
            holidays.add(toLocalDate(h.getHolidayDate()));
        }
        int days = 0;
        for (LocalDate d = from.plusDays(1); !d.isAfter(to); d = d.plusDays(1)) {
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY && !holidays.contains(d)) {
                days++;
            }
        }
        return days;
    }

    private boolean hasEscortPlan(MovementApplication app) {
        if (app.getAttachments() == null) {
            return false;
        }
        return app.getAttachments().stream().anyMatch(a -> "ESCORT_PLAN".equals(a.getAttachmentType()));
    }

    private static LocalDate toLocalDate(java.util.Date date) {
        if (date instanceof Date) {
            return ((Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
