// Partner API - Integration Squad 2021
package com.halvern.tollgate.portal.v1.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class V1TripRequest {

    @ApiModelProperty(value = "Planned departure, dd/MM/yyyy HH:mm", example = "02/11/2021 09:00", required = true)
    private String departure;

    @ApiModelProperty(value = "Planned estuary crossing, dd/MM/yyyy HH:mm, for routes over Northgate or Southreach", example = "02/11/2021 10:15")
    private String crossing;
}
