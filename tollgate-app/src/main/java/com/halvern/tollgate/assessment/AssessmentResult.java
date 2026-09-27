package com.halvern.tollgate.assessment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssessmentResult {

    private Assessment assessment;
    private boolean expressEligible;
}
