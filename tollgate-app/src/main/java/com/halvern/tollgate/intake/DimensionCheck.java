/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: DimensionCheck.java 1105 2017-10-20 15:36:49Z rmistry $
 */
package com.halvern.tollgate.intake;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.platform.ErrorCodes;
import com.halvern.tollgate.reference.RouteDao;

/**
 * Checks the load against Halvern's absolute limits and the clearance of the route.
 * Implements BR-12.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Component
public class DimensionCheck {

    /** BR-12 absolute limits. */
    private static final int MAX_GROSS_MASS_KG = 250000;
    private static final BigDecimal MAX_WIDTH_M = new BigDecimal("6.10");
    private static final BigDecimal MAX_LENGTH_M = new BigDecimal("50.00");
    private static final BigDecimal MAX_HEIGHT_M = new BigDecimal("5.90");

    /** BR-12: margin required between the load and the lowest overbridge. */
    private static final BigDecimal CLEARANCE_MARGIN_M = new BigDecimal("0.10");

    @Autowired
    private RouteDao routeDao;

    /**
     * Check. Adds a failure to the list for each problem found.
     *
     * @param app the application
     * @param failures the failures found so far
     */
    public void check(MovementApplication app, List<CheckFailure> failures) {
        if (app.getGrossMassKg().intValue() > MAX_GROSS_MASS_KG) {
            failures.add(new CheckFailure(ErrorCodes.MASS_OVER_LIMIT,
                    "Gross mass " + app.getGrossMassKg() + " kg is over the limit of " + MAX_GROSS_MASS_KG + " kg"));
        }
        if (app.getWidthM().compareTo(MAX_WIDTH_M) > 0) {
            failures.add(new CheckFailure(ErrorCodes.WIDTH_OVER_LIMIT,
                    "Width " + app.getWidthM() + " m is over the limit of " + MAX_WIDTH_M + " m"));
        }
        if (app.getLengthM().compareTo(MAX_LENGTH_M) > 0) {
            failures.add(new CheckFailure(ErrorCodes.LENGTH_OVER_LIMIT,
                    "Length " + app.getLengthM() + " m is over the limit of " + MAX_LENGTH_M + " m"));
        }
        if (app.getHeightM().compareTo(MAX_HEIGHT_M) > 0) {
            failures.add(new CheckFailure(ErrorCodes.HEIGHT_OVER_LIMIT,
                    "Height " + app.getHeightM() + " m is over the limit of " + MAX_HEIGHT_M + " m"));
        }
        BigDecimal clearance = routeDao.findMinimumClearance(app.getRouteId().longValue());
        if (clearance != null && app.getHeightM().add(CLEARANCE_MARGIN_M).compareTo(clearance) > 0) {
            failures.add(new CheckFailure(ErrorCodes.HEIGHT_OVER_CLEARANCE,
                    "Height " + app.getHeightM() + " m plus " + CLEARANCE_MARGIN_M + " m margin exceeds the "
                    + clearance + " m clearance on route " + app.getRouteCode()));
        }
    }
}
