package com.halvern.tollgate.determination;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.audit.AuditService;
import com.halvern.tollgate.intake.ApplicationDao;
import com.halvern.tollgate.intake.ApplicationTrip;
import com.halvern.tollgate.intake.CheckFailure;
import com.halvern.tollgate.intake.MovementApplication;
import com.halvern.tollgate.intake.PreCheckResult;
import com.halvern.tollgate.intake.PreCheckService;
import com.halvern.tollgate.permitdoc.Permit;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.ErrorCodes;

import lombok.extern.slf4j.Slf4j;

/**
 * Date amendments to issued permits. A change of load, dimensions or route needs a new application
 * (TG-180).
 *
 * @author Priya Venkat
 */
@Slf4j
@Service
public class AmendmentService {

    @Autowired
    private PermitVersionRepository permitVersionRepository;

    @Autowired
    private PermitConditionRepository permitConditionRepository;

    @Autowired
    private AmendmentRequestRepository amendmentRequestRepository;

    @Autowired
    private CaseFileRepository caseFileRepository;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private PreCheckService preCheckService;

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private AuditService auditService;

    @Transactional
    public AmendmentRequest request(String permitReference, LocalDate newFirst, LocalDate newLast, String username) {
        PermitVersion current = permitVersionRepository.findFirstByReferenceOrderByVersionDesc(permitReference).get();
        if (!Permit.ACTIVE.equals(current.getStatus()) && !Permit.SUSPENDED.equals(current.getStatus())) {
            throw new BusinessException("NOT_AMENDABLE", "Permit " + permitReference + " is " + current.getStatus());
        }
        if (newLast.isBefore(newFirst) || ChronoUnit.DAYS.between(newFirst, newLast) > 13) {
            throw new BusinessException("BAD_WINDOW", "Movement dates can span at most 14 days");
        }

        // TG-183 rerun the licence, insurance and notice checks for the new dates
        MovementApplication app = applicationDao.findById(current.getApplicationId());
        long shift = ChronoUnit.DAYS.between(((Date) app.getFirstMoveDate()).toLocalDate(), newFirst);
        app.setFirstMoveDate(Date.valueOf(newFirst));
        app.setLastMoveDate(Date.valueOf(newLast));
        for (ApplicationTrip trip : app.getTrips()) {
            trip.setDepartureAt(shiftDays(trip.getDepartureAt(), shift));
            if (trip.getCrossingAt() != null) {
                trip.setCrossingAt(shiftDays(trip.getCrossingAt(), shift));
            }
        }
        PreCheckResult checks = preCheckService.check(app);
        List<String> stops = new ArrayList<>();
        for (CheckFailure f : checks.getFailures()) {
            int code = f.getCode();
            if ((code >= ErrorCodes.LICENCE_MISSING && code <= ErrorCodes.LICENCE_EXPIRES)
                    || code == ErrorCodes.INSURANCE_MISSING || code == ErrorCodes.INSURANCE_NOT_COVERED) {
                stops.add(f.getMessage());
            }
        }
        if (!stops.isEmpty()) {
            throw new BusinessException("AMENDMENT_REFUSED", String.join("; ", stops));
        }
        List<String> warnings = checks.getWarnings().stream().filter(PreCheckResult.SHORT_NOTICE::equals)
                .collect(Collectors.toList());

        AmendmentRequest req = new AmendmentRequest();
        req.setCaseId(current.getCaseId());
        req.setPermitId(current.getId());
        req.setNewFirstMoveDate(newFirst);
        req.setNewLastMoveDate(newLast);
        req.setStatus(AmendmentRequest.REQUESTED);
        req.setWarnings(warnings.isEmpty() ? null : String.join(",", warnings));
        req.setRequestedBy(username);
        req.setRequestedAt(LocalDateTime.now());
        amendmentRequestRepository.save(req);
        log.info("Amendment {} requested on {} for {} to {}", req.getId(), permitReference, newFirst, newLast);
        return req;
    }

    private java.util.Date shiftDays(java.util.Date time, long days) {
        return java.util.Date.from(time.toInstant().atZone(ZoneId.systemDefault()).plusDays(days).toInstant());
    }

    @PreAuthorize("hasRole('OFFICER')")
    @Transactional
    public PermitVersion approve(Long amendmentId, String username) {
        AmendmentRequest req = amendmentRequestRepository.findById(amendmentId).get();
        if (!AmendmentRequest.REQUESTED.equals(req.getStatus())) {
            throw new BusinessException("NOT_PENDING", "Amendment " + amendmentId + " is " + req.getStatus());
        }
        PermitVersion current = permitVersionRepository.findById(req.getPermitId()).get();
        PermitVersion latest = permitVersionRepository.findFirstByReferenceOrderByVersionDesc(current.getReference()).get();
        if (!latest.getId().equals(current.getId())) {
            throw new BusinessException("STALE", "The permit has changed since the amendment was requested");
        }
        String oldStatus = current.getStatus();
        current.setStatus(PermitVersion.SUPERSEDED);
        current.setStatusChangedAt(LocalDateTime.now());
        permitVersionRepository.save(current);

        PermitVersion next = new PermitVersion();
        next.setReference(current.getReference());
        next.setApplicationId(current.getApplicationId());
        next.setCaseId(current.getCaseId());
        next.setVersion(current.getVersion() + 1);
        next.setStatus(Permit.ACTIVE);
        next.setWindowStart(req.getNewFirstMoveDate());
        next.setWindowEnd(req.getNewLastMoveDate());
        next.setIssuedBy(username);
        next.setIssuedAt(LocalDateTime.now());
        permitVersionRepository.save(next);
        for (PermitCondition c : permitConditionRepository.findByPermitId(current.getId())) {
            PermitCondition copy = new PermitCondition();
            copy.setCaseId(c.getCaseId());
            copy.setPermitId(next.getId());
            copy.setConditionType(c.getConditionType());
            copy.setDetail(c.getDetail());
            copy.setEscortVehicles(c.getEscortVehicles());
            copy.setStructureCode(c.getStructureCode());
            copy.setSource(c.getSource());
            permitConditionRepository.save(copy);
        }
        req.setStatus(AmendmentRequest.APPROVED);
        req.setDecidedBy(username);
        req.setDecidedAt(LocalDateTime.now());
        amendmentRequestRepository.save(req);

        auditService.recordPermit(current.getReference(), oldStatus, PermitVersion.SUPERSEDED, username,
                "Version " + current.getVersion() + " superseded by amendment");
        auditService.recordPermit(next.getReference(), null, Permit.ACTIVE, username,
                "Version " + next.getVersion() + ": " + next.getWindowStart() + " to " + next.getWindowEnd());
        decisionService.renderDocument(caseFileRepository.findById(next.getCaseId()).get(), next);
        return next;
    }

    @PreAuthorize("hasRole('OFFICER')")
    @Transactional
    public AmendmentRequest reject(Long amendmentId, String reason, String username) {
        AmendmentRequest req = amendmentRequestRepository.findById(amendmentId).get();
        if (!AmendmentRequest.REQUESTED.equals(req.getStatus())) {
            throw new BusinessException("NOT_PENDING", "Amendment " + amendmentId + " is " + req.getStatus());
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("REASON_REQUIRED", "Give a reason for rejecting the amendment");
        }
        req.setStatus(AmendmentRequest.REJECTED);
        req.setReason(reason.trim());
        req.setDecidedBy(username);
        req.setDecidedAt(LocalDateTime.now());
        return amendmentRequestRepository.save(req);
    }
}
