package com.halvern.tollgate.determination;

import java.time.LocalDate;
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

import lombok.Data;

/**
 * One version of a permit. The E1 permit table holds one row per version since TG-181.
 *
 * @author Priya Venkat
 */
@Data
@Entity
@Table(name = "permit")
public class PermitVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public static final String SUPERSEDED = "SUPERSEDED";

    private String reference;
    private Long applicationId;
    private Long caseId;
    private int version;
    private String status;
    private LocalDate windowStart;
    private LocalDate windowEnd;
    private String issuedBy;
    private LocalDateTime issuedAt;
    private String statusReason;
    private LocalDateTime statusChangedAt;
    private Long suspendedByClosureId;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "permit_id", insertable = false, updatable = false)
    @Fetch(FetchMode.SELECT)
    @OrderBy("id")
    private List<PermitCondition> conditions = new ArrayList<>();
}
