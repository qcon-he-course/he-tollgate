package com.halvern.tollgate.assessment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.reference.Structure;

/**
 * Conditions every permit for the load must carry (TG-161).
 *
 * @author Tom Hollis
 */
@Component
public class MandatoryConditionGenerator {

    @Autowired
    private RiskScoringService riskScoringService;

    public List<RequiredCondition> generate(ScoringInput in) {
        List<RequiredCondition> conditions = new ArrayList<>();

        // Escort for wide loads, two vehicles over 5 m
        if (in.getWidthM().compareTo(new BigDecimal("5.00")) > 0) {
            conditions.add(new RequiredCondition("ESCORT", "Two escort vehicles", 2, null));
        } else if (in.getWidthM().compareTo(new BigDecimal("4.00")) > 0) {
            conditions.add(new RequiredCondition("ESCORT", "One escort vehicle", 1, null));
        }

        // Night moves need beacons
        boolean night = false;
        for (LocalDateTime t : in.getDepartures()) {
            night = night || TriggerEvaluator.isNight(t);
        }
        for (LocalDateTime t : in.getEstuaryCrossings()) {
            night = night || TriggerEvaluator.isNight(t);
        }
        if (night) {
            conditions.add(new RequiredCondition("NIGHT_BEACONS", "Amber beacons on the vehicle and escorts", null, null));
        }

        // Heavy crossings of the estuary bridges: speed limit and off-peak crossing only
        int tonnes = riskScoringService.tonnes(in.getGrossMassKg());
        for (Structure s : in.getStructures()) {
            if (s.isEstuary() && s.isCarrying() && (double) tonnes / s.getMassRatingT() > 0.70) {
                conditions.add(new RequiredCondition("SPEED_LIMIT", "16 km/h on " + s.getName(), null, s.getCode()));
                conditions.add(new RequiredCondition("TIME_WINDOW",
                        "No crossing of " + s.getName() + " 07:00-09:59 or 16:00-18:59", null, s.getCode()));
            }
        }
        return conditions;
    }
}
