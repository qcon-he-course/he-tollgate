// Partner API - Integration Squad 2021
// v2: copied from v1, see PI-44
package com.halvern.tollgate.portal.v2.mapper;

import com.halvern.tollgate.determination.CaseFile;

/**
 * Case status shown to partners (PI-46).
 *
 * @author Aisha Rahman
 */
public final class CaseStatusMapper {

    private CaseStatusMapper() {
    }

    public static String toStatus(CaseFile caseFile) {
        if (caseFile == null) {
            return "DRAFT";
        }
        switch (caseFile.getState()) {
            case "LODGED":
                return "LODGED";
            case "IN_REVIEW":
                return "IN_REVIEW";
            case "AWAITING_HAULIER":
                return "AWAITING_HAULIER";
            case "AWAITING_PANEL":
                return "AWAITING_PANEL";
            case "DECIDED":
                return "DECIDED";
            case "WITHDRAWN":
                return "WITHDRAWN";
            default:
                return "IN_PROGRESS";
        }
    }
}
