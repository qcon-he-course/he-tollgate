package com.halvern.tollgate.sla.clock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.halvern.tollgate.sla.status.SlaCalculation;
import com.halvern.tollgate.sla.status.SlaStatus;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureEmbeddedDatabase(type = DatabaseType.POSTGRES, provider = DatabaseProvider.ZONKY)
@Sql("/fixtures/open-cases.sql")
class SlaClockRepositoryTest {

    private static final Instant NOW = Instant.parse("2023-03-14T12:00:00Z");

    @Autowired
    private SlaClockRepository clocks;

    @Autowired
    private SlaPauseRepository pauses;

    @Test
    void aSavedClockCanBeFoundByItsCase() {
        clocks.save(SlaClock.start(9001L, LocalDate.of(2023, 3, 13), 5, NOW));

        assertThat(clocks.findByCaseId(9001L)).get()
                .extracting(SlaClock::getStartedOn, SlaClock::getStatus)
                .containsExactly(LocalDate.of(2023, 3, 13), SlaStatus.GREEN);
    }

    @Test
    void aCalculationIsStoredWithTheClock() {
        SlaClock clock = SlaClock.start(9001L, LocalDate.of(2023, 3, 13), 5, NOW);
        clock.apply(new SlaCalculation(LocalDate.of(2023, 3, 17), 5, 4, 0, SlaStatus.AMBER), NOW);
        clocks.saveAndFlush(clock);

        SlaClock reloaded = clocks.findByCaseId(9001L).orElseThrow();
        assertThat(reloaded.getElapsedDays()).isEqualTo(4);
        assertThat(reloaded.getStatus()).isEqualTo(SlaStatus.AMBER);
        assertThat(reloaded.getCalculatedOn()).isEqualTo(LocalDate.of(2023, 3, 17));
    }

    @Test
    void onlyRunningClocksAreRecalculatedOvernight() {
        clocks.save(SlaClock.start(9001L, LocalDate.of(2023, 3, 13), 5, NOW));
        SlaClock stopped = SlaClock.start(9002L, LocalDate.of(2023, 3, 13), 15, NOW);
        stopped.stop(LocalDate.of(2023, 3, 14));
        clocks.save(stopped);

        assertThat(clocks.findByStoppedOnIsNull()).extracting(SlaClock::getCaseId).containsExactly(9001L);
    }

    @Test
    void pausesComeBackInTheOrderTheyStarted() {
        SlaPause second = SlaPause.begin(9002L, SlaPause.QUERY_ROUND, LocalDate.of(2023, 3, 21), NOW);
        SlaPause first = SlaPause.begin(9002L, SlaPause.QUERY_ROUND, LocalDate.of(2023, 3, 14), NOW);
        first.end(LocalDate.of(2023, 3, 16));
        pauses.save(second);
        pauses.save(first);

        assertThat(pauses.findByCaseIdOrderByStartedOnAsc(9002L))
                .extracting(SlaPause::getStartedOn, SlaPause::isOpen)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(LocalDate.of(2023, 3, 14), false),
                        org.assertj.core.groups.Tuple.tuple(LocalDate.of(2023, 3, 21), true));
    }
}
