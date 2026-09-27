// Partner API - Integration Squad 2021
// v2: copied from v1, see PI-44
package com.halvern.tollgate.portal.v2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.halvern.tollgate.determination.CaseFile;
import com.halvern.tollgate.intake.MovementApplication;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

// v2 change: a DTO instead of the entities
@Data
@ApiModel("ApplicationStatusV2")
public class V2ApplicationDto {

    private String reference;
    @ApiModelProperty("DRAFT, LODGED, IN_REVIEW, AWAITING_HAULIER, AWAITING_PANEL, DECIDED, WITHDRAWN")
    private String status;
    @ApiModelProperty("For DECIDED: ISSUED, ISSUED_WITH_CONDITIONS or REFUSED")
    private String outcome;
    private String loadType;
    private String loadDescription;
    private Integer grossMassKg;
    private BigDecimal widthM;
    private BigDecimal lengthM;
    private BigDecimal heightM;
    private String route;
    private LocalDate firstMoveDate;
    private LocalDate lastMoveDate;
    private LocalDateTime lodgedAt;
    private String track;
    private Integer riskScore;
    private String assignedOfficer;
    private String permitReference;
    private List<String> warnings = new ArrayList<>();
    private List<V2QuestionDto> questions = new ArrayList<>();
    private List<V2QueryRoundDto> queryRounds = new ArrayList<>();

    public static V2ApplicationDto toDto(MovementApplication app, CaseFile caseFile, String status) {
        V2ApplicationDto dto = new V2ApplicationDto();
        dto.setReference(app.getReference());
        dto.setStatus(status);
        dto.setLoadType(app.getLoadTypeCode());
        dto.setLoadDescription(app.getLoadDescription());
        dto.setGrossMassKg(app.getGrossMassKg());
        dto.setWidthM(app.getWidthM());
        dto.setLengthM(app.getLengthM());
        dto.setHeightM(app.getHeightM());
        dto.setRoute(app.getRouteCode());
        dto.setFirstMoveDate(app.getFirstMoveDate() == null ? null : ((java.sql.Date) app.getFirstMoveDate()).toLocalDate());
        dto.setLastMoveDate(app.getLastMoveDate() == null ? null : ((java.sql.Date) app.getLastMoveDate()).toLocalDate());
        dto.setLodgedAt(app.getLodgedAt() == null ? null : app.getLodgedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        if (caseFile != null) {
            dto.setOutcome(caseFile.getOutcome());
            dto.setTrack(caseFile.getTrack());
            dto.setRiskScore(caseFile.getRiskScore());
            dto.setAssignedOfficer(caseFile.getAssignedOfficer());
            if (caseFile.getWarnings() != null) {
                for (String w : caseFile.getWarnings().split(",")) {
                    dto.getWarnings().add(w);
                }
            }
        }
        return dto;
    }
}
