package com.halvern.tollgate.determination;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

import com.halvern.tollgate.assessment.Assessment;
import com.halvern.tollgate.consultation.EngineerAssessment;
import com.halvern.tollgate.consultation.QueryRound;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * Staff-side record of a lodged application (TG-101).
 *
 * @author Priya Venkat
 */
@Data
@Entity
@Table(name = "case_file")
public class CaseFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reference;
    private Long applicationId;
    private Long haulierId;
    // PI-18 partners see the application status, not the internal case state
    @JsonIgnore
    private String state;
    private String outcome;
    private String track;
    private String escalatedTrack;
    private Integer riskScore;
    private String warnings;
    private String assignedOfficer;
    private String assignedEngineer;
    private String officerComments;
    private LocalDateTime lodgedAt;
    private LocalDateTime decidedAt;
    private String closedReason;
    private LocalDateTime updatedAt;

    // EAGER: the case page and the decision screens need all of these (TG-128 LazyInitializationException)
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_id", insertable = false, updatable = false)
    @Fetch(FetchMode.SELECT)
    @OrderBy("id")
    private List<Assessment> assessments = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_id", insertable = false, updatable = false)
    @Fetch(FetchMode.SELECT)
    @OrderBy("roundNo")
    private List<QueryRound> queryRounds = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_id", insertable = false, updatable = false)
    @Fetch(FetchMode.SELECT)
    @OrderBy("id")
    private List<EngineerAssessment> engineerAssessments = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_id", insertable = false, updatable = false)
    @Fetch(FetchMode.SELECT)
    @OrderBy("id")
    private List<PermitCondition> conditions = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_id", insertable = false, updatable = false)
    @Fetch(FetchMode.SELECT)
    @OrderBy("id")
    private List<Decision> decisions = new ArrayList<>();
}
