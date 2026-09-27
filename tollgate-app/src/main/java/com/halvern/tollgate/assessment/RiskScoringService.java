package com.halvern.tollgate.assessment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.halvern.tollgate.reference.Structure;

/**
 * Risk score for an application. Bands agreed with the operations team (TG-212).
 *
 * @author Tom Hollis
 */
@Service
public class RiskScoringService {

    public List<AssessmentFactor> score(ScoringInput in) {
        List<AssessmentFactor> factors = new ArrayList<>();
        int tonnes = tonnes(in.getGrossMassKg());

        // TG-212 mass bands per ops
        int mass = 0;
        if (tonnes > 150) {
            mass = 30;
        } else if (tonnes > 80) {
            mass = 20;
        } else if (tonnes > 44) {
            mass = 10;
        }
        factors.add(factor("MASS", mass, tonnes + " t"));

        // TG-212 width bands
        int width = 0;
        if (in.getWidthM().compareTo(new BigDecimal("5.00")) > 0) {
            width = 15;
        } else if (in.getWidthM().compareTo(new BigDecimal("4.00")) > 0) {
            width = 10;
        } else if (in.getWidthM().compareTo(new BigDecimal("2.90")) > 0) {
            width = 5;
        }
        factors.add(factor("WIDTH", width, in.getWidthM() + " m"));

        int length = 0;
        if (in.getLengthM().compareTo(new BigDecimal("30.00")) > 0) {
            length = 10;
        } else if (in.getLengthM().compareTo(new BigDecimal("20.00")) > 0) {
            length = 5;
        }
        factors.add(factor("LENGTH", length, in.getLengthM() + " m"));

        int height = in.getHeightM().compareTo(new BigDecimal("4.95")) > 0 ? 5 : 0;
        factors.add(factor("HEIGHT", height, in.getHeightM() + " m"));

        // TG-215 structure utilisation, capped at 30
        int structures = 0;
        StringBuilder detail = new StringBuilder();
        for (Structure s : in.getStructures()) {
            if (!s.isCarrying()) {
                continue;
            }
            double utilisation = (double) tonnes / s.getMassRatingT();
            if (utilisation > 0.85) {
                structures += 10;
            } else if (utilisation > 0.70) {
                structures += 5;
            }
            detail.append(s.getCode()).append(String.format(" %.2f ", utilisation));
        }
        factors.add(factor("STRUCTURES", Math.min(structures, 30), detail.toString().trim()));

        // TG-142 incidents in the last 24 months
        int incidents = 0;
        if (in.getIncidentCount() >= 2) {
            incidents = 10;
        } else if (in.getIncidentCount() == 1) {
            incidents = 5;
        }
        factors.add(factor("INCIDENTS", incidents, String.valueOf(in.getIncidentCount())));
        return factors;
    }

    public int total(List<AssessmentFactor> factors) {
        int total = 0;
        for (AssessmentFactor f : factors) {
            total += f.getPoints();
        }
        return Math.min(total, 100);
    }

    public int tonnes(int grossMassKg) {
        return (int) Math.ceil(grossMassKg / 1000.0);
    }

    private AssessmentFactor factor(String name, int points, String detail) {
        AssessmentFactor f = new AssessmentFactor();
        f.setFactor(name);
        f.setPoints(points);
        f.setDetail(detail);
        return f;
    }
}
