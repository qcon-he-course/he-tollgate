package com.halvern.tollgate.assessment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A condition the permit must carry whatever the decision (TG-161).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequiredCondition {

    private String conditionType;
    private String detail;
    private Integer escortVehicles;
    private String structureCode;
}
