// Partner API - Integration Squad 2021
package com.halvern.tollgate.portal.v1.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class V1AnswerRequest {

    @ApiModelProperty(value = "Question number as shown in the questionnaire", example = "1", required = true)
    private int seq;

    @ApiModelProperty(value = "Your answer", required = true)
    private String answer;
}
