package com.halvern.tollgate.platform;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The application clock. Inject {@link Clock} instead of calling {@code now()} so that time can be fixed in tests.
 *
 * <p>See ADR-0008: new timestamps are UTC instants.
 */
@Configuration
public class TimeConfig {

    /** See ADR-0007: business dates are dates in London. */
    public static final ZoneId LONDON = ZoneId.of("Europe/London");

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
