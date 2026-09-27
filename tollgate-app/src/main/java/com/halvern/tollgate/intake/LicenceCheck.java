/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: LicenceCheck.java 1104 2017-10-20 11:08:15Z rmistry $
 */
package com.halvern.tollgate.intake;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.platform.ErrorCodes;
import com.halvern.tollgate.reference.HaulierDao;
import com.halvern.tollgate.reference.OperatorLicence;

/**
 * Checks the haulier's operator licence covers the whole move.
 * Implements BR-11.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Component
public class LicenceCheck {

    private static final Logger log = LoggerFactory.getLogger(LicenceCheck.class);

    @Autowired
    private HaulierDao haulierDao;

    /**
     * Check. Adds a failure to the list for each problem found.
     *
     * @param app the application, with trips loaded
     * @param failures the failures found so far
     */
    public void check(MovementApplication app, List<CheckFailure> failures) {
        OperatorLicence licence = haulierDao.findLicence(app.getHaulierId().longValue());
        if (licence == null) {
            failures.add(new CheckFailure(ErrorCodes.LICENCE_MISSING,
                    "No operator licence is on file for " + app.getHaulierName()));
            return;
        }
        // BR-11: the licence must be in force when the move starts
        if (licence.getValidFrom().after(app.getFirstMoveDate())) {
            failures.add(new CheckFailure(ErrorCodes.LICENCE_NOT_YET_VALID,
                    "Operator licence " + licence.getLicenceNumber() + " is not valid until "
                    + LegacyDateUtil.formatDate(licence.getValidFrom())));
        }
        // BR-11: and still in force for the last trip
        Date lastDeparture = app.getLastDeparture();
        if (lastDeparture != null && licence.getExpiryDate().before(lastDeparture)) {
            failures.add(new CheckFailure(ErrorCodes.LICENCE_EXPIRES,
                    "Operator licence " + licence.getLicenceNumber() + " expires on "
                    + LegacyDateUtil.formatDate(licence.getExpiryDate()) + ", before the last trip"));
        }
        log.debug("Licence check for " + app.getReference() + ": " + failures.size() + " failures so far");
    }
}
