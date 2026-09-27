package com.halvern.tollgate.assessment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.halvern.tollgate.reference.Structure;

import lombok.Data;

/**
 * Everything the scoring, triggers and mandatory conditions need about one application.
 */
@Data
public class ScoringInput {

    private int grossMassKg;
    private BigDecimal widthM;
    private BigDecimal lengthM;
    private BigDecimal heightM;
    private boolean hazardous;
    private int incidentCount;
    private List<Structure> structures = new ArrayList<>();
    private List<LocalDateTime> departures = new ArrayList<>();
    private List<LocalDateTime> estuaryCrossings = new ArrayList<>();
}
