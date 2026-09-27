package com.halvern.tollgate.intake;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.halvern.tollgate.platform.config.TollgateProperties;
import com.halvern.tollgate.reference.HaulierDao;
import com.halvern.tollgate.reference.HolidayDao;
import com.halvern.tollgate.reference.InsurancePolicy;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PreCheckServiceTest {

    @Mock
    private LicenceCheck licenceCheck;

    @Mock
    private DimensionCheck dimensionCheck;

    @Mock
    private HaulierDao haulierDao;

    @Mock
    private HolidayDao holidayDao;

    private PreCheckService service;

    @BeforeEach
    void setUp() {
        service = new PreCheckService(new TollgateProperties());
        ReflectionTestUtils.setField(service, "licenceCheck", licenceCheck);
        ReflectionTestUtils.setField(service, "dimensionCheck", dimensionCheck);
        ReflectionTestUtils.setField(service, "haulierDao", haulierDao);
        ReflectionTestUtils.setField(service, "holidayDao", holidayDao);
        when(holidayDao.findBetween(any(), any())).thenReturn(Collections.emptyList());
    }

    private MovementApplication application(LocalDate first, LocalDate last, String width) {
        MovementApplication app = new MovementApplication();
        app.setReference("HX-A-2019-000001");
        app.setHaulierId(1L);
        app.setFirstMoveDate(Date.valueOf(first));
        app.setLastMoveDate(Date.valueOf(last));
        app.setWidthM(new BigDecimal(width));
        app.setAttachments(new ArrayList<>());
        return app;
    }

    private void insured(LocalDate start, LocalDate end) {
        InsurancePolicy policy = new InsurancePolicy();
        policy.setPolicyNumber("HMV-1");
        policy.setStartDate(Date.valueOf(start));
        policy.setEndDate(Date.valueOf(end));
        when(haulierDao.findInsurance(anyLong())).thenReturn(policy);
    }

    @Test
    void insuranceEndingOnLastMoveDateIsFine() {
        LocalDate first = LocalDate.now().plusDays(40);
        insured(first.minusYears(1), first.plusDays(2));
        PreCheckResult result = service.check(application(first, first.plusDays(2), "2.50"));
        assertThat(result.getFailures()).isEmpty();
    }

    @Test
    void insuranceEndingEarlyIsAHardStop() {
        LocalDate first = LocalDate.now().plusDays(40);
        insured(first.minusYears(1), first.plusDays(1));
        PreCheckResult result = service.check(application(first, first.plusDays(2), "2.50"));
        assertThat(result.getFailures()).extracting(CheckFailure::getMessage).anyMatch(m -> m.contains("does not cover"));
    }

    @Test
    void wideLoadWithoutEscortPlanIsWarned() {
        LocalDate first = LocalDate.now().plusDays(40);
        insured(first.minusYears(1), first.plusYears(1));
        PreCheckResult result = service.check(application(first, first, "4.20"));
        assertThat(result.getWarnings()).containsExactly(PreCheckResult.NO_ESCORT_PLAN);
    }

    @Test
    void plentyOfNoticeIsNotShortNotice() {
        LocalDate first = LocalDate.now().plusDays(40);
        insured(first.minusYears(1), first.plusYears(1));
        assertThat(service.check(application(first, first, "2.50")).getWarnings()).isEmpty();
    }

    @Test
    void businessDaysSkipWeekends() {
        // Friday 7 June 2019 to Monday 17 June 2019
        assertThat(service.businessDaysAfter(LocalDate.of(2019, 6, 7), LocalDate.of(2019, 6, 17))).isEqualTo(6);
    }

    // CR-171 short notice warning
    @Test
    void shortNoticeWarningRaised() {
        LocalDate first = LocalDate.now().plusDays(13);
        insured(first.minusYears(1), first.plusYears(1));
        PreCheckResult result = service.check(application(first, first, "2.50"));
        assertThat(result.getWarnings()).contains(PreCheckResult.SHORT_NOTICE);
    }
}
