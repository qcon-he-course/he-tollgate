// Partner API - Integration Squad 2021
// v2: copied from v1, see PI-44
package com.halvern.tollgate.portal.v2.dto;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class V2TripRequest {

    // v2 change: ISO date-times
    @ApiModelProperty(value = "Planned departure", example = "2021-11-02T09:00:00", required = true)
    private LocalDateTime departure;

    @ApiModelProperty(value = "Planned estuary crossing, for routes over Northgate or Southreach", example = "2021-11-02T10:15:00")
    private LocalDateTime crossing;
}
