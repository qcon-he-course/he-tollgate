package com.halvern.tollgate.platform.demo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.halvern.tollgate.notify.MailNotifier;
import com.halvern.tollgate.sla.clock.SlaClockService;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType;

/**
 * CR-160 the background demo cases load into an empty database.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase(type = DatabaseType.POSTGRES, provider = DatabaseProvider.ZONKY)
class DemoBulkCasesTest {

    @MockBean
    private MailNotifier mailNotifier;

    @MockBean
    private SlaClockService slaClockService;

    @Autowired
    private DemoBulkCases demoBulkCases;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void loadsCases() {
        demoBulkCases.load(LocalDate.now());
        Integer cases = jdbcTemplate.queryForObject("select count(*) from case_file", Integer.class);
        assertTrue(cases >= DemoBulkCases.TERMINAL_CASES);
    }
}
