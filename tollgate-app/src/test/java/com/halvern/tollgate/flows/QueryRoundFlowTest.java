package com.halvern.tollgate.flows;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.halvern.tollgate.consultation.QueryRoundService;

class QueryRoundFlowTest extends AbstractFlowTest {

    @Autowired
    private QueryRoundService queryRoundService;

    @Test
    void roundIssuedAndAnswered() throws Exception {
        String ref = lodge("TRANSFORMER", 60000, "3.50", "R4", LocalDate.now().plusDays(50));
        queryRoundService.issueRound(ref, List.of("Axle spacing?"), "officer1");
        assertEquals("AWAITING_HAULIER", caseService.findByRef(ref).getState());
        queryRoundService.answerRound(ref, 1, List.of("1.5 m"), "officer1");
        assertEquals("IN_REVIEW", caseService.findByRef(ref).getState());
    }
}
