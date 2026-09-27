package com.halvern.tollgate.determination;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.assessment.Assessment;
import com.halvern.tollgate.assessment.AssessmentResult;
import com.halvern.tollgate.assessment.AssessmentService;
import com.halvern.tollgate.audit.AuditEntry;
import com.halvern.tollgate.permitdoc.PermitDao;
import com.halvern.tollgate.permitdoc.PermitStatusChange;
import com.halvern.tollgate.permitdoc.PermitStatusHistoryDao;
import com.halvern.tollgate.audit.AuditService;
import com.halvern.tollgate.assessment.TriggerHit;
import com.halvern.tollgate.consultation.EngineerAssessment;
import com.halvern.tollgate.consultation.PanelListing;
import com.halvern.tollgate.consultation.PanelListingRepository;
import com.halvern.tollgate.consultation.PanelSittingRepository;
import com.halvern.tollgate.consultation.QueryRound;
import com.halvern.tollgate.consultation.QueryRoundService;
import com.halvern.tollgate.notify.NotificationLogDao;
import com.halvern.tollgate.notify.MailNotifier;
import com.halvern.tollgate.notify.NotificationLogEntry;
import com.halvern.tollgate.reference.StaffUser;
import com.halvern.tollgate.reference.UserDao;
import com.halvern.tollgate.permitdoc.Permit;
import com.halvern.tollgate.platform.CurrentUser;
import com.halvern.tollgate.platform.TimeConfig;
import com.halvern.tollgate.platform.flags.FlagService;
import com.halvern.tollgate.platform.events.AssessmentCompleted;
import com.halvern.tollgate.platform.events.CaseLodged;
import com.halvern.tollgate.platform.events.EngineerAssessmentRecorded;
import com.halvern.tollgate.platform.events.PermitStatusChanged;
import com.halvern.tollgate.platform.events.QueryRoundAnswered;
import com.halvern.tollgate.platform.events.QueryRoundIssued;
import com.halvern.tollgate.questionnaire.AnswerForm;
import com.halvern.tollgate.reference.HaulierDao;
import com.halvern.tollgate.sla.clock.SlaClockService;
import com.halvern.tollgate.intake.ApplicationDao;
import com.halvern.tollgate.intake.ApplicationManager;
import com.halvern.tollgate.intake.MovementApplication;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.questionnaire.CaseQuestion;
import com.halvern.tollgate.questionnaire.QuestionnaireManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Case lifecycle. All changes of case state go through this service (TG-101).
 *
 * @author Priya Venkat
 */
@Slf4j
@Service
public class CaseService {

    @Autowired
    private CaseFileRepository caseFileRepository;

    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private LegacyStatusSync legacyStatusSync;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private QuestionnaireManager questionnaireManager;

    // TG-201 QueryRoundService also needs CaseService
    @Lazy
    @Autowired
    private QueryRoundService queryRoundService;

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private PermitVersionRepository permitVersionRepository;

    @Autowired
    private HaulierDao haulierDao;

    private static final List<String> TERMINAL = Arrays.asList(CaseStates.DECIDED, CaseStates.WITHDRAWN,
            CaseStates.CANCELLED, CaseStates.LAPSED);

    private static final List<String> OPEN_STATES = Arrays.asList(CaseStates.LODGED, CaseStates.IN_REVIEW,
            CaseStates.AWAITING_HAULIER, CaseStates.AWAITING_PANEL);

    @Autowired
    private PermitConditionRepository permitConditionRepository;

    @Autowired
    private AmendmentRequestRepository amendmentRequestRepository;

    @Autowired
    private DecisionRepository decisionRepository;

    @Autowired
    private PanelListingRepository panelListingRepository;

    @Autowired
    private PanelSittingRepository panelSittingRepository;

    @Autowired
    private NotificationLogDao notificationLogDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PermitDao permitDao;

    // See ADR-0006: case events are published from here
    @Autowired
    private ApplicationEventPublisher events;

    @Autowired
    private Clock clock;

    @Autowired
    private SlaClockService slaClockService;

    @Autowired
    private PermitStatusHistoryDao permitStatusHistoryDao;

    @Autowired
    private FlagService flagService;

    public CaseFile findByRef(String reference) {
        return caseFileRepository.findByReference(reference).get();
    }

    public boolean exists(String reference) {
        return caseFileRepository.findByReference(reference).isPresent();
    }

    /**
     * Lodge a draft application and open its case. The old lodgement checks still run in
     * ApplicationManager; if they fail nothing is saved.
     */
    @Transactional(rollbackFor = TollgateException.class)
    public CaseFile lodge(String reference, String username) throws TollgateException {
        List<String> warnings = applicationManager.lodge(reference, username);
        MovementApplication app = applicationDao.findByReference(reference);
        if (exists(reference)) {
            throw new BusinessException("CASE_EXISTS", "A case already exists for " + reference);
        }
        CaseFile caseFile = new CaseFile();
        caseFile.setReference(reference);
        caseFile.setApplicationId(app.getId());
        caseFile.setHaulierId(app.getHaulierId());
        caseFile.setState(CaseStates.LODGED);
        caseFile.setWarnings(warnings.isEmpty() ? null : String.join(",", warnings));
        caseFile.setLodgedAt(LocalDateTime.now());
        caseFile.setUpdatedAt(LocalDateTime.now());
        caseFileRepository.save(caseFile);
        legacyStatusSync.sync(caseFile);
        auditService.recordCase(reference, null, CaseStates.LODGED, username, null);
        log.info("Case {} opened by {}", reference, username);
        events.publishEvent(new CaseLodged(reference, caseFile.getId(), caseFile.getLodgedAt().toLocalDate(), username,
                Instant.now(clock)));

        // TG-145 load types with no required questions are assessed straight away
        boolean anyMandatory = questionnaireManager.findQuestions(app.getId()).stream().anyMatch(CaseQuestion::isMandatory);
        if (!anyMandatory) {
            runAssessment(caseFile);
        }
        return caseFile;
    }

    /**
     * Move a case to a new state, keep the old status in step and audit the change.
     */
    public CaseFile changeState(CaseFile caseFile, String newState, String actor, String reason) {
        String oldState = caseFile.getState();
        caseFile.setState(newState);
        caseFile.setUpdatedAt(LocalDateTime.now());
        caseFileRepository.save(caseFile);
        legacyStatusSync.sync(caseFile);
        auditService.recordCase(caseFile.getReference(), oldState, newState, actor, reason);
        log.info("Case {} {} -> {} by {}", caseFile.getReference(), oldState, newState, actor);
        // See ADR-0009: the SLA clock stops when the case is finished
        if (TERMINAL.contains(newState)) {
            slaClockService.stop(caseFile.getId(), LocalDate.ofInstant(Instant.now(clock), TimeConfig.LONDON));
        }
        return caseFile;
    }

    public CaseFile save(CaseFile caseFile) {
        caseFile.setUpdatedAt(LocalDateTime.now());
        return caseFileRepository.save(caseFile);
    }

    @Transactional(rollbackFor = TollgateException.class)
    public void acceptAnswer(String reference, long caseQuestionId, String username, String note) throws TollgateException {
        CaseFile caseFile = findByRef(reference);
        questionnaireManager.accept(caseFile.getApplicationId(), caseQuestionId, username, note);
        if (CaseStates.LODGED.equals(caseFile.getState()) && allMandatoryAccepted(caseFile)) {
            runAssessment(caseFile);
        }
    }

    @Transactional(rollbackFor = TollgateException.class)
    public void returnAnswer(String reference, long caseQuestionId, String username, String note) throws TollgateException {
        CaseFile caseFile = findByRef(reference);
        questionnaireManager.returnAnswer(caseFile.getApplicationId(), caseQuestionId, username, note);
        if (CaseStates.LODGED.equals(caseFile.getState())) {
            markAwaitingHaulier(caseFile, username, "Questionnaire answer returned");
        }
    }

    public CaseFile markAwaitingHaulier(CaseFile caseFile, String actor, String reason) {
        return changeState(caseFile, CaseStates.AWAITING_HAULIER, actor, reason);
    }

    /**
     * Revised questionnaire answers from the haulier (entered by an officer).
     */
    @Transactional(rollbackFor = TollgateException.class)
    public int reviseAnswers(String reference, AnswerForm form, String username) throws TollgateException {
        CaseFile caseFile = findByRef(reference);
        int saved = questionnaireManager.saveAnswers(caseFile.getApplicationId(), form);
        boolean stillReturned = questionnaireManager.findQuestions(caseFile.getApplicationId()).stream()
                .anyMatch(q -> CaseQuestion.RETURNED.equals(q.getReviewStatus()));
        if (CaseStates.AWAITING_HAULIER.equals(caseFile.getState()) && caseFile.getTrack() == null && !stillReturned) {
            changeState(caseFile, CaseStates.LODGED, username, "Returned answers revised");
        }
        return saved;
    }

    @Transactional
    public CaseFile withdraw(String reference, String username, String reason) {
        CaseFile caseFile = findByRef(reference);
        if (TERMINAL.contains(caseFile.getState())) {
            throw new BusinessException("TERMINAL", "Case " + reference + " is already " + caseFile.getState());
        }
        queryRoundService.closeOpenRounds(caseFile);
        caseFile.setClosedReason(reason);
        return changeState(caseFile, CaseStates.WITHDRAWN, username, reason == null ? "Withdrawn by the haulier" : reason);
    }

    @Transactional
    public CaseFile cancel(String reference, String reason, String username) {
        if (!CurrentUser.hasRole("OFFICER")) {
            throw new BusinessException("NOT_ALLOWED", "Only officers can cancel a case");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("REASON_REQUIRED", "Give a reason for cancelling the case");
        }
        CaseFile caseFile = findByRef(reference);
        if (TERMINAL.contains(caseFile.getState())) {
            throw new BusinessException("TERMINAL", "Case " + reference + " is already " + caseFile.getState());
        }
        queryRoundService.closeOpenRounds(caseFile);
        caseFile.setClosedReason(reason.trim());
        return changeState(caseFile, CaseStates.CANCELLED, username, reason.trim());
    }

    /**
     * Revoke the case's permit. Revocation is final.
     */
    @PreAuthorize("hasRole('OFFICER')")
    @Transactional
    public PermitVersion revokePermit(String reference, String reason, String username) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("REASON_REQUIRED", "Give a reason for revoking the permit");
        }
        CaseFile caseFile = findByRef(reference);
        List<PermitVersion> versions = permitVersionRepository.findByCaseIdOrderByVersionAsc(caseFile.getId());
        if (versions.isEmpty()) {
            throw new BusinessException("NO_PERMIT", "Case " + reference + " has no permit");
        }
        PermitVersion permit = versions.get(versions.size() - 1);
        if (!Permit.ACTIVE.equals(permit.getStatus()) && !Permit.SUSPENDED.equals(permit.getStatus())) {
            throw new BusinessException("NOT_REVOCABLE", "Permit " + permit.getReference() + " is " + permit.getStatus());
        }
        String oldStatus = permit.getStatus();
        permit.setStatus(Permit.REVOKED);
        permit.setStatusReason(reason.trim());
        permit.setStatusChangedAt(LocalDateTime.now());
        permitVersionRepository.save(permit);
        auditService.recordPermit(permit.getReference(), oldStatus, Permit.REVOKED, username, reason.trim());
        // The haulier is told by NotificationEventListener
        events.publishEvent(new PermitStatusChanged(permit.getReference(), reference, caseFile.getHaulierId(), oldStatus,
                Permit.REVOKED, reason.trim(), username, Instant.now(clock)));
        return permit;
    }

    private boolean allMandatoryAccepted(CaseFile caseFile) {
        return questionnaireManager.findQuestions(caseFile.getApplicationId()).stream()
                .filter(CaseQuestion::isMandatory)
                .allMatch(q -> CaseQuestion.ACCEPTED.equals(q.getReviewStatus()));
    }

    /**
     * Assess the case and move it into review.
     */
    private void runAssessment(CaseFile caseFile) {
        AssessmentResult result = assessmentService.assess(caseFile.getId(), caseFile.getApplicationId(),
                caseFile.getHaulierId(), caseFile.getLodgedAt().toLocalDate(), caseFile.getWarnings() != null);
        String oldState = caseFile.getState();
        caseFile.setTrack(result.getAssessment().getFinalTrack());
        caseFile.setRiskScore(result.getAssessment().getScore());
        caseFile.setState(CaseStates.IN_REVIEW);
        caseFile.setUpdatedAt(LocalDateTime.now());
        caseFileRepository.save(caseFile);
        legacyStatusSync.sync(caseFile);
        auditService.recordCase(caseFile.getReference(), oldState, CaseStates.IN_REVIEW, AuditEntry.SYSTEM,
                "Assessed: score " + result.getAssessment().getScore() + ", track " + caseFile.getTrack());
        events.publishEvent(new AssessmentCompleted(caseFile.getReference(), caseFile.getId(), caseFile.getTrack(),
                result.getAssessment().getScore(), result.isExpressEligible(), Instant.now(clock)));
        if (result.isExpressEligible() && flagService.isEnabled("express.auto-issue")) {
            decisionService.autoIssue(caseFile);
        }
    }

    // ------------------------------------------------------------------
    // Escalation, amendments and audit helpers for the case page (TG-209, TG-216, TG-219)
    // ------------------------------------------------------------------

    /**
     * Track changes on a case, oldest first, read from the audit trail (TG-209).
     */
    public List<AuditEntry> escalationHistory(CaseFile caseFile) {
        List<AuditEntry> escalations = new ArrayList<>();
        for (AuditEntry entry : auditService.findHistory(caseFile.getReference())) {
            if (entry.getReason() != null && (entry.getReason().startsWith("Escalated")
                    || entry.getReason().startsWith("Returned for reassessment"))) {
                escalations.add(entry);
            }
        }
        return escalations;
    }

    /**
     * True if the case has been escalated by hand at least once (TG-209).
     */
    public boolean wasEscalated(CaseFile caseFile) {
        return caseFile.getEscalatedTrack() != null || !escalationHistory(caseFile).isEmpty();
    }

    /**
     * Amendments still waiting for an officer on this case (TG-216).
     */
    public List<AmendmentRequest> pendingAmendments(CaseFile caseFile) {
        return amendments(caseFile).stream()
                .filter(a -> AmendmentRequest.REQUESTED.equals(a.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Number of versions the permit has had (TG-216).
     */
    public int permitVersionCount(CaseFile caseFile) {
        return permitVersions(caseFile).size();
    }

    /**
     * Movement window of the current permit in days, inclusive (TG-216). Zero if there is no permit.
     */
    public long permitWindowDays(CaseFile caseFile) {
        PermitVersion permit = currentPermit(caseFile);
        if (permit == null || permit.getWindowStart() == null || permit.getWindowEnd() == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(permit.getWindowStart(), permit.getWindowEnd()) + 1;
    }

    /**
     * Who last changed the case, from the audit trail (TG-219).
     */
    public String lastActor(CaseFile caseFile) {
        List<AuditEntry> history = auditService.findHistory(caseFile.getReference());
        return history.isEmpty() ? null : history.get(history.size() - 1).getActor();
    }

    /**
     * Audit entries for the case only, without the application and permit entries (TG-219).
     */
    public List<AuditEntry> caseAuditTrail(CaseFile caseFile) {
        return auditService.findHistory(caseFile.getReference()).stream()
                .filter(e -> AuditEntry.CASE.equals(e.getEntityType()))
                .collect(Collectors.toList());
    }

    /**
     * Time from lodgement to decision in hours, for the decided cases report (TG-219).
     */
    public Long hoursToDecision(CaseFile caseFile) {
        if (caseFile.getDecidedAt() == null || caseFile.getLodgedAt() == null) {
            return null;
        }
        return ChronoUnit.HOURS.between(caseFile.getLodgedAt(), caseFile.getDecidedAt());
    }

    // TG-221 which actions the case page offers

    public boolean canWithdraw(CaseFile caseFile) {
        return !isTerminal(caseFile);
    }

    public boolean canCancel(CaseFile caseFile) {
        return !isTerminal(caseFile) && CurrentUser.hasRole("OFFICER");
    }

    public boolean canAssignEngineer(CaseFile caseFile) {
        return !isTerminal(caseFile) && ("ENGINEER".equals(caseFile.getTrack()) || "PANEL".equals(caseFile.getTrack()));
    }

    public boolean canAmend(CaseFile caseFile) {
        PermitVersion permit = currentPermit(caseFile);
        return permit != null && Permit.ACTIVE.equals(permit.getStatus()) && pendingAmendments(caseFile).isEmpty();
    }

    public boolean canAnswerQueryRound(CaseFile caseFile) {
        return CaseStates.AWAITING_HAULIER.equals(caseFile.getState()) && openRound(caseFile) != null;
    }

    // TG-224 staff lists ordered by workload

    /**
     * Engineers with the number of open cases assigned to each, fewest first (TG-224).
     */
    public List<Map<String, Object>> engineersByWorkload() {
        Map<String, Long> open = new HashMap<>();
        for (CaseFile c : findOpenCases()) {
            if (c.getAssignedEngineer() != null) {
                open.put(c.getAssignedEngineer(), open.getOrDefault(c.getAssignedEngineer(), 0L) + 1);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (StaffUser engineer : engineers()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("username", engineer.getUsername());
            row.put("fullName", engineer.getFullName());
            row.put("openCases", open.getOrDefault(engineer.getUsername(), 0L));
            result.add(row);
        }
        result.sort(Comparator.comparing(r -> (Long) r.get("openCases")));
        return result;
    }

    /**
     * Officers with the number of open cases assigned to each, fewest first (TG-224).
     */
    public List<Map<String, Object>> officersByWorkload() {
        Map<String, Long> open = new HashMap<>();
        for (CaseFile c : findOpenCases()) {
            if (c.getAssignedOfficer() != null) {
                open.put(c.getAssignedOfficer(), open.getOrDefault(c.getAssignedOfficer(), 0L) + 1);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (StaffUser officer : officers()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("username", officer.getUsername());
            row.put("fullName", officer.getFullName());
            row.put("openCases", open.getOrDefault(officer.getUsername(), 0L));
            result.add(row);
        }
        result.sort(Comparator.comparing(r -> (Long) r.get("openCases")));
        return result;
    }

    /**
     * Open cases for one track, oldest first (TG-206).
     */
    public List<CaseFile> findOpenCasesForTrack(String track) {
        return findOpenCases().stream()
                .filter(c -> track.equals(c.getTrack()))
                .sorted(Comparator.comparing(CaseFile::getLodgedAt))
                .collect(Collectors.toList());
    }

    /**
     * Open cases for one officer, oldest first (TG-206).
     */
    public List<CaseFile> findOpenCasesForOfficer(String officer) {
        return findOpenCases().stream()
                .filter(c -> officer.equals(c.getAssignedOfficer()))
                .sorted(Comparator.comparing(CaseFile::getLodgedAt))
                .collect(Collectors.toList());
    }

    /**
     * Open cases waiting for an engineer's assessment (TG-206).
     */
    public List<CaseFile> findAwaitingEngineer() {
        List<CaseFile> result = new ArrayList<>();
        for (CaseFile c : findOpenCases()) {
            if (canAssignEngineer(c) && latestEngineerAssessment(c) == null) {
                result.add(c);
            }
        }
        result.sort(Comparator.comparing(CaseFile::getLodgedAt));
        return result;
    }

    // ------------------------------------------------------------------
    // Notifications and dates on the case page (TG-212, TG-232)
    // ------------------------------------------------------------------

    /**
     * Emails for the case that could not be sent (TG-212).
     */
    public List<NotificationLogEntry> failedNotifications(CaseFile caseFile) {
        return notifications(caseFile).stream()
                .filter(n -> NotificationLogEntry.FAILED.equals(n.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * True if an email from this template was sent for the case (TG-212).
     */
    public boolean notificationSent(CaseFile caseFile, String template) {
        return notifications(caseFile).stream()
                .anyMatch(n -> template.equals(n.getTemplate()) && NotificationLogEntry.SENT.equals(n.getStatus()));
    }

    /**
     * Number of emails sent for the case, by template (TG-212).
     */
    public Map<String, Long> notificationCounts(CaseFile caseFile) {
        return notifications(caseFile).stream()
                .collect(Collectors.groupingBy(NotificationLogEntry::getTemplate, TreeMap::new, Collectors.counting()));
    }

    /**
     * Calendar days from today to the first move date (TG-232). Negative once the move date has passed.
     */
    public long daysToFirstMove(CaseFile caseFile) {
        MovementApplication app = getApplication(caseFile);
        LocalDate first = ((java.sql.Date) app.getFirstMoveDate()).toLocalDate();
        return ChronoUnit.DAYS.between(LocalDate.now(), first);
    }

    /**
     * Business days between lodgement and the first move date, for the notice column (TG-232).
     */
    public int noticeDays(CaseFile caseFile) {
        MovementApplication app = getApplication(caseFile);
        LocalDate lodged = caseFile.getLodgedAt().toLocalDate();
        LocalDate first = ((java.sql.Date) app.getFirstMoveDate()).toLocalDate();
        int days = 0;
        for (LocalDate d = lodged.plusDays(1); !d.isAfter(first); d = d.plusDays(1)) {
            // TG-232 weekends only, the page is a guide
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days++;
            }
        }
        return days;
    }

    /**
     * Movement dates of the application as shown on the case page (TG-232).
     */
    public String movementDates(CaseFile caseFile) {
        MovementApplication app = getApplication(caseFile);
        LocalDate first = ((java.sql.Date) app.getFirstMoveDate()).toLocalDate();
        LocalDate last = ((java.sql.Date) app.getLastMoveDate()).toLocalDate();
        java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return first.equals(last) ? first.format(f) : first.format(f) + " to " + last.format(f);
    }

    // ------------------------------------------------------------------
    // Domain events (ADR-0006)
    //
    // Query rounds and engineer assessments are recorded in consultation, but the events for them are
    // published here so that there is one place that says what can happen to a case. Call these after the
    // change has been saved.
    // ------------------------------------------------------------------

    public void queryRoundIssued(CaseFile caseFile, QueryRound round) {
        events.publishEvent(new QueryRoundIssued(caseFile.getReference(), caseFile.getId(), round.getRoundNo(),
                round.getIssuedOn(), round.getDeadline(), Instant.now(clock)));
    }

    public void queryRoundAnswered(CaseFile caseFile, QueryRound round) {
        events.publishEvent(new QueryRoundAnswered(caseFile.getReference(), caseFile.getId(), round.getRoundNo(),
                round.getAnsweredAt().toLocalDate(), Instant.now(clock)));
    }

    public void engineerAssessmentRecorded(CaseFile caseFile, EngineerAssessment assessment) {
        events.publishEvent(new EngineerAssessmentRecorded(caseFile.getReference(), caseFile.getId(),
                caseFile.getTrack(), assessment.getRecommendation(), assessment.getEngineer(), Instant.now(clock)));
    }

    // ------------------------------------------------------------------
    // Case page and worklist (TG-205)
    // ------------------------------------------------------------------

    public List<CaseFile> findOpenCases() {
        return caseFileRepository.findByStateIn(OPEN_STATES);
    }

    public List<String> openStates() {
        return OPEN_STATES;
    }

    /**
     * Open cases for the worklist, optionally filtered. Blank filters match everything.
     */
    public List<CaseFile> findOpenCases(String track, String state, String officer) {
        List<CaseFile> result = new ArrayList<>();
        for (CaseFile c : findOpenCases()) {
            if (track != null && !track.isEmpty() && !track.equals(c.getTrack())) {
                continue;
            }
            if (state != null && !state.isEmpty() && !state.equals(c.getState())) {
                continue;
            }
            if (officer != null && !officer.isEmpty() && !officer.equals(c.getAssignedOfficer())) {
                continue;
            }
            result.add(c);
        }
        result.sort(Comparator.comparing(CaseFile::getLodgedAt));
        return result;
    }

    public Map<String, Long> countsByState() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String s : OPEN_STATES) {
            counts.put(s, 0L);
        }
        for (CaseFile c : findOpenCases()) {
            counts.put(c.getState(), counts.get(c.getState()) + 1);
        }
        return counts;
    }

    public List<CaseFile> findByHaulier(Long haulierId) {
        return caseFileRepository.findAll().stream()
                .filter(c -> haulierId.equals(c.getHaulierId()))
                .sorted(Comparator.comparing(CaseFile::getLodgedAt).reversed())
                .collect(Collectors.toList());
    }

    public MovementApplication getApplication(CaseFile caseFile) {
        return applicationDao.findById(caseFile.getApplicationId());
    }

    public String haulierName(CaseFile caseFile) {
        return haulierDao.findById(caseFile.getHaulierId()).getName();
    }

    public List<String> warningsOf(CaseFile caseFile) {
        if (caseFile.getWarnings() == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(caseFile.getWarnings().split(","));
    }

    public Assessment currentAssessment(CaseFile caseFile) {
        Assessment current = null;
        for (Assessment a : caseFile.getAssessments()) {
            if (!a.isSuperseded()) {
                current = a;
            }
        }
        return current;
    }

    public List<TriggerHit> currentTriggers(CaseFile caseFile) {
        Assessment a = currentAssessment(caseFile);
        return a == null ? new ArrayList<>() : a.getTriggerHits();
    }

    public EngineerAssessment latestEngineerAssessment(CaseFile caseFile) {
        List<EngineerAssessment> list = caseFile.getEngineerAssessments();
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    public QueryRound openRound(CaseFile caseFile) {
        for (QueryRound r : caseFile.getQueryRounds()) {
            if (QueryRound.OPEN.equals(r.getStatus())) {
                return r;
            }
        }
        return null;
    }

    public Decision latestDecision(CaseFile caseFile) {
        List<Decision> decisions = decisionRepository.findByCaseIdOrderByDecidedAtAsc(caseFile.getId());
        return decisions.isEmpty() ? null : decisions.get(decisions.size() - 1);
    }

    public List<PermitVersion> permitVersions(CaseFile caseFile) {
        return permitVersionRepository.findByCaseIdOrderByVersionAsc(caseFile.getId());
    }

    public PermitVersion currentPermit(CaseFile caseFile) {
        List<PermitVersion> versions = permitVersions(caseFile);
        return versions.isEmpty() ? null : versions.get(versions.size() - 1);
    }

    public List<PermitCondition> conditionsFor(PermitVersion permit) {
        return permitConditionRepository.findByPermitId(permit.getId());
    }

    public boolean hasDocument(PermitVersion permit) {
        return permitDao.findDocument(permit.getId()) != null;
    }

    public List<AmendmentRequest> amendments(CaseFile caseFile) {
        return amendmentRequestRepository.findByCaseIdOrderByRequestedAtDesc(caseFile.getId());
    }

    public List<AmendmentRequest> pendingAmendments() {
        return amendmentRequestRepository.findByStatusOrderByRequestedAtAsc(AmendmentRequest.REQUESTED);
    }

    public List<PanelListing> panelListings(CaseFile caseFile) {
        return panelListingRepository.findByCaseId(caseFile.getId());
    }

    /**
     * Case changes from the audit trail and permit changes from the permit status history, oldest first.
     * Permit changes come from the history table because it also catches changes made outside the services.
     */
    public List<TimelineEntry> timeline(CaseFile caseFile) {
        List<TimelineEntry> entries = new ArrayList<>();
        for (AuditEntry a : auditService.findHistory(caseFile.getReference())) {
            if (AuditEntry.PERMIT.equals(a.getEntityType())) {
                continue;
            }
            entries.add(new TimelineEntry(new java.sql.Timestamp(a.getCreatedAt().getTime()).toLocalDateTime(),
                    a.getReference(), a.getOldState(), a.getNewState(), a.getActor(), a.getReason()));
        }
        for (PermitStatusChange change : permitStatusHistoryDao.findForApplication(caseFile.getApplicationId())) {
            entries.add(new TimelineEntry(LocalDateTime.ofInstant(change.changedAt(), TimeConfig.LONDON),
                    change.versionRef().toString(), change.oldStatus(), change.newStatus(), change.changedBy(), null));
        }
        entries.sort(Comparator.comparing(TimelineEntry::at));
        return entries;
    }

    public List<NotificationLogEntry> notifications(CaseFile caseFile) {
        return notificationLogDao.findForApplication(caseFile.getReference());
    }

    public List<StaffUser> engineers() {
        return userDao.findAll().stream().filter(u -> u.isEnabled() && u.hasRole("ENGINEER")).collect(Collectors.toList());
    }

    public List<StaffUser> officers() {
        return userDao.findAll().stream().filter(u -> u.isEnabled() && u.hasRole("OFFICER")).collect(Collectors.toList());
    }

    public boolean isTerminal(CaseFile caseFile) {
        return TERMINAL.contains(caseFile.getState());
    }

    public boolean canDecide(CaseFile caseFile) {
        return CaseStates.IN_REVIEW.equals(caseFile.getState()) && !"PANEL".equals(caseFile.getTrack());
    }

    public boolean canRecordPanelDecision(CaseFile caseFile) {
        return CaseStates.AWAITING_PANEL.equals(caseFile.getState());
    }

    public boolean canIssueQueryRound(CaseFile caseFile) {
        return CaseStates.IN_REVIEW.equals(caseFile.getState()) && caseFile.getTrack() != null
                && !"EXPRESS".equals(caseFile.getTrack()) && caseFile.getQueryRounds().size() < 3;
    }

    public boolean canEscalate(CaseFile caseFile) {
        return (CaseStates.IN_REVIEW.equals(caseFile.getState()) || CaseStates.AWAITING_HAULIER.equals(caseFile.getState()))
                && caseFile.getTrack() != null && !"PANEL".equals(caseFile.getTrack());
    }

    public boolean canReassess(CaseFile caseFile) {
        return CaseStates.IN_REVIEW.equals(caseFile.getState()) || CaseStates.AWAITING_PANEL.equals(caseFile.getState());
    }

    public boolean canRecordEngineerAssessment(CaseFile caseFile) {
        return CaseStates.IN_REVIEW.equals(caseFile.getState()) || CaseStates.AWAITING_HAULIER.equals(caseFile.getState());
    }

    public boolean canRevoke(CaseFile caseFile) {
        PermitVersion permit = currentPermit(caseFile);
        return permit != null && (Permit.ACTIVE.equals(permit.getStatus()) || Permit.SUSPENDED.equals(permit.getStatus()));
    }

    @Transactional
    public CaseFile assignOfficer(String reference, String officer, String username) {
        if (!CurrentUser.hasRole("OFFICER")) {
            throw new BusinessException("NOT_ALLOWED", "Only officers can assign cases");
        }
        StaffUser user = userDao.findByUsername(officer);
        if (user == null || !user.hasRole("OFFICER")) {
            throw new BusinessException("NOT_OFFICER", officer + " is not an officer");
        }
        CaseFile caseFile = findByRef(reference);
        caseFile.setAssignedOfficer(officer);
        save(caseFile);
        auditService.recordCase(reference, caseFile.getState(), caseFile.getState(), username, "Assigned to " + officer);
        return caseFile;
    }

    @Transactional
    public CaseFile updateComments(String reference, String comments, String username) {
        CaseFile caseFile = findByRef(reference);
        if (comments != null && comments.length() > 2000) {
            throw new BusinessException("TOO_LONG", "Comments must be 2000 characters or fewer");
        }
        caseFile.setOfficerComments(comments);
        save(caseFile);
        log.info("Comments on {} updated by {}", reference, username);
        return caseFile;
    }

    // CR-141 CSV export of cases for the monthly management pack

    private static final String EXPORT_SQL = "select c.reference, h.name as haulier, c.state, c.outcome, c.track, c.risk_score, "
            + "c.assigned_officer, c.assigned_engineer, c.lodged_at, c.decided_at, r.code as route, lt.code as load_type, "
            + "a.gross_mass_kg, a.width_m, a.length_m, a.height_m, a.first_move_date, a.last_move_date "
            + "from case_file c join haulier h on h.id = c.haulier_id join movement_application a on a.id = c.application_id "
            + "left join route r on r.id = a.route_id left join load_type lt on lt.id = a.load_type_id "
            + "where (? = '' or c.state = ?) and (? = '' or c.track = ?) and c.lodged_at >= ? and c.lodged_at < ? "
            + "order by c.lodged_at";

    private static final String[] EXPORT_COLUMNS = { "reference", "haulier", "state", "outcome", "track", "risk_score",
            "assigned_officer", "assigned_engineer", "lodged_at", "decided_at", "route", "load_type", "gross_mass_kg",
            "width_m", "length_m", "height_m", "first_move_date", "last_move_date" };

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * CR-141 cases lodged between two dates as CSV. Blank state or track means all.
     */
    public String exportCases(String state, String track, LocalDate from, LocalDate to) {
        String s = state == null ? "" : state.trim();
        String t = track == null ? "" : track.trim();
        LocalDate start = from == null ? LocalDate.now().minusMonths(1) : from;
        LocalDate end = to == null ? LocalDate.now() : to;
        if (end.isBefore(start)) {
            throw new BusinessException("BAD_DATES", "The end date is before the start date");
        }
        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",", EXPORT_COLUMNS)).append("\r\n");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(EXPORT_SQL, s, s, t, t,
                java.sql.Timestamp.valueOf(start.atStartOfDay()), java.sql.Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
        for (Map<String, Object> row : rows) {
            List<String> values = new ArrayList<>();
            for (String column : EXPORT_COLUMNS) {
                values.add(csvValue(row.get(column)));
            }
            csv.append(String.join(",", values)).append("\r\n");
        }
        log.info("Exported {} cases ({} to {}, state '{}', track '{}')", rows.size(), start, end, s, t);
        return csv.toString();
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        // CR-141 Excel treats a leading = + - @ as a formula
        if (!text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            text = "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    // CR-144 case search

    /**
     * CR-144 search by reference, haulier name or description, with optional state, track and lodgement dates.
     * Returns at most 200 rows, newest first.
     */
    public List<Map<String, Object>> searchCases(String text, String state, String track, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder("select c.reference, h.name as haulier, c.state, c.track, c.outcome, "
                + "c.assigned_officer, c.lodged_at, a.load_description from case_file c "
                + "join haulier h on h.id = c.haulier_id join movement_application a on a.id = c.application_id where 1 = 1");
        List<Object> params = new ArrayList<>();
        if (text != null && !text.trim().isEmpty()) {
            String like = "%" + text.trim().toLowerCase() + "%";
            sql.append(" and (lower(c.reference) like ? or lower(h.name) like ? or lower(a.load_description) like ?)");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (state != null && !state.trim().isEmpty()) {
            sql.append(" and c.state = ?");
            params.add(state.trim());
        }
        if (track != null && !track.trim().isEmpty()) {
            sql.append(" and c.track = ?");
            params.add(track.trim());
        }
        if (from != null) {
            sql.append(" and c.lodged_at >= ?");
            params.add(java.sql.Timestamp.valueOf(from.atStartOfDay()));
        }
        if (to != null) {
            sql.append(" and c.lodged_at < ?");
            params.add(java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
        }
        sql.append(" order by c.lodged_at desc limit 200");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        for (Map<String, Object> row : rows) {
            row.put("stateLabel", stateLabel((String) row.get("state")));
            java.sql.Timestamp lodged = (java.sql.Timestamp) row.get("lodged_at");
            row.put("ageDays", lodged == null ? null : ChronoUnit.DAYS.between(lodged.toLocalDateTime().toLocalDate(), LocalDate.now()));
        }
        return rows;
    }

    /**
     * CR-144 wording for states on the search and report pages.
     */
    public String stateLabel(String state) {
        if (state == null) {
            return "";
        }
        switch (state) {
            case "LODGED":
                return "Lodged, waiting for answers";
            case "IN_REVIEW":
                return "In review";
            case "AWAITING_HAULIER":
                return "Waiting for the haulier";
            case "AWAITING_PANEL":
                return "Listed for panel";
            case "DECIDED":
                return "Decided";
            case "WITHDRAWN":
                return "Withdrawn";
            case "CANCELLED":
                return "Cancelled";
            case "LAPSED":
                return "Lapsed";
            default:
                return state;
        }
    }

    // CR-146 move an officer's open cases to another officer

    /**
     * CR-146 reassign every open case of one officer to another, for leave and leavers.
     * @return the references moved
     */
    @Transactional
    public List<String> reassignOfficer(String fromOfficer, String toOfficer, String username) {
        if (fromOfficer == null || toOfficer == null || fromOfficer.equals(toOfficer)) {
            throw new BusinessException("BAD_REASSIGN", "Choose two different officers");
        }
        StaffUser target = userDao.findByUsername(toOfficer);
        if (target == null || !target.isEnabled() || !target.hasRole("OFFICER")) {
            throw new BusinessException("NOT_OFFICER", toOfficer + " is not an active officer");
        }
        List<String> moved = new ArrayList<>();
        for (CaseFile caseFile : findOpenCases()) {
            if (!fromOfficer.equals(caseFile.getAssignedOfficer())) {
                continue;
            }
            caseFile.setAssignedOfficer(toOfficer);
            save(caseFile);
            auditService.recordCase(caseFile.getReference(), caseFile.getState(), caseFile.getState(), username,
                    "Reassigned from " + fromOfficer + " to " + toOfficer);
            moved.add(caseFile.getReference());
        }
        log.info("{} moved {} open cases from {} to {}", username, moved.size(), fromOfficer, toOfficer);
        return moved;
    }

    /**
     * CR-146 open cases per officer, for the reassignment screen and the KPI page.
     */
    public Map<String, Long> openCasesByOfficer() {
        Map<String, Long> counts = new TreeMap<>();
        for (StaffUser officer : officers()) {
            counts.put(officer.getUsername(), 0L);
        }
        for (CaseFile caseFile : findOpenCases()) {
            String officer = caseFile.getAssignedOfficer() == null ? "(unassigned)" : caseFile.getAssignedOfficer();
            counts.put(officer, counts.getOrDefault(officer, 0L) + 1);
        }
        return counts;
    }

    // CR-155 monthly KPI report

    private static final String KPI_LODGED_SQL = "select count(*) from case_file where lodged_at >= ? and lodged_at < ?";

    private static final String KPI_CLOSED_SQL = "select c.state, c.outcome, c.track, c.lodged_at, c.decided_at, c.updated_at "
            + "from case_file c where c.state in ('DECIDED', 'WITHDRAWN', 'CANCELLED', 'LAPSED') "
            + "and coalesce(c.decided_at, c.updated_at) >= ? and coalesce(c.decided_at, c.updated_at) < ?";

    /**
     * CR-155 figures for one month: cases lodged and closed, outcomes, average days to a decision and the share
     * decided within target, by track.
     */
    public Map<String, Object> monthlyKpis(YearMonth month) {
        java.sql.Timestamp start = java.sql.Timestamp.valueOf(month.atDay(1).atStartOfDay());
        java.sql.Timestamp end = java.sql.Timestamp.valueOf(month.plusMonths(1).atDay(1).atStartOfDay());
        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("month", month.toString());
        kpis.put("lodged", jdbcTemplate.queryForObject(KPI_LODGED_SQL, Long.class, start, end));

        Map<String, Long> closed = new LinkedHashMap<>();
        for (String s : Arrays.asList("ISSUED", "ISSUED_WITH_CONDITIONS", "REFUSED", "WITHDRAWN", "CANCELLED", "LAPSED")) {
            closed.put(s, 0L);
        }
        Map<String, List<Long>> daysByTrack = new LinkedHashMap<>();
        for (String t : Arrays.asList("EXPRESS", "DESK", "ENGINEER", "PANEL")) {
            daysByTrack.put(t, new ArrayList<>());
        }
        for (Map<String, Object> row : jdbcTemplate.queryForList(KPI_CLOSED_SQL, start, end)) {
            String state = (String) row.get("state");
            String key = "DECIDED".equals(state) ? (String) row.get("outcome") : state;
            closed.put(key, closed.getOrDefault(key, 0L) + 1);
            String track = (String) row.get("track");
            java.sql.Timestamp decided = (java.sql.Timestamp) row.get("decided_at");
            if ("DECIDED".equals(state) && track != null && decided != null) {
                LocalDate lodgedOn = ((java.sql.Timestamp) row.get("lodged_at")).toLocalDateTime().toLocalDate();
                daysByTrack.get(track).add(workingDaysBetween(lodgedOn, decided.toLocalDateTime().toLocalDate()));
            }
        }
        kpis.put("closed", closed);

        Map<String, Map<String, Object>> tracks = new LinkedHashMap<>();
        for (Map.Entry<String, List<Long>> e : daysByTrack.entrySet()) {
            Map<String, Object> t = new LinkedHashMap<>();
            List<Long> days = e.getValue();
            t.put("decided", days.size());
            t.put("averageDays", days.isEmpty() ? null : Math.round(days.stream().mapToLong(Long::longValue).average().getAsDouble() * 10) / 10.0);
            long target = kpiTarget(e.getKey());
            long within = days.stream().filter(d -> d <= target).count();
            t.put("target", target);
            t.put("withinTargetPercent", days.isEmpty() ? null : Math.round(within * 100.0 / days.size()));
            tracks.put(e.getKey(), t);
        }
        kpis.put("tracks", tracks);
        kpis.put("openByOfficer", openCasesByOfficer());
        return kpis;
    }

    // CR-155 same targets as the SLA dashboard
    private long kpiTarget(String track) {
        switch (track) {
            case "EXPRESS":
                return 2;
            case "ENGINEER":
                return 15;
            case "PANEL":
                return 25;
            default:
                return 5;
        }
    }

    /**
     * CR-155 business days after one date up to and including another. Weekends and the holiday table are skipped.
     */
    public long workingDaysBetween(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            return 0;
        }
        java.util.Set<LocalDate> holidays = new java.util.HashSet<>();
        for (Map<String, Object> h : jdbcTemplate.queryForList("select holiday_date from holiday where holiday_date > ? and holiday_date <= ?",
                java.sql.Date.valueOf(from), java.sql.Date.valueOf(to))) {
            holidays.add(((java.sql.Date) h.get("holiday_date")).toLocalDate());
        }
        long days = 0;
        for (LocalDate d = from.plusDays(1); !d.isAfter(to); d = d.plusDays(1)) {
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY && !holidays.contains(d)) {
                days++;
            }
        }
        return days;
    }

    // CR-149 haulier history for the permit office

    /**
     * CR-149 a haulier's cases, permits and incidents over the last two years.
     */
    public Map<String, Object> haulierHistory(Long haulierId) {
        Map<String, Object> history = new LinkedHashMap<>();
        history.put("haulier", haulierDao.findById(haulierId));
        java.sql.Timestamp since = java.sql.Timestamp.valueOf(LocalDate.now().minusYears(2).atStartOfDay());
        history.put("cases", jdbcTemplate.queryForList("select c.reference, c.state, c.outcome, c.track, c.lodged_at "
                + "from case_file c where c.haulier_id = ? and c.lodged_at >= ? order by c.lodged_at desc", haulierId, since));
        history.put("permits", jdbcTemplate.queryForList("select p.reference, p.version, p.status, p.window_start, p.window_end "
                + "from permit p join movement_application a on a.id = p.application_id where a.haulier_id = ? "
                + "and p.issued_at >= ? order by p.issued_at desc, p.version desc", haulierId, since));
        history.put("incidents", jdbcTemplate.queryForList("select incident_date, description from incident "
                + "where haulier_id = ? and incident_date >= ? order by incident_date desc", haulierId, LocalDate.now().minusYears(2)));
        Map<String, Long> byState = new LinkedHashMap<>();
        for (Map<String, Object> c : (List<Map<String, Object>>) history.get("cases")) {
            String key = stateLabel((String) c.get("state"));
            byState.put(key, byState.getOrDefault(key, 0L) + 1);
        }
        history.put("casesByState", byState);
        return history;
    }

    // INC-40250 resend the decision email when a haulier says it never arrived

    /**
     * INC-40250 send the decision email again for a decided case.
     */
    public void resendDecisionEmail(String reference, String username) {
        if (!CurrentUser.hasRole("OFFICER")) {
            throw new BusinessException("NOT_ALLOWED", "Only officers can resend the decision email");
        }
        CaseFile caseFile = findByRef(reference);
        if (!CaseStates.DECIDED.equals(caseFile.getState())) {
            throw new BusinessException("NOT_DECIDED", "Case " + reference + " has not been decided");
        }
        Decision decision = latestDecision(caseFile);
        MovementApplication app = getApplication(caseFile);
        PermitVersion permit = Outcomes.REFUSED.equals(decision.getOutcome()) ? null : currentPermit(caseFile);
        events.publishEvent(new com.halvern.tollgate.platform.events.CaseDecided(reference, caseFile.getId(),
                caseFile.getHaulierId(), app.getHaulierName(), app.getRouteCode(), decision.getOutcome(), decision.getReason(),
                permit == null ? null : permit.getReference(),
                permit == null ? null : permit.getWindowStart(),
                permit == null ? null : permit.getWindowEnd(),
                decision.getDecidedBy(), Instant.now(clock)));
        auditService.recordCase(reference, caseFile.getState(), caseFile.getState(), username, "Decision email sent again");
        log.info("Decision email for {} sent again by {}", reference, username);
    }

    // INC-40330 support tool: put the old status back in step for every case

    /**
     * INC-40330 after the March 2024 data fix some applications showed the wrong status in v1. Runs the legacy
     * status sync for every case.
     * @return number of cases synced
     */
    @Transactional
    public int resyncLegacyStatuses() {
        int synced = 0;
        for (CaseFile caseFile : caseFileRepository.findAll()) {
            try {
                legacyStatusSync.sync(caseFile);
                synced++;
            } catch (Exception e) {
                log.error("Could not sync legacy status for " + caseFile.getReference(), e);
            }
        }
        log.info("Legacy status resync: {} cases", synced);
        return synced;
    }

    // CR-158 short summary of a case for the search page and emails to colleagues

    /**
     * CR-158 one-paragraph summary of a case.
     */
    public String caseSummary(String reference) {
        CaseFile caseFile = findByRef(reference);
        MovementApplication app = getApplication(caseFile);
        StringBuilder summary = new StringBuilder();
        summary.append(reference).append(" (").append(stateLabel(caseFile.getState())).append(")");
        summary.append(": ").append(app.getHaulierName());
        summary.append(", ").append(app.getLoadTypeCode()).append(" ").append(app.getGrossMassKg()).append(" kg");
        summary.append(" on ").append(app.getRouteCode());
        if (caseFile.getTrack() != null) {
            summary.append(", ").append(caseFile.getTrack()).append(" track, score ").append(caseFile.getRiskScore());
        }
        if (caseFile.getAssignedOfficer() != null) {
            summary.append(", officer ").append(caseFile.getAssignedOfficer());
        }
        if (caseFile.getAssignedEngineer() != null) {
            summary.append(", engineer ").append(caseFile.getAssignedEngineer());
        }
        if (caseFile.getOutcome() != null) {
            summary.append(". Outcome ").append(caseFile.getOutcome());
        }
        List<String> warnings = warningsOf(caseFile);
        if (!warnings.isEmpty()) {
            summary.append(". Warnings: ").append(String.join(", ", warnings));
        }
        QueryRound round = openRound(caseFile);
        if (round != null) {
            summary.append(". Query round ").append(round.getRoundNo()).append(" due ").append(round.getDeadline());
        }
        return summary.append(".").toString();
    }

    /**
     * CR-158 days since the case last changed, for the stale case report.
     */
    public long daysSinceLastChange(CaseFile caseFile) {
        LocalDateTime changed = caseFile.getUpdatedAt() == null ? caseFile.getLodgedAt() : caseFile.getUpdatedAt();
        return ChronoUnit.DAYS.between(changed.toLocalDate(), LocalDate.now());
    }

    /**
     * CR-158 open cases with no change for more than the given number of days, oldest first.
     */
    public List<CaseFile> staleCases(int days) {
        List<CaseFile> stale = new ArrayList<>();
        for (CaseFile caseFile : findOpenCases()) {
            if (daysSinceLastChange(caseFile) > days) {
                stale.add(caseFile);
            }
        }
        stale.sort(Comparator.comparing(c -> c.getUpdatedAt() == null ? c.getLodgedAt() : c.getUpdatedAt()));
        return stale;
    }

    // INC-40412 hauliers lodging the same move twice

    /**
     * INC-40412 other open cases from the same haulier on the same route whose movement dates overlap this one.
     */
    public List<String> possibleDuplicates(CaseFile caseFile) {
        MovementApplication app = getApplication(caseFile);
        if (app.getRouteId() == null || app.getFirstMoveDate() == null || app.getLastMoveDate() == null) {
            return new ArrayList<>();
        }
        return jdbcTemplate.queryForList("select c.reference from case_file c join movement_application a on a.id = c.application_id "
                + "where c.haulier_id = ? and a.route_id = ? and c.id <> ? "
                + "and c.state in ('LODGED', 'IN_REVIEW', 'AWAITING_HAULIER', 'AWAITING_PANEL') "
                + "and a.first_move_date <= ? and a.last_move_date >= ? order by c.reference", String.class,
                caseFile.getHaulierId(), app.getRouteId(), caseFile.getId(), app.getLastMoveDate(), app.getFirstMoveDate());
    }

    // CR-168 reprint a permit, for example after a template change

    /**
     * CR-168 print the current permit version again and replace its document.
     */
    @Transactional
    public PermitVersion reprintPermit(String reference, String username) {
        if (!CurrentUser.hasRole("OFFICER")) {
            throw new BusinessException("NOT_ALLOWED", "Only officers can reprint a permit");
        }
        CaseFile caseFile = findByRef(reference);
        PermitVersion permit = currentPermit(caseFile);
        if (permit == null) {
            throw new BusinessException("NO_PERMIT", "Case " + reference + " has no permit");
        }
        if (!Permit.ACTIVE.equals(permit.getStatus()) && !Permit.SUSPENDED.equals(permit.getStatus())) {
            throw new BusinessException("NOT_PRINTABLE", "Permit " + permit.getReference() + " is " + permit.getStatus());
        }
        jdbcTemplate.update("delete from permit_document where permit_id = ?", permit.getId());
        decisionService.renderDocument(caseFile, permit);
        auditService.recordCase(reference, caseFile.getState(), caseFile.getState(), username,
                "Permit " + permit.getReference() + " version " + permit.getVersion() + " reprinted");
        log.info("Permit {} v{} reprinted by {}", permit.getReference(), permit.getVersion(), username);
        return permit;
    }

    // CR-169 cases whose move is close but that are not decided

    /**
     * CR-169 open cases whose first move date is within the given number of days, soonest first.
     */
    public List<Map<String, Object>> movesDueSoon(int days) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select c.reference, h.name as haulier, c.state, c.track, "
                + "c.assigned_officer, a.first_move_date from case_file c join haulier h on h.id = c.haulier_id "
                + "join movement_application a on a.id = c.application_id "
                + "where c.state in ('LODGED', 'IN_REVIEW', 'AWAITING_HAULIER', 'AWAITING_PANEL') "
                + "and a.first_move_date <= ? order by a.first_move_date, c.reference",
                java.sql.Date.valueOf(LocalDate.now().plusDays(days)));
        for (Map<String, Object> row : rows) {
            row.put("stateLabel", stateLabel((String) row.get("state")));
            java.sql.Date first = (java.sql.Date) row.get("first_move_date");
            row.put("daysToMove", ChronoUnit.DAYS.between(LocalDate.now(), first.toLocalDate()));
        }
        return rows;
    }

    /**
     * CR-155 open cases per track.
     */
    public Map<String, Long> countsByTrack() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String t : Arrays.asList("EXPRESS", "DESK", "ENGINEER", "PANEL")) {
            counts.put(t, 0L);
        }
        counts.put("(not assessed)", 0L);
        for (Map<String, Object> row : jdbcTemplate.queryForList("select coalesce(track, '(not assessed)') as track, count(*) as n "
                + "from case_file where state in ('LODGED', 'IN_REVIEW', 'AWAITING_HAULIER', 'AWAITING_PANEL') group by 1")) {
            counts.put((String) row.get("track"), ((Number) row.get("n")).longValue());
        }
        return counts;
    }

    /**
     * CR-155 decisions per outcome over the last twelve months.
     */
    public Map<String, Long> countsByOutcome() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String o : Arrays.asList(Outcomes.ISSUED, Outcomes.ISSUED_WITH_CONDITIONS, Outcomes.REFUSED)) {
            counts.put(o, 0L);
        }
        for (Map<String, Object> row : jdbcTemplate.queryForList("select outcome, count(*) as n from case_file "
                + "where state = 'DECIDED' and decided_at >= ? group by outcome",
                java.sql.Timestamp.valueOf(LocalDate.now().minusYears(1).atStartOfDay()))) {
            counts.put((String) row.get("outcome"), ((Number) row.get("n")).longValue());
        }
        return counts;
    }

    /**
     * CR-172 the status wording the partner support desk uses on the phone, same as the v2 API.
     */
    public String partnerStatus(String reference) {
        CaseFile caseFile = findByRef(reference);
        String state = caseFile.getState();
        if ("DECIDED".equals(state)) {
            return Outcomes.REFUSED.equals(caseFile.getOutcome()) ? "REFUSED" : "APPROVED";
        }
        if ("AWAITING_HAULIER".equals(state)) {
            return "ACTION_REQUIRED";
        }
        if ("WITHDRAWN".equals(state)) {
            return "WITHDRAWN";
        }
        if ("CANCELLED".equals(state)) {
            return "CANCELLED";
        }
        return "IN_PROGRESS";
    }

    // CR-165 weekly digest for the team leads

    /**
     * CR-165 email this month's KPI figures to a team lead.
     */
    public void emailKpiDigest(String to, String username) throws TollgateException {
        if (!CurrentUser.hasRole("OFFICER") && !CurrentUser.hasRole("ADMIN")) {
            throw new BusinessException("NOT_ALLOWED", "Only officers and admins can send the digest");
        }
        // CR-165 figures stay inside Halvern
        String recipient = digestRecipient(to);
        YearMonth month = YearMonth.now();
        Map<String, Object> kpis = monthlyKpis(month);
        StringBuilder body = new StringBuilder();
        body.append("Cases lodged: ").append(kpis.get("lodged")).append("\n");
        Map<String, Long> closed = (Map<String, Long>) kpis.get("closed");
        for (Map.Entry<String, Long> e : closed.entrySet()) {
            body.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        Map<String, Map<String, Object>> tracks = (Map<String, Map<String, Object>>) kpis.get("tracks");
        for (Map.Entry<String, Map<String, Object>> e : tracks.entrySet()) {
            Map<String, Object> t = e.getValue();
            body.append(e.getKey()).append(": ").append(t.get("decided")).append(" decided");
            if (t.get("averageDays") != null) {
                body.append(", average ").append(t.get("averageDays")).append(" days, ")
                        .append(t.get("withinTargetPercent")).append("% within ").append(t.get("target")).append(" days");
            }
            body.append("\n");
        }
        Map<String, String> values = new HashMap<>();
        values.put("month", month.toString());
        values.put("figures", body.toString().trim());
        mailNotifier.send("kpi-digest", recipient, values, "KPI-" + month);
        log.info("KPI digest for {} sent to {} by {}", month, to, username);
    }

    @Autowired
    private MailNotifier mailNotifier;

    // CR-165 one Halvern Crossings address only
    private String digestRecipient(String to) {
        if (to == null || to.contains("\r") || to.contains("\n")) {
            throw new BusinessException("BAD_RECIPIENT", "The digest can only be sent to a Halvern Crossings address");
        }
        javax.mail.internet.InternetAddress[] parsed;
        try {
            parsed = javax.mail.internet.InternetAddress.parse(to.trim(), true);
        } catch (javax.mail.internet.AddressException e) {
            throw new BusinessException("BAD_RECIPIENT", "Enter one email address");
        }
        if (parsed.length != 1) {
            throw new BusinessException("BAD_RECIPIENT", "Enter one email address");
        }
        String address = parsed[0].getAddress().toLowerCase(java.util.Locale.ROOT);
        int at = address.lastIndexOf('@');
        if (at < 0 || !address.substring(at + 1).equals("halvern-crossings.example")) {
            throw new BusinessException("BAD_RECIPIENT", "The digest can only be sent to a Halvern Crossings address");
        }
        return address;
    }

    // CR-178 checklist of what is still outstanding on a case

    /**
     * CR-178 things an officer still has to do on an open case, in the order they usually come up.
     */
    public List<String> outstandingTasks(CaseFile caseFile) {
        List<String> tasks = new ArrayList<>();
        if (isTerminal(caseFile)) {
            return tasks;
        }
        if (caseFile.getAssignedOfficer() == null) {
            tasks.add("Assign an officer");
        }
        if ("LODGED".equals(caseFile.getState())) {
            long pending = questionnaireManager.findQuestions(caseFile.getApplicationId()).stream()
                    .filter(q -> q.isMandatory() && !CaseQuestion.ACCEPTED.equals(q.getReviewStatus())).count();
            if (pending > 0) {
                tasks.add("Review " + pending + " required questionnaire answer" + (pending == 1 ? "" : "s"));
            }
        }
        String track = caseFile.getTrack();
        if (("ENGINEER".equals(track) || "PANEL".equals(track)) && caseFile.getAssignedEngineer() == null) {
            tasks.add("Assign an engineer");
        }
        if (("ENGINEER".equals(track) || "PANEL".equals(track)) && caseFile.getAssignedEngineer() != null
                && latestEngineerAssessment(caseFile) == null) {
            tasks.add("Engineer assessment from " + caseFile.getAssignedEngineer());
        }
        QueryRound round = openRound(caseFile);
        if (round != null) {
            tasks.add("Answers to query round " + round.getRoundNo() + " due " + round.getDeadline());
        }
        if (warningsOf(caseFile).contains("NO_ESCORT_PLAN")) {
            tasks.add("Ask the haulier for an escort plan");
        }
        if (warningsOf(caseFile).contains("SHORT_NOTICE")) {
            tasks.add("Short notice: agree the dates with the haulier");
        }
        if ("IN_REVIEW".equals(caseFile.getState()) && canDecide(caseFile) && round == null) {
            tasks.add("Decide");
        }
        if ("AWAITING_PANEL".equals(caseFile.getState())) {
            tasks.add("Panel decision at the next sitting");
        }
        return tasks;
    }

    // CR-180 an officer's own open cases with SLA status

    /**
     * CR-180 open cases assigned to an officer, with SLA status, most urgent first.
     */
    public List<Map<String, Object>> myCases(String officer) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select c.reference, h.name as haulier, c.state, c.track, "
                + "s.status as sla_status, s.target_days - s.elapsed_days as days_left from case_file c "
                + "join haulier h on h.id = c.haulier_id left join sla_clock s on s.case_id = c.id "
                + "where c.assigned_officer = ? and c.state in ('LODGED', 'IN_REVIEW', 'AWAITING_HAULIER', 'AWAITING_PANEL') "
                + "order by case s.status when 'RED' then 0 when 'AMBER' then 1 else 2 end, days_left", officer);
        for (Map<String, Object> row : rows) {
            row.put("stateLabel", stateLabel((String) row.get("state")));
        }
        return rows;
    }

    // CR-181 assign one engineer to several cases at once

    /**
     * CR-181 assign an engineer to several cases in one go, for the weekly allocation meeting.
     * Same steps as assigning one case on the case page.
     * @return the references assigned
     */
    @Transactional
    public List<String> assignEngineerToCases(List<String> references, String engineer, String officer) throws TollgateException {
        if (!CurrentUser.hasRole("OFFICER")) {
            throw new BusinessException("NOT_ALLOWED", "Only officers can assign an engineer");
        }
        StaffUser user = userDao.findByUsername(engineer);
        if (user == null || !user.hasRole("ENGINEER")) {
            throw new BusinessException("NOT_ENGINEER", engineer + " is not an engineer");
        }
        List<String> assigned = new ArrayList<>();
        for (String reference : references) {
            if (reference == null || reference.trim().isEmpty()) {
                continue;
            }
            CaseFile caseFile = findByRef(reference.trim());
            if (isTerminal(caseFile)) {
                continue;
            }
            caseFile.setAssignedEngineer(engineer);
            if (caseFile.getAssignedOfficer() == null) {
                caseFile.setAssignedOfficer(officer);
            }
            save(caseFile);
            auditService.recordCase(caseFile.getReference(), caseFile.getState(), caseFile.getState(), officer,
                    "Engineer " + engineer + " assigned");
            assigned.add(caseFile.getReference());
        }
        if (!assigned.isEmpty()) {
            Map<String, String> values = new HashMap<>();
            values.put("reference", String.join(", ", assigned));
            values.put("engineer", user.getFullName());
            mailNotifier.send("engineer-assigned", user.getEmail(), values, assigned.get(0));
        }
        return assigned;
    }

    // INC-40470 query rounds past their deadline that the night run has not lapsed yet

    /**
     * INC-40470 open query rounds whose deadline has passed.
     */
    public List<Map<String, Object>> overdueRounds() {
        return jdbcTemplate.queryForList("select c.reference, q.round_no, q.issued_on, q.deadline, c.assigned_officer "
                + "from query_round q join case_file c on c.id = q.case_id where q.status = 'OPEN' and q.deadline < ? "
                + "order by q.deadline", java.sql.Date.valueOf(LocalDate.now()));
    }

    // CR-183 how long open cases have been open

    /**
     * CR-183 open cases by age since lodgement, in calendar days.
     */
    public Map<String, Long> ageBuckets() {
        Map<String, Long> buckets = new LinkedHashMap<>();
        buckets.put("0-5 days", 0L);
        buckets.put("6-10 days", 0L);
        buckets.put("11-20 days", 0L);
        buckets.put("21-40 days", 0L);
        buckets.put("over 40 days", 0L);
        for (java.sql.Timestamp lodged : jdbcTemplate.queryForList("select lodged_at from case_file "
                + "where state in ('LODGED', 'IN_REVIEW', 'AWAITING_HAULIER', 'AWAITING_PANEL')", java.sql.Timestamp.class)) {
            long age = ChronoUnit.DAYS.between(lodged.toLocalDateTime().toLocalDate(), LocalDate.now());
            String key = age <= 5 ? "0-5 days" : age <= 10 ? "6-10 days" : age <= 20 ? "11-20 days" : age <= 40 ? "21-40 days" : "over 40 days";
            buckets.put(key, buckets.get(key) + 1);
        }
        return buckets;
    }

    // CR-184 latest case activity for the dashboard side panel

    /**
     * CR-184 the most recent case changes from the audit trail.
     */
    public List<Map<String, Object>> recentActivity(int limit) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select reference, old_state, new_state, actor, created_at, reason "
                + "from audit_entry where entity_type = 'CASE' order by created_at desc, id desc limit ?", Math.max(1, Math.min(limit, 100)));
        for (Map<String, Object> row : rows) {
            row.put("stateLabel", stateLabel((String) row.get("new_state")));
        }
        return rows;
    }

    /**
     * CR-141 dates typed on the report pages, dd/MM/yyyy or yyyy-MM-dd. Blank gives null.
     */
    public LocalDate parseReportDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String t = text.trim();
        try {
            if (t.contains("/")) {
                return LocalDate.parse(t, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            return LocalDate.parse(t);
        } catch (java.time.format.DateTimeParseException e) {
            throw new BusinessException("BAD_DATE", "Enter dates as dd/mm/yyyy");
        }
    }

    // CR-187 case details as plain text, pasted into emails to the structures team

    /**
     * CR-187 the case, its assessment and its permit as plain text.
     */
    public String caseAsText(String reference) {
        CaseFile caseFile = findByRef(reference);
        MovementApplication app = getApplication(caseFile);
        StringBuilder text = new StringBuilder();
        text.append("Case ").append(reference).append("\n");
        text.append("Haulier: ").append(app.getHaulierName()).append("\n");
        text.append("State: ").append(stateLabel(caseFile.getState())).append("\n");
        text.append("Load: ").append(app.getLoadTypeCode()).append(", ").append(app.getGrossMassKg()).append(" kg, ")
                .append(app.getWidthM()).append(" x ").append(app.getLengthM()).append(" x ").append(app.getHeightM()).append(" m\n");
        text.append("Route: ").append(app.getRouteCode()).append("\n");
        text.append("Movement dates: ").append(movementDates(caseFile)).append("\n");
        Assessment assessment = currentAssessment(caseFile);
        if (assessment != null) {
            text.append("Score: ").append(assessment.getScore()).append(", track ").append(assessment.getFinalTrack()).append("\n");
            for (TriggerHit hit : currentTriggers(caseFile)) {
                text.append("Trigger: ").append(hit.getTriggerCode()).append("\n");
            }
        }
        EngineerAssessment engineer = latestEngineerAssessment(caseFile);
        if (engineer != null) {
            text.append("Engineer: ").append(engineer.getEngineer()).append(", ").append(engineer.getRecommendation()).append("\n");
        }
        PermitVersion permit = currentPermit(caseFile);
        if (permit != null) {
            text.append("Permit: ").append(permit.getReference()).append(" version ").append(permit.getVersion())
                    .append(", ").append(permit.getStatus()).append("\n");
            for (PermitCondition c : conditionsFor(permit)) {
                text.append("Condition: ").append(c.getConditionType());
                if (c.getDetail() != null) {
                    text.append(" - ").append(c.getDetail());
                }
                text.append("\n");
            }
        }
        for (String task : outstandingTasks(caseFile)) {
            text.append("To do: ").append(task).append("\n");
        }
        return text.toString();
    }
}
