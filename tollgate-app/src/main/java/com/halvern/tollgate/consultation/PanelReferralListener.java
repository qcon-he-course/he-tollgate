package com.halvern.tollgate.consultation;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.assessment.Tracks;
import com.halvern.tollgate.audit.AuditEntry;
import com.halvern.tollgate.determination.CaseFile;
import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.determination.EscalationService;
import com.halvern.tollgate.platform.events.EngineerAssessmentRecorded;

/**
 * Sends a case towards the panel once an engineer has assessed it.
 *
 * <p>An engineer who refers an Engineer-track case escalates it to the Panel track. Any other assessment may be the
 * last thing a Panel-track case was waiting for, so the case is offered to the panel scheduler.
 *
 * <p>This used to be a direct call from {@link EngineerAssessmentService}; see ADR-0006.
 */
@Component
public class PanelReferralListener {

    private final CaseService caseService;
    private final EscalationService escalationService;
    private final PanelScheduler panelScheduler;

    public PanelReferralListener(CaseService caseService, EscalationService escalationService,
            PanelScheduler panelScheduler) {
        this.caseService = caseService;
        this.escalationService = escalationService;
        this.panelScheduler = panelScheduler;
    }

    @EventListener
    public void on(EngineerAssessmentRecorded event) {
        if (event.isReferralToPanel() && Tracks.ENGINEER.equals(event.track())) {
            escalationService.escalate(event.reference(), AuditEntry.SYSTEM, "Engineer referred the case to the panel");
            return;
        }
        CaseFile caseFile = caseService.findByRef(event.reference());
        panelScheduler.listIfReady(caseFile);
    }
}
