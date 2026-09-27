// Partner API - Integration Squad 2021
// v2: copied from v1, see PI-44
package com.halvern.tollgate.portal.v2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("ApplicationV2")
public class V2ApplicationRequest {

    @ApiModelProperty(value = "Registration of one of your vehicles", example = "VS15 NDX", required = true)
    private String vehicleRegistration;

    @ApiModelProperty(value = "Load type code", example = "CONSTRUCTION_PLANT", required = true)
    private String loadType;

    @ApiModelProperty(value = "Description of the load", example = "Tracked excavator on a low loader")
    private String loadDescription;

    @ApiModelProperty(value = "Gross mass in kg", example = "30000", required = true)
    private Integer grossMassKg;

    @ApiModelProperty(value = "Width in metres", example = "2.50", required = true)
    private BigDecimal widthM;

    @ApiModelProperty(value = "Length in metres", example = "16.00", required = true)
    private BigDecimal lengthM;

    @ApiModelProperty(value = "Height in metres", example = "4.00", required = true)
    private BigDecimal heightM;

    @ApiModelProperty(value = "Hazardous load", example = "false")
    private boolean hazardous;

    @ApiModelProperty(value = "UN number for hazardous loads", example = "1203")
    private String unNumber;

    @ApiModelProperty(value = "Route code", example = "R4", required = true)
    private String route;

    // v2 change: ISO dates
    @ApiModelProperty(value = "First move date", example = "2021-11-02", required = true)
    private LocalDate firstMoveDate;

    @ApiModelProperty(value = "Last move date", example = "2021-11-04", required = true)
    private LocalDate lastMoveDate;

    @ApiModelProperty(value = "Trips, 1 to 10")
    private List<V2TripRequest> trips = new ArrayList<>();
}
