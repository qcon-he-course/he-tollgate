// Partner API - Integration Squad 2021
// v2: copied from v1, see PI-44
package com.halvern.tollgate.portal.v2.dto;

import java.time.LocalDate;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class V2AmendmentRequest {

    @ApiModelProperty(value = "New first move date", example = "2021-11-09", required = true)
    private LocalDate firstMoveDate;

    @ApiModelProperty(value = "New last move date", example = "2021-11-11", required = true)
    private LocalDate lastMoveDate;
}
