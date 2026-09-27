package com.halvern.tollgate.determination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.halvern.tollgate.audit.AuditService;
import com.halvern.tollgate.consultation.EngineerAssessmentRepository;
import com.halvern.tollgate.intake.ApplicationDao;
import com.halvern.tollgate.permitdoc.PermitDao;
import com.halvern.tollgate.permitdoc.PermitRenderer;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.flags.FlagService;

/**
 * CR-170 unit tests for DecisionService.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DecisionServiceTest {

    @Mock
    private CaseFileRepository caseFileRepository;

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private PermitVersionRepository permitVersionRepository;

    @Mock
    private PermitConditionRepository permitConditionRepository;

    @Mock
    private EngineerAssessmentRepository engineerAssessmentRepository;

    @Mock
    private ApplicationDao applicationDao;

    @Mock
    private PermitDao permitDao;

    @Mock
    private PermitRenderer permitRenderer;

    @Mock
    private LegacyStatusSync legacyStatusSync;

    @Mock
    private AuditService auditService;

    @Mock
    private FlagService flagService;

    @InjectMocks
    private DecisionService decisionService;

    @Test
    void refuseRequiresReason() {
        assertThrows(BusinessException.class, () -> decisionService.refuse("HX-A-2024-000001", null));
    }

    @Test
    void refuseChecksState() {
        CaseFile caseFile = new CaseFile();
        caseFile.setReference("HX-A-2024-000002");
        caseFile.setState(CaseStates.DECIDED);
        when(caseFileRepository.findByReference("HX-A-2024-000002")).thenReturn(Optional.of(caseFile));
        assertThrows(BusinessException.class, () -> decisionService.refuse("HX-A-2024-000002", "No"));
    }

    @Test
    void decideRejectsPanelCase() {
        CaseFile caseFile = new CaseFile();
        caseFile.setReference("HX-A-2024-000003");
        caseFile.setState(CaseStates.IN_REVIEW);
        caseFile.setTrack("PANEL");
        when(caseFileRepository.findByReference("HX-A-2024-000003")).thenReturn(Optional.of(caseFile));
        assertThrows(BusinessException.class, () -> decisionService.decide("HX-A-2024-000003", true, null, new ArrayList<>(), "officer1"));
        verify(decisionRepository, never()).save(any());
    }

    @Test
    void decideRejectsCaseNotInReview() {
        CaseFile caseFile = new CaseFile();
        caseFile.setReference("HX-A-2024-000004");
        caseFile.setState(CaseStates.LODGED);
        when(caseFileRepository.findByReference("HX-A-2024-000004")).thenReturn(Optional.of(caseFile));
        BusinessException e = assertThrows(BusinessException.class,
                () -> decisionService.decide("HX-A-2024-000004", true, null, new ArrayList<>(), "officer1"));
        assertEquals("NOT_IN_REVIEW", e.getCode());
    }

    @Test
    void renderDocumentUsesRenderer() {
        when(flagService.isEnabled("permit.pdf.legacy-renderer")).thenReturn(false);
        when(permitRenderer.render(any())).thenReturn(new byte[] { 1 });
        byte[] pdf = permitRenderer.render(null);
        assertEquals(1, pdf.length);
        verify(permitDao, never()).insertDocument(anyLong(), any(), any());
    }
}
