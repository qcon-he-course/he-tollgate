// Partner API - Integration Squad 2021
// v2: copied from v1, see PI-44
package com.halvern.tollgate.portal.v2.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.halvern.tollgate.determination.PermitCondition;
import com.halvern.tollgate.determination.PermitVersion;

import lombok.Data;

@Data
public class V2PermitDto {

    private String reference;
    private int version;
    private String status;
    private LocalDate firstMoveDate;
    private LocalDate lastMoveDate;
    private List<String> conditions = new ArrayList<>();

    public static V2PermitDto toDto(PermitVersion p, List<PermitCondition> conditions) {
        V2PermitDto dto = new V2PermitDto();
        dto.setReference(p.getReference());
        dto.setVersion(p.getVersion());
        dto.setStatus(p.getStatus());
        dto.setFirstMoveDate(p.getWindowStart());
        dto.setLastMoveDate(p.getWindowEnd());
        for (PermitCondition c : conditions) {
            dto.getConditions().add(c.getConditionType() + ": " + (c.getDetail() == null ? "" : c.getDetail()));
        }
        return dto;
    }
}
