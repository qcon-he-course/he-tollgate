package com.halvern.tollgate.determination;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.audit.AuditService;
import com.halvern.tollgate.permitdoc.Permit;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.events.PermitStatusChanged;

import lombok.extern.slf4j.Slf4j;

/**
 * CR-122 permit status changes from the case page form. Same steps as CaseService.revokePermit.
 */
@Slf4j
@Service
public class PermitStatusService {

    @Autowired
    private CaseFileRepository caseFileRepository;

    @Autowired
    private PermitVersionRepository permitVersionRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ApplicationEventPublisher events;

    @Autowired
    private Clock clock;

    @Transactional
    public PermitVersion revoke(String reference, String reason, String username) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("REASON_REQUIRED", "Give a reason for revoking the permit");
        }
        CaseFile caseFile = caseFileRepository.findByReference(reference).get();
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
        events.publishEvent(new PermitStatusChanged(permit.getReference(), reference, caseFile.getHaulierId(), oldStatus,
                Permit.REVOKED, reason.trim(), username, Instant.now(clock)));
        log.info("Permit {} revoked by {}", permit.getReference(), username);
        return permit;
    }
}
