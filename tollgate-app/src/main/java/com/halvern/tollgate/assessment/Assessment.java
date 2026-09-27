package com.halvern.tollgate.assessment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import lombok.Data;

/**
 * Result of a risk assessment of a case (TG-140).
 *
 * @author Tom Hollis
 */
@Data
@Entity
@Table(name = "assessment")
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id")
    private Long caseId;
    private int score;
    private String scoreTrack;
    private String finalTrack;
    private LocalDateTime assessedAt;
    private boolean superseded;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "assessment_id", nullable = false)
    @Fetch(FetchMode.SELECT)
    @OrderBy("id")
    private List<AssessmentFactor> factors = new ArrayList<>();

    // Trigger hits are saved by ExpressEligibility (TG-141)
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "assessment_id", insertable = false, updatable = false)
    @Fetch(FetchMode.SELECT)
    @OrderBy("id")
    private List<TriggerHit> triggerHits = new ArrayList<>();
}
