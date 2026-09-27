package com.halvern.tollgate.sla;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.halvern.tollgate.sla.clock.SlaClockService;

class SlaRecalculationJobTest {

    private final SlaClockService clocks = mock(SlaClockService.class);

    @ParameterizedTest(name = "run at {0} recalculates as at {1}")
    @CsvSource({
            "2023-03-14T12:00:00Z, 2023-03-14",
            "2023-07-14T12:00:00Z, 2023-07-14",
            "2023-12-29T12:00:00Z, 2023-12-29" })
    void recalculatesRunningClocksAsAtToday(Instant now, LocalDate expectedDate) {
        SlaRecalculationJob job = new SlaRecalculationJob(clocks, Clock.fixed(now, ZoneOffset.UTC));

        job.run();

        verify(clocks).recalculateRunningClocks(expectedDate);
    }
}
