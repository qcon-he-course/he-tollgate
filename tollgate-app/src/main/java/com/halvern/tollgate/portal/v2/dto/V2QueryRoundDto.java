// Partner API - Integration Squad 2021
// v2: copied from v1, see PI-44
package com.halvern.tollgate.portal.v2.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.halvern.tollgate.consultation.QueryQuestion;
import com.halvern.tollgate.consultation.QueryRound;

import lombok.Data;

@Data
public class V2QueryRoundDto {

    private int roundNo;
    private String status;
    private LocalDate deadline;
    private List<String> questions = new ArrayList<>();
    private List<String> answers = new ArrayList<>();

    public static V2QueryRoundDto toDto(QueryRound r) {
        V2QueryRoundDto dto = new V2QueryRoundDto();
        dto.setRoundNo(r.getRoundNo());
        dto.setStatus(r.getStatus());
        dto.setDeadline(r.getDeadline());
        for (QueryQuestion q : r.getQuestions()) {
            dto.getQuestions().add(q.getQuestionText());
            dto.getAnswers().add(q.getAnswerText());
        }
        return dto;
    }
}
