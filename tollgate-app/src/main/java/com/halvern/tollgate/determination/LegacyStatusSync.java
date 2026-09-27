package com.halvern.tollgate.determination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Keeps movement_application.status in step with the case state, for the screens and reports that
 * still read the old status (TG-104). Old statuses: DRAFT, SUBMITTED, APPROVED, REJECTED.
 *
 * @author Priya Venkat
 */
@Slf4j
@Component
public class LegacyStatusSync {

    private static final String UPDATE_STATUS = "UPDATE movement_application SET status = ? WHERE id = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void sync(CaseFile caseFile) {
        String legacy = null;
        switch (caseFile.getState()) {
            case CaseStates.LODGED:
            case CaseStates.IN_REVIEW:
            case CaseStates.AWAITING_HAULIER:
            case CaseStates.AWAITING_PANEL:
                legacy = "SUBMITTED";
                break;
            case CaseStates.DECIDED:
                legacy = Outcomes.REFUSED.equals(caseFile.getOutcome()) ? "REJECTED" : "APPROVED";
                break;
            default:
                break;
        }
        if (legacy == null) {
            log.debug("No legacy status for case {} in state {}", caseFile.getReference(), caseFile.getState());
            return;
        }
        jdbcTemplate.update(UPDATE_STATUS, legacy, caseFile.getApplicationId());
    }
}
