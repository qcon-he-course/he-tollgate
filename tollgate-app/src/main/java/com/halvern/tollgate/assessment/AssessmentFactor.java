package com.halvern.tollgate.assessment;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * Points scored for one factor of an assessment.
 */
@Data
@Entity
@Table(name = "assessment_factor")
public class AssessmentFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String factor;
    private int points;
    private String detail;
}
