package com.halvern.tollgate.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.halvern.tollgate.reference.Structure;

class TriggerEvaluatorTest {

    private final TriggerEvaluator evaluator = new TriggerEvaluator();

    private ScoringInput input() {
        ScoringInput in = new ScoringInput();
        in.setWidthM(new BigDecimal("2.50"));
        in.setLengthM(new BigDecimal("16.00"));
        in.setHeightM(new BigDecimal("4.00"));
        return in;
    }

    private List<String> codes(List<TriggerHit> hits) {
        return hits.stream().map(TriggerHit::getTriggerCode).collect(Collectors.toList());
    }

    @Test
    void hazardousGoesToEngineer() {
        ScoringInput in = input();
        in.setHazardous(true);
        List<TriggerHit> hits = evaluator.evaluate(in, 20);
        assertThat(codes(hits)).containsExactly("HAZARDOUS");
        assertThat(hits.get(0).getMinTrack()).isEqualTo(Tracks.ENGINEER);
    }

    @Test
    void nightCrossing() {
        ScoringInput in = input();
        in.getEstuaryCrossings().add(LocalDateTime.of(2019, 6, 3, 23, 10));
        assertThat(codes(evaluator.evaluate(in, 20))).containsExactly("NIGHT_ESTUARY");
    }

    @Test
    void dayCrossingIsFine() {
        ScoringInput in = input();
        in.getEstuaryCrossings().add(LocalDateTime.of(2019, 6, 3, 10, 0));
        assertThat(evaluator.evaluate(in, 20)).isEmpty();
    }

    @Test
    void overRating() {
        ScoringInput in = input();
        Structure s = new Structure();
        s.setCode("NEB");
        s.setStructureType(Structure.TYPE_CARRYING);
        s.setMassRatingT(150);
        in.getStructures().add(s);
        assertThat(codes(evaluator.evaluate(in, 160))).contains("OVER_RATING", "HIGH_UTILISATION");
    }

    @Test
    void wideLoad() {
        ScoringInput in = input();
        in.setWidthM(new BigDecimal("5.50"));
        assertThat(codes(evaluator.evaluate(in, 20))).containsExactly("WIDE_LOAD");
    }
}
