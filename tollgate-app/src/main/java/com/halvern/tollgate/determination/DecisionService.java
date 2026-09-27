package com.halvern.tollgate.determination;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.assessment.AssessmentService;
import com.halvern.tollgate.assessment.MandatoryConditionGenerator;
import com.halvern.tollgate.assessment.RequiredCondition;
import com.halvern.tollgate.assessment.Tracks;
import com.halvern.tollgate.audit.AuditEntry;
import com.halvern.tollgate.audit.AuditService;
import com.halvern.tollgate.consultation.EngineerAssessment;
import com.halvern.tollgate.consultation.EngineerAssessmentRepository;
import com.halvern.tollgate.intake.ApplicationDao;
import com.halvern.tollgate.intake.MovementApplication;
import com.halvern.tollgate.permitdoc.LegacyPermitPdfWriter;
import com.halvern.tollgate.permitdoc.Permit;
import com.halvern.tollgate.permitdoc.PermitDao;
import com.halvern.tollgate.permitdoc.PermitDocumentModel;
import com.halvern.tollgate.permitdoc.PermitRenderer;
import com.halvern.tollgate.permitdoc.PermitReference;
import com.halvern.tollgate.permitdoc.PermitRenderingException;
import com.halvern.tollgate.permitdoc.PermitVersionRef;
import com.halvern.tollgate.permitdoc.PermitReferenceGenerator;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.CurrentUser;
import com.halvern.tollgate.platform.TimeConfig;
import com.halvern.tollgate.platform.flags.FlagService;
import com.halvern.tollgate.platform.events.CaseDecided;
import com.halvern.tollgate.reference.RouteDao;
import com.halvern.tollgate.reference.RouteSegment;

import lombok.extern.slf4j.Slf4j;

/**
 * Decisions and the permit that an issue decision creates (TG-160).
 *
 * @author Priya Venkat
 */
@Slf4j
@Service
public class DecisionService {

    @Autowired
    private CaseFileRepository caseFileRepository;

    @Autowired
    private DecisionRepository decisionRepository;

    @Autowired
    private PermitVersionRepository permitVersionRepository;

    @Autowired
    private PermitConditionRepository permitConditionRepository;

    @Autowired
    private EngineerAssessmentRepository engineerAssessmentRepository;

    @Autowired
    private PermitReferenceGenerator permitReferenceGenerator;

    @Autowired
    private PermitRenderer permitRenderer;

    @Autowired
    private LegacyPermitPdfWriter legacyPermitPdfWriter;

    @Autowired
    private FlagService flagService;

    @Autowired
    private PermitDao permitDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private RouteDao routeDao;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private MandatoryConditionGenerator mandatoryConditionGenerator;

    @Autowired
    private LegacyStatusSync legacyStatusSync;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ApplicationEventPublisher events;

    @Autowired
    private Clock clock;

    /**
     * Officer decision on an Express, Desk or Engineer case.
     */
    @PreAuthorize("hasRole('OFFICER')")
    public Decision decide(String reference, boolean issue, String reason, List<PermitCondition> added, String username) {
        CaseFile caseFile = caseFileRepository.findByReference(reference).get();
        if (!CaseStates.IN_REVIEW.equals(caseFile.getState())) {
            throw new BusinessException("NOT_IN_REVIEW", "Case " + reference + " is not in review");
        }
        if (Tracks.PANEL.equals(caseFile.getTrack())) {
            throw new BusinessException("PANEL_CASE", "Panel cases are decided by the panel chair");
        }
        return decideAndIssue(caseFile, issue, reason, added, username, false);
    }

    /**
     * CR-147 refuse from the case page with just a reason.
     */
    @PreAuthorize("hasRole('OFFICER')")
    public Decision refuse(String reference, String reason) {
        CaseFile caseFile = caseFileRepository.findByReference(reference)
                .orElseThrow(() -> new BusinessException("CASE_NOT_FOUND", "No case " + reference));
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("REASON_REQUIRED", "A refusal needs a reason");
        }
        if (!CaseStates.IN_REVIEW.equals(caseFile.getState())) {
            throw new BusinessException("NOT_IN_REVIEW", "Case " + reference + " is not in review");
        }
        if (Tracks.PANEL.equals(caseFile.getTrack())) {
            throw new BusinessException("PANEL_CASE", "Panel cases are decided by the panel chair");
        }
        return decideAndIssue(caseFile, false, reason.trim(), new ArrayList<>(), CurrentUser.name(), false);
    }

    /**
     * Panel chair decision. The chair check is done by PanelDecisionService.
     */
    public Decision decideForPanel(CaseFile caseFile, boolean issue, String reason, List<PermitCondition> added,
            String username) {
        return decideAndIssue(caseFile, issue, reason, added, username, true);
    }

    /**
     * TG-220 Express cases with no warnings are issued without an officer.
     */
    public Decision autoIssue(CaseFile caseFile) {
        return decideAndIssue(caseFile, true, null, new ArrayList<>(), AuditEntry.SYSTEM, false);
    }

    private Decision decideAndIssue(CaseFile caseFile, boolean issue, String reason, List<PermitCondition> added,
            String username, boolean panel) {
        if (!issue && (reason == null || reason.trim().isEmpty())) {
            throw new BusinessException("REASON_REQUIRED", "A refusal needs a reason");
        }
        checkEngineerAssessment(caseFile, issue, reason);
        Decision decision = recordDecision(caseFile, issue, reason, added == null ? new ArrayList<>() : added,
                username, panel);
        if (issue) {
            // TG-164 print the permit once the decision is saved
            PermitVersion permit = permitVersionRepository.findByCaseIdOrderByVersionAsc(caseFile.getId()).get(0);
            renderDocument(caseFile, permit);
        }
        publishDecided(caseFile, decision);
        return decision;
    }

    @Transactional
    public Decision recordDecision(CaseFile caseFile, boolean issue, String reason, List<PermitCondition> added,
            String username, boolean panel) {
        List<PermitCondition> conditions = new ArrayList<>();
        if (issue) {
            MovementApplication app = applicationDao.findById(caseFile.getApplicationId());
            for (RequiredCondition rc : mandatoryConditionGenerator.generate(assessmentService.buildInput(
                    caseFile.getApplicationId(), caseFile.getHaulierId(), caseFile.getLodgedAt().toLocalDate()))) {
                PermitCondition c = new PermitCondition();
                c.setConditionType(rc.getConditionType());
                c.setDetail(rc.getDetail());
                c.setEscortVehicles(rc.getEscortVehicles());
                c.setStructureCode(rc.getStructureCode());
                c.setSource(PermitCondition.SOURCE_MANDATORY);
                conditions.add(c);
            }
            for (PermitCondition c : added) {
                c.setSource(panel ? PermitCondition.SOURCE_PANEL : PermitCondition.SOURCE_OFFICER);
                conditions.add(c);
            }

            PermitReference ref = permitReferenceGenerator.nextReference();
            PermitVersion permit = new PermitVersion();
            permit.setReference(ref.toString());
            permit.setApplicationId(caseFile.getApplicationId());
            permit.setCaseId(caseFile.getId());
            permit.setVersion(1);
            permit.setStatus(Permit.ACTIVE);
            permit.setWindowStart(((Date) app.getFirstMoveDate()).toLocalDate());
            permit.setWindowEnd(((Date) app.getLastMoveDate()).toLocalDate());
            permit.setIssuedBy(username);
            permit.setIssuedAt(LocalDateTime.now());
            permitVersionRepository.save(permit);
            for (PermitCondition c : conditions) {
                c.setCaseId(caseFile.getId());
                c.setPermitId(permit.getId());
                permitConditionRepository.save(c);
            }
            auditService.recordPermit(permit.getReference(), null, Permit.ACTIVE, username, null);
        }

        Decision decision = new Decision();
        decision.setCaseId(caseFile.getId());
        decision.setOutcome(!issue ? Outcomes.REFUSED : conditions.isEmpty() ? Outcomes.ISSUED : Outcomes.ISSUED_WITH_CONDITIONS);
        decision.setReason(reason);
        decision.setDecidedBy(username);
        decision.setDecidedAt(LocalDateTime.now());
        decision.setPanel(panel);
        decisionRepository.save(decision);

        String oldState = caseFile.getState();
        caseFile.setState(CaseStates.DECIDED);
        caseFile.setOutcome(decision.getOutcome());
        caseFile.setDecidedAt(decision.getDecidedAt());
        caseFile.setUpdatedAt(LocalDateTime.now());
        caseFileRepository.save(caseFile);
        legacyStatusSync.sync(caseFile);
        auditService.recordCase(caseFile.getReference(), oldState, CaseStates.DECIDED, username, decision.getOutcome()
                + (reason == null ? "" : ": " + reason));
        log.info("Case {} decided {} by {}", caseFile.getReference(), decision.getOutcome(), username);
        return decision;
    }

    /**
     * Print the permit for a version and store the document. See ADR-0010.
     */
    public void renderDocument(CaseFile caseFile, PermitVersion permit) {
        MovementApplication app = applicationDao.findById(caseFile.getApplicationId());
        List<PermitDocumentModel.Condition> conditions = permitConditionRepository.findByPermitId(permit.getId()).stream()
                .map(c -> new PermitDocumentModel.Condition(c.getConditionType(), c.getDetail()))
                .collect(Collectors.toList());
        List<PermitDocumentModel.Trip> trips = app.getTrips().stream()
                .map(t -> new PermitDocumentModel.Trip(t.getTripNo(), toLocal(t.getDepartureAt()), toLocal(t.getCrossingAt())))
                .collect(Collectors.toList());
        String structures = routeDao.findSegments(app.getRouteId()).stream()
                .map(RouteSegment::getStructure)
                .map(s -> s.getCode())
                .collect(Collectors.joining(", "));
        PermitDocumentModel model = new PermitDocumentModel(
                PermitVersionRef.of(permit.getReference(), permit.getVersion()),
                app.getReference(),
                app.getHaulierName(),
                app.getVehicleRegistration() + " (" + app.getVehicleCategory() + ")",
                app.getLoadTypeCode() + ": " + app.getLoadDescription(),
                app.getGrossMassKg() + " kg",
                "Width " + app.getWidthM() + " m, length " + app.getLengthM() + " m, height " + app.getHeightM() + " m",
                app.isHazardous() ? "Yes, UN " + app.getUnNumber() : "No",
                permit.getWindowStart(),
                permit.getWindowEnd(),
                app.getRouteCode() + ": " + structures,
                trips,
                conditions,
                app.getWidthM());
        // CR-119 switch back to the old writer if the HTML renderer has problems in production
        if (flagService.isEnabled("permit.pdf.legacy-renderer")) {
            try {
                List<String> lines = new ArrayList<>();
                boolean escort = false;
                for (PermitCondition c : permitConditionRepository.findByPermitId(permit.getId())) {
                    lines.add(c.getConditionType() + ": " + (c.getDetail() == null ? "" : c.getDetail()));
                    escort = escort || PermitCondition.ESCORT.equals(c.getConditionType());
                }
                byte[] pdf = legacyPermitPdfWriter.write(PermitReference.parse(permit.getReference()), permit.getVersion(), app,
                        Date.valueOf(permit.getWindowStart()), Date.valueOf(permit.getWindowEnd()),
                        routeDao.findSegments(app.getRouteId()), lines, escort);
                permitDao.insertDocument(permit.getId(), pdf, LegacyPermitPdfWriter.CONTENT_TYPE);
            } catch (Exception e) {
                log.error("Legacy permit writer failed for " + permit.getReference(), e);
                throw new BusinessException("DOCUMENT_FAILED", e.getMessage());
            }
            return;
        }
        try {
            permitDao.insertDocument(permit.getId(), permitRenderer.render(model), PermitRenderer.CONTENT_TYPE);
        } catch (PermitRenderingException e) {
            throw new BusinessException("DOCUMENT_FAILED", e.getMessage());
        }
    }

    private static java.time.LocalDateTime toLocal(java.util.Date date) {
        return date == null ? null : java.time.LocalDateTime.ofInstant(date.toInstant(), TimeConfig.LONDON);
    }

    private void checkEngineerAssessment(CaseFile caseFile, boolean issue, String reason) {
        if (!Tracks.ENGINEER.equals(caseFile.getTrack()) && !Tracks.PANEL.equals(caseFile.getTrack())) {
            return;
        }
        List<EngineerAssessment> assessments = engineerAssessmentRepository.findByCaseIdOrderByRecordedAtAsc(caseFile.getId());
        if (assessments.isEmpty()) {
            throw new BusinessException("ENGINEER_ASSESSMENT_REQUIRED", "An engineer assessment is needed before a decision");
        }
        EngineerAssessment latest = assessments.get(assessments.size() - 1);
        if (issue && EngineerAssessment.UNSATISFACTORY.equals(latest.getRecommendation())
                && (reason == null || reason.trim().isEmpty())) {
            throw new BusinessException("REASON_REQUIRED", "Issuing against an unsatisfactory engineer assessment needs a reason");
        }
    }

    /**
     * See ADR-0006: the haulier's email and the SLA clock both follow from this event.
     */
    private void publishDecided(CaseFile caseFile, Decision decision) {
        MovementApplication app = applicationDao.findById(caseFile.getApplicationId());
        PermitVersion permit = Outcomes.REFUSED.equals(decision.getOutcome()) ? null
                : permitVersionRepository.findByCaseIdOrderByVersionAsc(caseFile.getId()).get(0);
        events.publishEvent(new CaseDecided(caseFile.getReference(), caseFile.getId(), caseFile.getHaulierId(),
                app.getHaulierName(), app.getRouteCode(), decision.getOutcome(), decision.getReason(),
                permit == null ? null : permit.getReference(),
                permit == null ? null : permit.getWindowStart(),
                permit == null ? null : permit.getWindowEnd(),
                decision.getDecidedBy(), Instant.now(clock)));
    }
}
