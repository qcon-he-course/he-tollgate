package com.halvern.tollgate.consultation;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.audit.AuditService;
import com.halvern.tollgate.determination.CaseFile;
import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.determination.CaseStates;
import com.halvern.tollgate.notify.MailNotifier;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.CurrentUser;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.reference.StaffUser;
import com.halvern.tollgate.reference.UserDao;

import lombok.extern.slf4j.Slf4j;

/**
 * Engineer assignment and engineer assessments (TG-150).
 *
 * @author Dana Kerr
 */
@Slf4j
@Service
public class EngineerAssessmentService {

    @Autowired
    private EngineerAssessmentRepository engineerAssessmentRepository;

    @Autowired
    private CaseService caseService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private MailNotifier mailNotifier;

    @Autowired
    private AuditService auditService;

    @Transactional(rollbackFor = TollgateException.class)
    public CaseFile assignEngineer(String reference, String engineer, String officer) throws TollgateException {
        if (!CurrentUser.hasRole("OFFICER")) {
            throw new BusinessException("NOT_ALLOWED", "Only officers can assign an engineer");
        }
        StaffUser user = userDao.findByUsername(engineer);
        if (user == null || !user.hasRole("ENGINEER")) {
            throw new BusinessException("NOT_ENGINEER", engineer + " is not an engineer");
        }
        CaseFile caseFile = caseService.findByRef(reference);
        caseFile.setAssignedEngineer(engineer);
        if (caseFile.getAssignedOfficer() == null) {
            caseFile.setAssignedOfficer(officer);
        }
        caseService.save(caseFile);
        auditService.recordCase(reference, caseFile.getState(), caseFile.getState(), officer, "Engineer " + engineer + " assigned");

        Map<String, String> values = new HashMap<>();
        values.put("reference", reference);
        values.put("engineer", user.getFullName());
        mailNotifier.send("engineer-assigned", user.getEmail(), values, reference);
        return caseFile;
    }

    @Transactional
    public EngineerAssessment record(String reference, String recommendation, String notes, String proposedConditions,
            String engineer) {
        if (!CurrentUser.hasRole("ENGINEER")) {
            throw new BusinessException("NOT_ALLOWED", "Only engineers can record an engineer assessment");
        }
        if (!Arrays.asList(EngineerAssessment.SATISFACTORY, EngineerAssessment.SATISFACTORY_WITH_CONDITIONS,
                EngineerAssessment.REFER_TO_PANEL, EngineerAssessment.UNSATISFACTORY).contains(recommendation)) {
            throw new BusinessException("BAD_RECOMMENDATION", "Choose a recommendation");
        }
        CaseFile caseFile = caseService.findByRef(reference);
        if (!CaseStates.IN_REVIEW.equals(caseFile.getState()) && !CaseStates.AWAITING_HAULIER.equals(caseFile.getState())) {
            throw new BusinessException("NOT_IN_REVIEW", "Case " + reference + " is not in review");
        }
        EngineerAssessment ea = new EngineerAssessment();
        ea.setCaseId(caseFile.getId());
        ea.setEngineer(engineer);
        ea.setRecommendation(recommendation);
        ea.setNotes(notes);
        ea.setProposedConditions(proposedConditions);
        ea.setRecordedAt(LocalDateTime.now());
        engineerAssessmentRepository.save(ea);
        auditService.recordCase(reference, caseFile.getState(), caseFile.getState(), engineer,
                "Engineer assessment: " + recommendation);

        // Referral to the panel and panel listing follow from the event: see PanelReferralListener
        caseService.engineerAssessmentRecorded(caseFile, ea);
        return ea;
    }
}
