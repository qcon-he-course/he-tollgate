package com.halvern.tollgate.flows;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.halvern.tollgate.determination.AmendmentRequest;
import com.halvern.tollgate.determination.AmendmentService;
import com.halvern.tollgate.determination.PermitVersion;

class ExpressFlowTest extends AbstractFlowTest {

    @Autowired
    private AmendmentService amendmentService;

    @Test
    void expressIssuedAndAmended() throws Exception {
        String ref = lodge("CONSTRUCTION_PLANT", 30000, "2.50", "R4", LocalDate.now().plusDays(50));
        assertEquals("DECIDED", caseService.findByRef(ref).getState());
        PermitVersion permit = caseService.currentPermit(caseService.findByRef(ref));
        AmendmentRequest req = amendmentService.request(permit.getReference(), LocalDate.now().plusDays(55), LocalDate.now().plusDays(56), "officer1");
        amendmentService.approve(req.getId(), "officer1");
        assertEquals(2, caseService.permitVersions(caseService.findByRef(ref)).size());
    }
}
