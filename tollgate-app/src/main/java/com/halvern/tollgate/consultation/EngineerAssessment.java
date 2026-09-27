package com.halvern.tollgate.consultation;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * An engineer's assessment of a case (TG-150).
 *
 * @author Dana Kerr
 */
@Data
@Entity
@Table(name = "engineer_assessment")
public class EngineerAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public static final String SATISFACTORY = "SATISFACTORY";
    public static final String SATISFACTORY_WITH_CONDITIONS = "SATISFACTORY_WITH_CONDITIONS";
    public static final String REFER_TO_PANEL = "REFER_TO_PANEL";
    public static final String UNSATISFACTORY = "UNSATISFACTORY";

    @Column(name = "case_id")
    private Long caseId;
    private String engineer;
    private String recommendation;
    private String notes;
    private String proposedConditions;
    private LocalDateTime recordedAt;
}
