/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: ClosureManager.java 1192 2018-03-20 14:48:31Z rmistry $
 */
package com.halvern.tollgate.permitdoc;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.audit.AuditService;
import com.halvern.tollgate.notify.MailNotifier;
import com.halvern.tollgate.platform.DateHelper;
import com.halvern.tollgate.platform.ErrorCodes;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.reference.HaulierDao;
import com.halvern.tollgate.reference.Structure;
import com.halvern.tollgate.reference.StructureDao;

/**
 * Records structure closures, suspending the permits they affect, and ends them, reinstating
 * those permits.
 * Implements BR-33 and BR-34.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Service
public class ClosureManager {

    private static final Logger log = LoggerFactory.getLogger(ClosureManager.class);

    @Autowired
    private ClosureDao closureDao;

    @Autowired
    private PermitDao permitDao;

    @Autowired
    private StructureDao structureDao;

    @Autowired
    private HaulierDao haulierDao;

    @Autowired
    private AuditService auditService;

    @Autowired
    private MailNotifier mailNotifier;

    /**
     * Record a closure and suspend every active permit crossing the structure during it.
     *
     * @param closure the closure
     * @return the permits suspended
     * @throws TollgateException if the dates are invalid
     */
    @Transactional(rollbackFor = TollgateException.class)
    public List<Permit> recordClosure(StructureClosure closure) throws TollgateException {
        if (closure.getEndDate().before(closure.getStartDate())) {
            throw new TollgateException(ErrorCodes.CLOSURE_DATES_INVALID, "The closure ends before it starts");
        }
        long closureId = closureDao.insert(closure);
        List<Permit> affected = permitDao.findActiveCrossing(closure.getStructureId().longValue(),
                closure.getStartDate(), closure.getEndDate());
        Structure structure = structureDao.findById(closure.getStructureId().longValue());
        String reason = "Closure of " + structure.getCode() + " " + structure.getName();
        for (Permit p : affected) {
            permitDao.updateStatus(p.getId().longValue(), Permit.SUSPENDED, reason, Long.valueOf(closureId));
            auditService.recordPermit(p.getReference(), Permit.ACTIVE, Permit.SUSPENDED, closure.getRecordedBy(), reason);
            Map<String, String> values = permitValues(p, structure);
            values.put("closureStart", DateHelper.formatScreenDate(closure.getStartDate()));
            values.put("closureEnd", DateHelper.formatScreenDate(closure.getEndDate()));
            mailNotifier.send("permit-suspended", haulierDao.findById(p.getHaulierId().longValue()).getContactEmail(),
                    values, p.getReference());
        }
        log.info("Closure " + closureId + " suspended " + affected.size() + " permits");
        return affected;
    }

    /**
     * End a closure. Permits it suspended become active again, or expired if their movement dates
     * have passed.
     *
     * @param closureId the closure id
     * @param username the user ending the closure
     * @return the permits reinstated or expired
     * @throws TollgateException if the closure is not active
     */
    @Transactional(rollbackFor = TollgateException.class)
    public List<Permit> endClosure(long closureId, String username) throws TollgateException {
        if (!closureDao.end(closureId, username)) {
            throw new TollgateException(ErrorCodes.CLOSURE_NOT_FOUND, "Closure " + closureId + " is not active");
        }
        Date today = DateHelper.today();
        StructureClosure closure = closureDao.findById(closureId);
        Structure structure = structureDao.findById(closure.getStructureId().longValue());
        String reason = "Closure of " + structure.getCode() + " " + structure.getName() + " ended";
        List<Permit> suspended = permitDao.findSuspendedByClosure(closureId);
        for (Permit p : suspended) {
            // TG-185 amended permits carry their own window
            if (p.getWindowEnd().before(today)) {
                permitDao.updateStatus(p.getId().longValue(), Permit.EXPIRED, "Movement dates have passed", null);
                auditService.recordPermit(p.getReference(), Permit.SUSPENDED, Permit.EXPIRED, username,
                        "Movement dates have passed");
            } else {
                permitDao.updateStatus(p.getId().longValue(), Permit.ACTIVE, reason, null);
                auditService.recordPermit(p.getReference(), Permit.SUSPENDED, Permit.ACTIVE, username, reason);
                mailNotifier.send("permit-reinstated",
                        haulierDao.findById(p.getHaulierId().longValue()).getContactEmail(),
                        permitValues(p, structure), p.getReference());
            }
        }
        log.info("Closure " + closureId + " ended by " + username + "; " + suspended.size() + " permits updated");
        return suspended;
    }

    private Map<String, String> permitValues(Permit p, Structure structure) {
        Map<String, String> values = new HashMap<String, String>();
        values.put("permitReference", p.getReference());
        values.put("haulierName", p.getHaulierName());
        values.put("structure", structure.getCode() + " " + structure.getName());
        values.put("firstMoveDate", DateHelper.formatScreenDate(p.getWindowStart()));
        values.put("lastMoveDate", DateHelper.formatScreenDate(p.getWindowEnd()));
        return values;
    }

    /**
     * End closures whose last day has passed.
     *
     * @param username the account running the job
     * @return the number of closures ended
     */
    @Transactional
    public int endFinishedClosures(String username) {
        List<StructureClosure> finished = closureDao.findActiveEndingBefore(DateHelper.today());
        int ended = 0;
        for (StructureClosure c : finished) {
            try {
                endClosure(c.getId().longValue(), username);
                ended++;
            } catch (TollgateException e) {
                log.warn("Closure " + c.getId() + " not ended: " + e.getMessage());
            }
        }
        return ended;
    }
}
