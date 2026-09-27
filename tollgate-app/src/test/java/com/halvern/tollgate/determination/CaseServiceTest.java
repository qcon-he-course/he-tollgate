package com.halvern.tollgate.determination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.halvern.tollgate.assessment.Assessment;
import com.halvern.tollgate.assessment.AssessmentService;
import com.halvern.tollgate.audit.AuditEntry;
import com.halvern.tollgate.audit.AuditService;
import com.halvern.tollgate.consultation.EngineerAssessment;
import com.halvern.tollgate.consultation.PanelListingRepository;
import com.halvern.tollgate.consultation.PanelSittingRepository;
import com.halvern.tollgate.consultation.QueryRound;
import com.halvern.tollgate.consultation.QueryRoundService;
import com.halvern.tollgate.intake.ApplicationDao;
import com.halvern.tollgate.intake.ApplicationManager;
import com.halvern.tollgate.intake.MovementApplication;
import com.halvern.tollgate.notify.MailNotifier;
import com.halvern.tollgate.notify.NotificationLogDao;
import com.halvern.tollgate.notify.NotificationLogEntry;
import com.halvern.tollgate.permitdoc.PermitDao;
import com.halvern.tollgate.permitdoc.PermitStatusHistoryDao;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.flags.FlagService;
import com.halvern.tollgate.questionnaire.QuestionnaireManager;
import com.halvern.tollgate.reference.Haulier;
import com.halvern.tollgate.reference.HaulierDao;
import com.halvern.tollgate.reference.StaffUser;
import com.halvern.tollgate.reference.UserDao;
import com.halvern.tollgate.sla.clock.SlaClockService;

/**
 * CR-170 unit tests for CaseService.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseServiceTest {

    @Mock private CaseFileRepository caseFileRepository;
    @Mock private ApplicationManager applicationManager;
    @Mock private ApplicationDao applicationDao;
    @Mock private LegacyStatusSync legacyStatusSync;
    @Mock private AuditService auditService;
    @Mock private AssessmentService assessmentService;
    @Mock private QuestionnaireManager questionnaireManager;
    @Mock private QueryRoundService queryRoundService;
    @Mock private DecisionService decisionService;
    @Mock private PermitVersionRepository permitVersionRepository;
    @Mock private HaulierDao haulierDao;
    @Mock private PermitConditionRepository permitConditionRepository;
    @Mock private AmendmentRequestRepository amendmentRequestRepository;
    @Mock private DecisionRepository decisionRepository;
    @Mock private PanelListingRepository panelListingRepository;
    @Mock private PanelSittingRepository panelSittingRepository;
    @Mock private NotificationLogDao notificationLogDao;
    @Mock private UserDao userDao;
    @Mock private PermitDao permitDao;
    @Mock private ApplicationEventPublisher events;
    @Mock private SlaClockService slaClockService;
    @Mock private PermitStatusHistoryDao permitStatusHistoryDao;
    @Mock private FlagService flagService;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private MailNotifier mailNotifier;

    @InjectMocks
    private CaseService caseService;

    private CaseFile caseFile;
    private MovementApplication app;

    @BeforeEach
    void setUp() {
        caseFile = new CaseFile();
        caseFile.setId(1L);
        caseFile.setReference("HX-A-2024-000101");
        caseFile.setApplicationId(10L);
        caseFile.setHaulierId(1L);
        caseFile.setState(CaseStates.IN_REVIEW);
        caseFile.setTrack("DESK");
        caseFile.setRiskScore(20);
        caseFile.setAssignedOfficer("officer1");
        caseFile.setLodgedAt(LocalDateTime.now().minusDays(20));
        caseFile.setUpdatedAt(LocalDateTime.now().minusDays(15));
        app = new MovementApplication();
        app.setId(10L);
        app.setReference("HX-A-2024-000101");
        app.setHaulierName("Marram Heavy Haul");
        app.setLoadTypeCode("TRANSFORMER");
        app.setGrossMassKg(60000);
        app.setWidthM(new BigDecimal("3.50"));
        app.setLengthM(new BigDecimal("22.00"));
        app.setHeightM(new BigDecimal("4.00"));
        app.setRouteId(4L);
        app.setRouteCode("R4");
        app.setFirstMoveDate(Date.valueOf(LocalDate.now().plusDays(20)));
        app.setLastMoveDate(Date.valueOf(LocalDate.now().plusDays(21)));
        when(caseFileRepository.findByReference("HX-A-2024-000101")).thenReturn(Optional.of(caseFile));
        when(caseFileRepository.findByStateIn(any())).thenReturn(new ArrayList<>(List.of(caseFile)));
        when(applicationDao.findById(10L)).thenReturn(app);
        org.springframework.test.util.ReflectionTestUtils.setField(caseService, "clock", java.time.Clock.systemUTC());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("officer1", null,
                List.of(new SimpleGrantedAuthority("ROLE_OFFICER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private StaffUser user(String username, String role) {
        StaffUser u = new StaffUser();
        u.setUsername(username);
        u.setFullName(username);
        u.setEmail(username + "@halvern-crossings.example");
        u.setEnabled(true);
        u.setRoles(new ArrayList<>(List.of(role)));
        return u;
    }

    @Test
    void findByRefReturnsCase() {
        assertEquals(caseFile, caseService.findByRef("HX-A-2024-000101"));
    }

    @Test
    void changeStateSavesAndAudits() {
        caseService.changeState(caseFile, CaseStates.AWAITING_HAULIER, "officer1", "test");
        verify(caseFileRepository).save(caseFile);
        verify(auditService).recordCase(anyString(), any(), any(), anyString(), any());
        verify(legacyStatusSync).sync(caseFile);
    }

    @Test
    void changeStateToTerminalStopsClock() {
        caseService.changeState(caseFile, CaseStates.CANCELLED, "officer1", "test");
        verify(slaClockService).stop(eq(1L), any());
    }

    @Test
    void withdrawClosesRounds() {
        caseService.withdraw("HX-A-2024-000101", "officer1", "Haulier asked");
        verify(queryRoundService).closeOpenRounds(caseFile);
    }

    @Test
    void cancelNeedsReason() {
        assertThrows(BusinessException.class, () -> caseService.cancel("HX-A-2024-000101", " ", "officer1"));
    }

    @Test
    void cancelCase() {
        caseService.cancel("HX-A-2024-000101", "Duplicate", "officer1");
        verify(queryRoundService).closeOpenRounds(caseFile);
    }

    @Test
    void exportCasesHasHeader() {
        String csv = caseService.exportCases("", "", LocalDate.now().minusDays(30), LocalDate.now());
        assertTrue(csv.startsWith("reference,haulier"));
    }

    @Test
    void exportCasesRejectsBadDates() {
        assertThrows(BusinessException.class, () -> caseService.exportCases(null, null, LocalDate.now(), LocalDate.now().minusDays(1)));
    }

    @Test
    void exportCasesWritesRows() {
        Map<String, Object> row = new java.util.HashMap<>();
        row.put("reference", "HX-A-2024-000101");
        row.put("haulier", "=Marram, \"Heavy\"");
        when(jdbcTemplate.queryForList(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(List.of(row));
        String csv = caseService.exportCases(null, null, null, null);
        assertNotNull(csv);
    }

    @Test
    void searchCasesReturnsRows() {
        List<Map<String, Object>> rows = caseService.searchCases("transformer", "IN_REVIEW", "DESK", LocalDate.now().minusDays(10), LocalDate.now());
        assertEquals(0, rows.size());
    }

    @Test
    void stateLabels() {
        for (String s : Arrays.asList("LODGED", "IN_REVIEW", "AWAITING_HAULIER", "AWAITING_PANEL", "DECIDED", "WITHDRAWN", "CANCELLED", "LAPSED", "OTHER")) {
            assertNotNull(caseService.stateLabel(s));
        }
        assertEquals("", caseService.stateLabel(null));
    }

    @Test
    void reassignOfficerMovesCases() {
        when(userDao.findByUsername("officer2")).thenReturn(user("officer2", "OFFICER"));
        List<String> moved = caseService.reassignOfficer("officer1", "officer2", "admin1");
        assertEquals(1, moved.size());
        verify(auditService).recordCase(anyString(), any(), any(), eq("admin1"), anyString());
    }

    @Test
    void reassignOfficerNeedsTwoOfficers() {
        assertThrows(BusinessException.class, () -> caseService.reassignOfficer("officer1", "officer1", "admin1"));
    }

    @Test
    void openCasesByOfficer() {
        when(userDao.findAll()).thenReturn(List.of(user("officer1", "OFFICER")));
        Map<String, Long> counts = caseService.openCasesByOfficer();
        assertEquals(1L, counts.get("officer1"));
    }

    @Test
    void monthlyKpis() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(), any())).thenReturn(5L);
        Map<String, Object> row = new java.util.HashMap<>();
        row.put("state", "DECIDED");
        row.put("outcome", "ISSUED");
        row.put("track", "DESK");
        row.put("lodged_at", java.sql.Timestamp.valueOf(LocalDateTime.now().minusDays(8)));
        row.put("decided_at", java.sql.Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        when(jdbcTemplate.queryForList(anyString(), any(java.sql.Timestamp.class), any(java.sql.Timestamp.class))).thenReturn(List.of(row));
        Map<String, Object> kpis = caseService.monthlyKpis(YearMonth.now());
        assertEquals(5L, kpis.get("lodged"));
    }

    @Test
    void workingDaysBetween() {
        assertEquals(0, caseService.workingDaysBetween(LocalDate.of(2024, 3, 5), LocalDate.of(2024, 3, 4)));
        assertTrue(caseService.workingDaysBetween(LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 11)) > 0);
    }

    @Test
    void haulierHistory() {
        Haulier haulier = new Haulier();
        haulier.setName("Marram Heavy Haul");
        when(haulierDao.findById(1L)).thenReturn(haulier);
        Map<String, Object> history = caseService.haulierHistory(1L);
        assertEquals(haulier, history.get("haulier"));
    }

    @Test
    void resendDecisionEmail() {
        caseFile.setState(CaseStates.DECIDED);
        caseFile.setOutcome(Outcomes.REFUSED);
        Decision decision = new Decision();
        decision.setOutcome(Outcomes.REFUSED);
        decision.setReason("Too heavy");
        decision.setDecidedBy("officer1");
        when(decisionRepository.findByCaseIdOrderByDecidedAtAsc(1L)).thenReturn(List.of(decision));
        caseService.resendDecisionEmail("HX-A-2024-000101", "officer1");
        verify(events).publishEvent(any(Object.class));
    }

    @Test
    void resendNeedsDecidedCase() {
        assertThrows(BusinessException.class, () -> caseService.resendDecisionEmail("HX-A-2024-000101", "officer1"));
    }

    @Test
    void resyncLegacyStatuses() {
        when(caseFileRepository.findAll()).thenReturn(List.of(caseFile, caseFile));
        assertEquals(2, caseService.resyncLegacyStatuses());
        verify(legacyStatusSync, times(2)).sync(caseFile);
    }

    @Test
    void caseSummary() {
        String summary = caseService.caseSummary("HX-A-2024-000101");
        assertTrue(summary.contains("HX-A-2024-000101"));
    }

    @Test
    void staleCases() {
        caseService.staleCases(10);
    }

    @Test
    void possibleDuplicates() {
        caseService.possibleDuplicates(caseFile);
    }

    @Test
    void outstandingTasks() {
        caseService.outstandingTasks(caseFile);
    }

    @Test
    void outstandingTasksForEngineerCase() {
        caseFile.setTrack("ENGINEER");
        caseFile.setWarnings("NO_ESCORT_PLAN,SHORT_NOTICE");
        caseFile.setAssignedOfficer(null);
        List<String> tasks = caseService.outstandingTasks(caseFile);
        assertTrue(tasks.size() > 0);
    }

    @Test
    void reprintPermit() {
        PermitVersion permit = new PermitVersion();
        permit.setId(5L);
        permit.setReference("HX-P-2024-00001");
        permit.setVersion(1);
        permit.setStatus("ACTIVE");
        when(permitVersionRepository.findByCaseIdOrderByVersionAsc(1L)).thenReturn(List.of(permit));
        caseService.reprintPermit("HX-A-2024-000101", "officer1");
        verify(decisionService).renderDocument(caseFile, permit);
    }

    @Test
    void reprintNeedsPermit() {
        assertThrows(BusinessException.class, () -> caseService.reprintPermit("HX-A-2024-000101", "officer1"));
    }

    @Test
    void movesDueSoon() {
        assertEquals(0, caseService.movesDueSoon(10).size());
    }

    @Test
    void countsByTrackAndOutcome() {
        assertEquals(0L, caseService.countsByTrack().get("DESK"));
        assertEquals(0L, caseService.countsByOutcome().get("ISSUED"));
    }

    @Test
    void partnerStatus() {
        assertEquals("IN_PROGRESS", caseService.partnerStatus("HX-A-2024-000101"));
        caseFile.setState(CaseStates.AWAITING_HAULIER);
        assertEquals("ACTION_REQUIRED", caseService.partnerStatus("HX-A-2024-000101"));
        caseFile.setState(CaseStates.DECIDED);
        caseFile.setOutcome(Outcomes.ISSUED);
        assertEquals("APPROVED", caseService.partnerStatus("HX-A-2024-000101"));
    }

    @Test
    void emailKpiDigest() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(), any())).thenReturn(1L);
        caseService.emailKpiDigest("lead1@halvern-crossings.example", "lead1");
        verify(mailNotifier).send(eq("kpi-digest"), anyString(), any(), anyString());
    }

    @Test
    void digestRejectsOtherAddresses() {
        assertThrows(BusinessException.class, () -> caseService.emailKpiDigest("x@evil.example,lead1@halvern-crossings.example", "lead1"));
        assertThrows(BusinessException.class, () -> caseService.emailKpiDigest("lead1@halvern-crossings.example.evil.example", "lead1"));
    }

    @Test
    void myCases() {
        assertEquals(0, caseService.myCases("officer1").size());
    }

    @Test
    void assignEngineerToCases() throws Exception {
        when(userDao.findByUsername("engineer1")).thenReturn(user("engineer1", "ENGINEER"));
        List<String> assigned = caseService.assignEngineerToCases(List.of("HX-A-2024-000101", " "), "engineer1", "officer1");
        assertEquals(1, assigned.size());
        verify(mailNotifier).send(eq("engineer-assigned"), anyString(), any(), anyString());
    }

    @Test
    void overdueRoundsAndBuckets() {
        assertEquals(0, caseService.overdueRounds().size());
        assertEquals(5, caseService.ageBuckets().size());
        assertEquals(0, caseService.recentActivity(10).size());
    }

    @Test
    void parseReportDate() {
        assertEquals(LocalDate.of(2024, 3, 4), caseService.parseReportDate("04/03/2024"));
        assertEquals(LocalDate.of(2024, 3, 4), caseService.parseReportDate("2024-03-04"));
        assertThrows(BusinessException.class, () -> caseService.parseReportDate("March"));
    }

    @Test
    void caseAsText() {
        Assessment assessment = new Assessment();
        assessment.setScore(20);
        assessment.setFinalTrack("DESK");
        caseFile.setAssessments(new ArrayList<>(List.of(assessment)));
        EngineerAssessment ea = new EngineerAssessment();
        ea.setEngineer("engineer1");
        ea.setRecommendation("SATISFACTORY");
        caseFile.setEngineerAssessments(new ArrayList<>(List.of(ea)));
        String text = caseService.caseAsText("HX-A-2024-000101");
        assertTrue(text.contains("Marram"));
    }

    @Test
    void escalationAndAuditHelpers() {
        AuditEntry entry = new AuditEntry();
        entry.setEntityType(AuditEntry.CASE);
        entry.setActor("officer1");
        entry.setReason("Escalated from DESK to ENGINEER");
        when(auditService.findHistory("HX-A-2024-000101")).thenReturn(List.of(entry));
        assertEquals(1, caseService.escalationHistory(caseFile).size());
        assertTrue(caseService.wasEscalated(caseFile));
        assertEquals("officer1", caseService.lastActor(caseFile));
        assertEquals(1, caseService.caseAuditTrail(caseFile).size());
    }

    @Test
    void permitHelpers() {
        PermitVersion permit = new PermitVersion();
        permit.setStatus("ACTIVE");
        permit.setWindowStart(LocalDate.now());
        permit.setWindowEnd(LocalDate.now().plusDays(2));
        when(permitVersionRepository.findByCaseIdOrderByVersionAsc(1L)).thenReturn(List.of(permit));
        assertEquals(3, caseService.permitWindowDays(caseFile));
        assertEquals(1, caseService.permitVersionCount(caseFile));
        assertTrue(caseService.canAmend(caseFile));
        assertEquals(0, caseService.pendingAmendments(caseFile).size());
    }

    @Test
    void permissionHelpers() {
        assertTrue(caseService.canWithdraw(caseFile));
        assertTrue(caseService.canCancel(caseFile));
        caseService.canAssignEngineer(caseFile);
        caseService.canAnswerQueryRound(caseFile);
        caseFile.setDecidedAt(LocalDateTime.now());
        assertNotNull(caseService.hoursToDecision(caseFile));
    }

    @Test
    void workloadLists() {
        when(userDao.findAll()).thenReturn(List.of(user("officer1", "OFFICER"), user("engineer1", "ENGINEER")));
        caseFile.setAssignedEngineer("engineer1");
        assertEquals(1, caseService.engineersByWorkload().size());
        assertEquals(1, caseService.officersByWorkload().size());
        assertEquals(1, caseService.findOpenCasesForTrack("DESK").size());
        assertEquals(1, caseService.findOpenCasesForOfficer("officer1").size());
        assertNotNull(caseService.findAwaitingEngineer());
    }

    @Test
    void notificationHelpers() {
        NotificationLogEntry sent = new NotificationLogEntry();
        sent.setTemplate("application-lodged");
        sent.setStatus(NotificationLogEntry.SENT);
        when(notificationLogDao.findForApplication("HX-A-2024-000101")).thenReturn(List.of(sent));
        assertTrue(caseService.notificationSent(caseFile, "application-lodged"));
        assertEquals(0, caseService.failedNotifications(caseFile).size());
        assertEquals(1L, caseService.notificationCounts(caseFile).get("application-lodged"));
    }

    @Test
    void dateHelpers() {
        assertTrue(caseService.daysToFirstMove(caseFile) > 0);
        assertTrue(caseService.noticeDays(caseFile) > 0);
        assertNotNull(caseService.movementDates(caseFile));
    }

    @Test
    void openRoundAndViewHelpers() {
        QueryRound round = new QueryRound();
        round.setStatus(QueryRound.OPEN);
        round.setRoundNo(1);
        caseFile.setQueryRounds(new ArrayList<>(List.of(round)));
        assertEquals(round, caseService.openRound(caseFile));
        assertEquals(Collections.emptyList(), caseService.warningsOf(new CaseFile()));
    }
}
