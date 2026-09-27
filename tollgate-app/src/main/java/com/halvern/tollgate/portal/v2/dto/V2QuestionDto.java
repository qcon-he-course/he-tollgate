// Partner API - Integration Squad 2021
// v2: copied from v1, see PI-44
package com.halvern.tollgate.portal.v2.dto;

import com.halvern.tollgate.questionnaire.CaseQuestion;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class V2QuestionDto {

    @ApiModelProperty("Question number")
    private int seq;
    private String question;
    private boolean mandatory;
    private String answer;
    @ApiModelProperty("PENDING, ACCEPTED or RETURNED")
    private String reviewStatus;
    private String reviewNote;

    public static V2QuestionDto toDto(CaseQuestion q) {
        V2QuestionDto dto = new V2QuestionDto();
        dto.setSeq(q.getSeq());
        dto.setQuestion(q.getQuestionText());
        dto.setMandatory(q.isMandatory());
        dto.setAnswer(q.getAnswerText());
        dto.setReviewStatus(q.getReviewStatus());
        dto.setReviewNote(q.getReviewNote());
        return dto;
    }
}
