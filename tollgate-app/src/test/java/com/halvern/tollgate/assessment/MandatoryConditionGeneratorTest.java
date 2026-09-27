package com.halvern.tollgate.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.halvern.tollgate.reference.Structure;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MandatoryConditionGeneratorTest {

    @Mock
    private RiskScoringService riskScoringService;

    @InjectMocks
    private MandatoryConditionGenerator generator;

    private ScoringInput input(String width) {
        ScoringInput in = new ScoringInput();
        in.setGrossMassKg(30000);
        in.setWidthM(new BigDecimal(width));
        in.setLengthM(new BigDecimal("16.00"));
        in.setHeightM(new BigDecimal("4.00"));
        when(riskScoringService.tonnes(anyInt())).thenReturn(30);
        return in;
    }

    @Test
    void escortOverFourMetres() {
        List<RequiredCondition> conditions = generator.generate(input("4.50"));
        assertThat(conditions).extracting(RequiredCondition::getConditionType).containsExactly("ESCORT");
        assertThat(conditions.get(0).getEscortVehicles()).isEqualTo(1);
    }

    @Test
    void twoEscortsOverFiveMetres() {
        assertThat(generator.generate(input("5.20")).get(0).getEscortVehicles()).isEqualTo(2);
    }

    @Test
    void noEscortAtExactlyFourMetres() {
        assertThat(generator.generate(input("4.00"))).isEmpty();
    }

    @Test
    void nightDepartureNeedsBeacons() {
        ScoringInput in = input("2.50");
        in.getDepartures().add(LocalDateTime.of(2019, 3, 4, 5, 30));
        assertThat(generator.generate(in)).extracting(RequiredCondition::getConditionType).containsExactly("NIGHT_BEACONS");
    }

    @Test
    void heavyEstuaryCrossing() {
        ScoringInput in = input("2.50");
        when(riskScoringService.tonnes(anyInt())).thenReturn(120);
        Structure neb = new Structure();
        neb.setCode("NEB");
        neb.setName("Northgate Estuary Bridge");
        neb.setStructureType(Structure.TYPE_CARRYING);
        neb.setMassRatingT(150);
        neb.setEstuary(true);
        in.getStructures().add(neb);
        assertThat(generator.generate(in)).extracting(RequiredCondition::getConditionType)
                .containsExactly("SPEED_LIMIT", "TIME_WINDOW");
    }
}
