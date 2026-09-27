package com.halvern.tollgate.platform.events;

import java.time.Instant;

public record EngineerAssessmentRecorded(String reference, Long caseId, String track, String recommendation,
        String engineer, Instant occurredAt) {

    public static final String REFER_TO_PANEL = "REFER_TO_PANEL";

    public boolean isReferralToPanel() {
        return REFER_TO_PANEL.equals(recommendation);
    }
}
