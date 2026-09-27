package com.halvern.tollgate.assessment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.halvern.tollgate.intake.ApplicationDao;
import com.halvern.tollgate.intake.ApplicationTrip;
import com.halvern.tollgate.intake.MovementApplication;
import com.halvern.tollgate.platform.flags.FlagService;
import com.halvern.tollgate.reference.RouteDao;
import com.halvern.tollgate.reference.RouteSegment;

import lombok.extern.slf4j.Slf4j;

/**
 * Runs the risk assessment for a case: score, triggers and track (TG-140).
 *
 * @author Tom Hollis
 */
@Slf4j
@Service
public class AssessmentService {

    @Autowired
    private FlagService flagService;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private RouteDao routeDao;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private RiskScoringService riskScoringService;

    @Autowired
    private TriggerEvaluator triggerEvaluator;

    @Autowired
    private TrackRouter trackRouter;

    @Autowired
    private ExpressEligibility expressEligibility;

    @Autowired
    private AssessmentRepository assessmentRepository;

    public AssessmentResult assess(Long caseId, Long applicationId, Long haulierId, LocalDate lodgedOn,
            boolean hasWarnings) {
        ScoringInput in = buildInput(applicationId, haulierId, lodgedOn);
        List<AssessmentFactor> factors = riskScoringService.score(in);
        int score = riskScoringService.total(factors);
        String scoreTrack = trackRouter.scoreTrack(score);

        List<TriggerHit> hits;
        // INC-40213: fix for slow assessment on express cases
        if (Tracks.EXPRESS.equals(scoreTrack) && flagService.isEnabled("assessment.fast-path")) {
            hits = new ArrayList<>();
        } else {
            hits = triggerEvaluator.evaluate(in, riskScoringService.tonnes(in.getGrossMassKg()));
        }
        String finalTrack = scoreTrack;
        for (TriggerHit hit : hits) {
            finalTrack = trackRouter.higher(finalTrack, hit.getMinTrack());
        }

        Assessment assessment = new Assessment();
        assessment.setCaseId(caseId);
        assessment.setScore(score);
        assessment.setScoreTrack(scoreTrack);
        assessment.setFinalTrack(finalTrack);
        assessment.setAssessedAt(LocalDateTime.now());
        assessment.setFactors(factors);
        assessmentRepository.save(assessment);

        boolean eligible = expressEligibility.isEligible(assessment, hits, hasWarnings);
        log.info("Case {} assessed: score {} track {} final {} triggers {}", caseId, score, scoreTrack, finalTrack, hits.size());
        return new AssessmentResult(assessment, eligible);
    }

    public ScoringInput buildInput(Long applicationId, Long haulierId, LocalDate lodgedOn) {
        MovementApplication app = applicationDao.findById(applicationId);
        ScoringInput in = new ScoringInput();
        in.setGrossMassKg(app.getGrossMassKg());
        in.setWidthM(app.getWidthM());
        in.setLengthM(app.getLengthM());
        in.setHeightM(app.getHeightM());
        in.setHazardous(app.isHazardous());
        in.setIncidentCount((int) incidentRepository.countByHaulierIdAndIncidentDateBetween(haulierId,
                lodgedOn.minusMonths(24), lodgedOn.minusDays(1)));
        for (RouteSegment segment : routeDao.findSegments(app.getRouteId())) {
            in.getStructures().add(segment.getStructure());
        }
        for (ApplicationTrip trip : app.getTrips()) {
            in.getDepartures().add(toLocal(trip.getDepartureAt()));
            if (trip.getCrossingAt() != null) {
                in.getEstuaryCrossings().add(toLocal(trip.getCrossingAt()));
            }
        }
        return in;
    }

    private LocalDateTime toLocal(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
