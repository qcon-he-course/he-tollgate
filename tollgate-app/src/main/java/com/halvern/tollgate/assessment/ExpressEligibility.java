package com.halvern.tollgate.assessment;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Decides whether an assessed case can be issued without an officer (TG-220).
 *
 * @author Tom Hollis
 */
@Component
public class ExpressEligibility {

    @Autowired
    private TriggerHitRepository triggerHitRepository;

    /**
     * Express cases with no warnings are issued automatically.
     */
    public boolean isEligible(Assessment assessment, List<TriggerHit> hits, boolean hasWarnings) {
        for (TriggerHit hit : hits) {
            hit.setAssessmentId(assessment.getId());
            triggerHitRepository.save(hit);
        }
        return Tracks.EXPRESS.equals(assessment.getFinalTrack()) && !hasWarnings;
    }
}
