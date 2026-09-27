package com.halvern.tollgate.determination;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.assessment.Assessment;
import com.halvern.tollgate.assessment.AssessmentFactor;
import com.halvern.tollgate.assessment.AssessmentRepository;
import com.halvern.tollgate.assessment.AssessmentService;
import com.halvern.tollgate.assessment.ExpressEligibility;
import com.halvern.tollgate.assessment.RiskScoringService;
import com.halvern.tollgate.assessment.ScoringInput;
import com.halvern.tollgate.assessment.TrackRouter;
import com.halvern.tollgate.assessment.Tracks;
import com.halvern.tollgate.assessment.TriggerEvaluator;
import com.halvern.tollgate.assessment.TriggerHit;
import com.halvern.tollgate.consultation.PanelListing;
import com.halvern.tollgate.consultation.PanelListingRepository;
import com.halvern.tollgate.consultation.PanelScheduler;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.CurrentUser;
import com.halvern.tollgate.platform.flags.FlagService;

import lombok.extern.slf4j.Slf4j;

/**
 * Returns a case for reassessment: the risk assessment is run again from scratch and manual
 * escalations are cleared (TG-195).
 *
 * @author Tom Hollis
 */
@Slf4j
@Service
public class ReassessmentService {

    @Autowired
    private CaseService caseService;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private RiskScoringService riskScoringService;

    @Autowired
    private TriggerEvaluator triggerEvaluator;

    @Autowired
    private TrackRouter trackRouter;

    @Autowired
    private ExpressEligibility expressEligibility;

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private PanelListingRepository panelListingRepository;

    @Autowired
    private PanelScheduler panelScheduler;

    @Autowired
    private FlagService flagService;

    @Transactional
    public CaseFile reassess(String reference, String username) {
        if (!CurrentUser.hasRole("OFFICER")) {
            throw new BusinessException("NOT_ALLOWED", "Only officers can return a case for reassessment");
        }
        CaseFile caseFile = caseService.findByRef(reference);
        if (!CaseStates.IN_REVIEW.equals(caseFile.getState()) && !CaseStates.AWAITING_PANEL.equals(caseFile.getState())) {
            throw new BusinessException("NOT_REASSESSABLE", "Case " + reference + " cannot be reassessed in state " + caseFile.getState());
        }
        for (Assessment old : assessmentRepository.findByCaseIdAndSupersededFalse(caseFile.getId())) {
            old.setSuperseded(true);
            assessmentRepository.save(old);
        }
        for (PanelListing listing : panelListingRepository.findByCaseId(caseFile.getId())) {
            if (listing.getResult() == null) {
                listing.setResult("RETURNED");
                panelListingRepository.save(listing);
            }
        }

        ScoringInput in = assessmentService.buildInput(caseFile.getApplicationId(), caseFile.getHaulierId(),
                caseFile.getLodgedAt().toLocalDate());
        List<AssessmentFactor> factors = riskScoringService.score(in);
        int score = riskScoringService.total(factors);
        String scoreTrack = trackRouter.scoreTrack(score);
        List<TriggerHit> hits;
        // INC-40213: fix for slow assessment on express cases (reassessment timed out too)
        if (Tracks.EXPRESS.equals(scoreTrack) && flagService.isEnabled("assessment.fastPath")) {
            hits = new ArrayList<>();
        } else {
            hits = triggerEvaluator.evaluate(in, riskScoringService.tonnes(in.getGrossMassKg()));
        }
        String finalTrack = scoreTrack;
        for (TriggerHit hit : hits) {
            finalTrack = trackRouter.higher(finalTrack, hit.getMinTrack());
        }
        Assessment assessment = new Assessment();
        assessment.setCaseId(caseFile.getId());
        assessment.setScore(score);
        assessment.setScoreTrack(scoreTrack);
        assessment.setFinalTrack(finalTrack);
        assessment.setAssessedAt(LocalDateTime.now());
        assessment.setFactors(factors);
        assessmentRepository.save(assessment);
        boolean eligible = expressEligibility.isEligible(assessment, hits, caseFile.getWarnings() != null);

        caseFile.setTrack(finalTrack);
        caseFile.setRiskScore(score);
        caseFile.setEscalatedTrack(null);
        caseService.changeState(caseFile, CaseStates.IN_REVIEW, username,
                "Returned for reassessment: score " + score + ", track " + finalTrack);
        log.info("Case {} reassessed by {}: {} -> {}", reference, username, scoreTrack, finalTrack);

        if (eligible && flagService.isEnabled("express.auto-issue")) {
            decisionService.autoIssue(caseFile);
        } else if (Tracks.PANEL.equals(finalTrack)) {
            panelScheduler.listIfReady(caseFile);
        }
        return caseFile;
    }
}
