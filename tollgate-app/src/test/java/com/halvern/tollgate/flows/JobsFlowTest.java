package com.halvern.tollgate.flows;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.halvern.tollgate.consultation.LapseJob;
import com.halvern.tollgate.consultation.QueryRoundService;
import com.halvern.tollgate.permitdoc.PermitExpiryJob;
import com.halvern.tollgate.sla.SlaRecalculationJob;

class JobsFlowTest extends AbstractFlowTest {

    @Autowired
    private LapseJob lapseJob;

    @Autowired
    private SlaRecalculationJob slaRecalculationJob;

    @Autowired
    private PermitExpiryJob permitExpiryJob;

    @Autowired
    private QueryRoundService queryRoundService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void nightlyJobsRun() throws Exception {
        String ref = lodge("TRANSFORMER", 60000, "3.50", "R4", LocalDate.now().plusDays(60));
        queryRoundService.issueRound(ref, List.of("Escort company?"), "officer1");
        jdbcTemplate.update("update query_round set issued_on = ? where case_id = (select id from case_file where reference = ?)",
                Date.valueOf(LocalDate.now().minusDays(20)), ref);
        lapseJob.run();
        slaRecalculationJob.run();
        permitExpiryJob.run();
    }
}
