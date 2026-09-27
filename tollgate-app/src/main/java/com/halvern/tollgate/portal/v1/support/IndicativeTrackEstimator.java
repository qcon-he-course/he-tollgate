// Partner API - Integration Squad 2021
package com.halvern.tollgate.portal.v1.support;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.intake.MovementApplication;
import com.halvern.tollgate.reference.RouteDao;
import com.halvern.tollgate.reference.RouteSegment;

/**
 * Indicative track for the v1 API, so the portal can show hauliers roughly how their application will be
 * handled before it is assessed. Simplified from the 2021 scoring rules, for indication only (PI-31).
 *
 * @author Aisha Rahman
 */
@Component
public class IndicativeTrackEstimator {

    @Autowired
    private RouteDao routeDao;

    public String estimate(MovementApplication app) {
        if (app.getGrossMassKg() == null || app.getWidthM() == null || app.getLengthM() == null
                || app.getHeightM() == null || app.getRouteId() == null) {
            return null;
        }
        int tonnes = (int) Math.ceil(app.getGrossMassKg() / 1000.0);
        int score = 0;
        if (tonnes > 150) {
            score += 30;
        } else if (tonnes > 80) {
            score += 20;
        } else if (tonnes > 44) {
            score += 10;
        }
        BigDecimal width = app.getWidthM();
        if (width.compareTo(new BigDecimal("5.00")) > 0) {
            score += 15;
        } else if (width.compareTo(new BigDecimal("4.50")) > 0) {
            score += 10;
        } else if (width.compareTo(new BigDecimal("2.90")) > 0) {
            score += 5;
        }
        if (app.getLengthM().compareTo(new BigDecimal("30.00")) > 0) {
            score += 10;
        } else if (app.getLengthM().compareTo(new BigDecimal("20.00")) > 0) {
            score += 5;
        }
        if (app.getHeightM().compareTo(new BigDecimal("4.95")) > 0) {
            score += 5;
        }
        int structures = 0;
        List<RouteSegment> segments = routeDao.findSegments(app.getRouteId());
        for (RouteSegment seg : segments) {
            if (seg.getStructure().isCarrying()) {
                double u = (double) tonnes / seg.getStructure().getMassRatingT();
                structures += u > 0.85 ? 10 : u > 0.70 ? 5 : 0;
            }
        }
        score += Math.min(structures, 30);
        score = Math.min(score, 100);
        if (score >= 60) {
            return "PANEL";
        }
        if (score >= 40) {
            return "ENGINEER";
        }
        if (score >= 20) {
            return "DESK";
        }
        return "EXPRESS";
    }
}
