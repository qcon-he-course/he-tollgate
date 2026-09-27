package com.halvern.tollgate.flows;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.halvern.tollgate.consultation.EngineerAssessmentService;
import com.halvern.tollgate.determination.PanelDecisionService;

class EngineerFlowTest extends AbstractFlowTest {

    @Autowired
    private EngineerAssessmentService engineerAssessmentService;

    @Autowired
    private PanelDecisionService panelDecisionService;

    @Test
    void engineerRefersToPanel() throws Exception {
        String ref = lodge("MOBILE_CRANE", 120000, "4.50", "R4", LocalDate.now().plusDays(50));
        engineerAssessmentService.assignEngineer(ref, "engineer1", "officer1");
        engineerAssessmentService.record(ref, "REFER_TO_PANEL", "Too close to rating", null, "engineer1");
        assertEquals("PANEL", caseService.findByRef(ref).getTrack());
    }

    @Test
    void engineerSatisfactory() throws Exception {
        String ref = lodge("MOBILE_CRANE", 120000, "4.50", "R4", LocalDate.now().plusDays(52));
        engineerAssessmentService.assignEngineer(ref, "engineer1", "officer1");
        engineerAssessmentService.record(ref, "SATISFACTORY", "Fine", null, "engineer1");
        assertEquals("ENGINEER", caseService.findByRef(ref).getTrack());
    }
}
