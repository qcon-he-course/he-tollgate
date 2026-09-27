/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: PermitManager.java 1191 2018-03-20 10:15:02Z gferrand $
 */
package com.halvern.tollgate.permitdoc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.audit.AuditEntry;
import com.halvern.tollgate.audit.AuditService;
import com.halvern.tollgate.platform.DateHelper;

/**
 * Expires permits when their movement dates pass.
 * Implements BR-30, BR-31, BR-35 and BR-36.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Service
public class PermitManager {

    private static final Logger log = LoggerFactory.getLogger(PermitManager.class);

    @Autowired
    private PermitDao permitDao;







    @Autowired
    private AuditService auditService;


    /**
     * Expire permits whose movement dates have passed.
     *
     * @return the number of permits expired
     */
    @Transactional
    public int expirePermits() {
        List<Permit> permits = permitDao.findToExpire(DateHelper.today());
        for (Permit p : permits) {
            permitDao.updateStatus(p.getId().longValue(), Permit.EXPIRED, "Movement dates have passed", null);
            auditService.recordPermit(p.getReference(), p.getStatus(), Permit.EXPIRED, AuditEntry.SYSTEM,
                    "Movement dates have passed");
        }
        log.info("Expired " + permits.size() + " permits");
        return permits.size();
    }
}
