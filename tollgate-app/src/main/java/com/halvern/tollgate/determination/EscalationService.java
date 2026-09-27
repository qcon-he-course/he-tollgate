package com.halvern.tollgate.determination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.assessment.TrackRouter;
import com.halvern.tollgate.assessment.Tracks;
import com.halvern.tollgate.audit.AuditService;
import com.halvern.tollgate.consultation.PanelScheduler;
import com.halvern.tollgate.platform.BusinessException;

import lombok.extern.slf4j.Slf4j;

/**
 * Moves a case up one track, never down (TG-190).
 *
 * @author Priya Venkat
 */
@Slf4j
@Service
public class EscalationService {

    @Autowired
    private CaseService caseService;

    @Autowired
    private TrackRouter trackRouter;

    @Autowired
    private AuditService auditService;

    @Autowired
    private PanelScheduler panelScheduler;

    @Transactional
    public CaseFile escalate(String reference, String actor, String reason) {
        CaseFile caseFile = caseService.findByRef(reference);
        if (!CaseStates.IN_REVIEW.equals(caseFile.getState()) && !CaseStates.AWAITING_HAULIER.equals(caseFile.getState())) {
            throw new BusinessException("NOT_ESCALATABLE", "Case " + reference + " cannot be escalated in state " + caseFile.getState());
        }
        String from = caseFile.getTrack();
        if (Tracks.PANEL.equals(from)) {
            throw new BusinessException("ALREADY_PANEL", "Case " + reference + " is already on the Panel track");
        }
        String to = trackRouter.next(from);
        caseFile.setTrack(to);
        caseFile.setEscalatedTrack(to);
        caseService.save(caseFile);
        auditService.recordCase(reference, caseFile.getState(), caseFile.getState(), actor,
                "Escalated from " + from + " to " + to + (reason == null ? "" : ": " + reason));
        log.info("Case {} escalated {} -> {} by {}", reference, from, to, actor);
        if (Tracks.PANEL.equals(to)) {
            panelScheduler.listIfReady(caseFile);
        }
        return caseFile;
    }
}
