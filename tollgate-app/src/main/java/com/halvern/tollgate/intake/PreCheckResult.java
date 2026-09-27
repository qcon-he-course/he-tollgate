package com.halvern.tollgate.intake;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Hard stops and warnings from the lodgement checks.
 */
@Data
public class PreCheckResult {

    public static final String SHORT_NOTICE = "SHORT_NOTICE";
    public static final String NO_ESCORT_PLAN = "NO_ESCORT_PLAN";

    private List<CheckFailure> failures = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
}
