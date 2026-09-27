package com.halvern.tollgate.assessment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.halvern.tollgate.reference.Structure;

/**
 * Triggers force a minimum track whatever the score (TG-141).
 *
 * @author Tom Hollis
 */
@Component
public class TriggerEvaluator {

    public List<TriggerHit> evaluate(ScoringInput in, int tonnes) {
        List<TriggerHit> hits = new ArrayList<>();
        for (Structure s : in.getStructures()) {
            if (!s.isCarrying()) {
                continue;
            }
            if (tonnes > s.getMassRatingT()) {
                hits.add(hit("OVER_RATING", Tracks.PANEL, tonnes + " t over " + s.getCode() + " rating " + s.getMassRatingT() + " t"));
            }
            if ((double) tonnes / s.getMassRatingT() > 0.85) {
                hits.add(hit("HIGH_UTILISATION", Tracks.ENGINEER, s.getCode()));
            }
        }
        if (in.isHazardous()) {
            hits.add(hit("HAZARDOUS", Tracks.ENGINEER, null));
        }
        for (LocalDateTime crossing : in.getEstuaryCrossings()) {
            if (isNight(crossing)) {
                hits.add(hit("NIGHT_ESTUARY", Tracks.ENGINEER, crossing.toString()));
                break;
            }
        }
        if (in.getWidthM().compareTo(new BigDecimal("5.00")) > 0) {
            hits.add(hit("WIDE_LOAD", Tracks.DESK, in.getWidthM() + " m"));
        }
        return hits;
    }

    /** 22:00 to 05:59. */
    public static boolean isNight(LocalDateTime time) {
        int hour = time.getHour();
        return hour >= 22 || hour < 6;
    }

    private TriggerHit hit(String code, String track, String detail) {
        TriggerHit h = new TriggerHit();
        h.setTriggerCode(code);
        h.setMinTrack(track);
        h.setDetail(detail);
        return h;
    }
}
