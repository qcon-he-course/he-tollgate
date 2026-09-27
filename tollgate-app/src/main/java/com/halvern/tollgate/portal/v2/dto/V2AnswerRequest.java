// Partner API - Integration Squad 2021
// v2: copied from v1, see PI-44
package com.halvern.tollgate.portal.v2.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class V2AnswerRequest {

    @ApiModelProperty(value = "Question number", example = "1", required = true)
    private int seq;

    @ApiModelProperty(value = "Your answer", required = true)
    private String answer;
}
