package com.halvern.tollgate.determination;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * A decision on a case (TG-160).
 *
 * @author Priya Venkat
 */
@Data
@Entity
@Table(name = "decision")
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id")
    private Long caseId;
    private String outcome;
    private String reason;
    private String decidedBy;
    private LocalDateTime decidedAt;
    private boolean panel;
}
