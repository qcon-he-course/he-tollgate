package com.halvern.tollgate.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.halvern.tollgate.reference.Structure;

class RiskScoringServiceTest {

    private final RiskScoringService service = new RiskScoringService();

    private ScoringInput load(int kg, String width, String length, String height) {
        ScoringInput in = new ScoringInput();
        in.setGrossMassKg(kg);
        in.setWidthM(new BigDecimal(width));
        in.setLengthM(new BigDecimal(length));
        in.setHeightM(new BigDecimal(height));
        return in;
    }

    private int points(List<AssessmentFactor> factors, String name) {
        return factors.stream().filter(f -> f.getFactor().equals(name)).findFirst().get().getPoints();
    }

    @Test
    void smallLoadScoresZero() {
        List<AssessmentFactor> factors = service.score(load(30000, "2.50", "16.00", "4.00"));
        assertThat(service.total(factors)).isEqualTo(0);
    }

    @Test
    void deskProfileScoresTwenty() {
        List<AssessmentFactor> factors = service.score(load(60000, "3.50", "22.00", "4.00"));
        assertThat(service.total(factors)).isEqualTo(20);
    }

    @Test
    void massBands() {
        assertThat(points(service.score(load(44000, "2.50", "16.00", "4.00")), "MASS")).isEqualTo(0);
        assertThat(points(service.score(load(44001, "2.50", "16.00", "4.00")), "MASS")).isEqualTo(10);
        assertThat(points(service.score(load(150000, "2.50", "16.00", "4.00")), "MASS")).isEqualTo(20);
        assertThat(points(service.score(load(150001, "2.50", "16.00", "4.00")), "MASS")).isEqualTo(30);
    }

    @Test
    void widthBoundaries() {
        assertThat(points(service.score(load(1000, "2.90", "16.00", "4.00")), "WIDTH")).isEqualTo(0);
        assertThat(points(service.score(load(1000, "4.00", "16.00", "4.00")), "WIDTH")).isEqualTo(5);
        assertThat(points(service.score(load(1000, "5.00", "16.00", "4.00")), "WIDTH")).isEqualTo(10);
        assertThat(points(service.score(load(1000, "5.01", "16.00", "4.00")), "WIDTH")).isEqualTo(15);
    }

    @Test
    void structurePointsAreCapped() {
        ScoringInput in = load(160000, "4.00", "20.00", "4.50");
        for (int rating : new int[] {150, 140, 180, 120}) {
            Structure s = new Structure();
            s.setCode("S" + rating);
            s.setStructureType(Structure.TYPE_CARRYING);
            s.setMassRatingT(rating);
            in.getStructures().add(s);
        }
        assertThat(points(service.score(in), "STRUCTURES")).isEqualTo(30);
    }

    @Test
    void incidents() {
        ScoringInput in = load(1000, "2.50", "16.00", "4.00");
        in.setIncidentCount(1);
        assertThat(points(service.score(in), "INCIDENTS")).isEqualTo(5);
        in.setIncidentCount(3);
        assertThat(points(service.score(in), "INCIDENTS")).isEqualTo(10);
    }
}
