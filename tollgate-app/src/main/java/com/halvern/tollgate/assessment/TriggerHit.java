package com.halvern.tollgate.assessment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * A trigger that fired during an assessment and the minimum track it forces.
 */
@Data
@Entity
@Table(name = "trigger_hit")
public class TriggerHit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assessment_id")
    private Long assessmentId;
    private String triggerCode;
    private String minTrack;
    private String detail;
}
