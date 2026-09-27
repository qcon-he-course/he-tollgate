package com.halvern.tollgate.consultation;

import java.time.LocalDate;
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
 * A round of questions sent to the haulier (TG-152).
 *
 * @author Dana Kerr
 */
@Data
@Entity
@Table(name = "query_round")
public class QueryRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public static final String OPEN = "OPEN";
    public static final String ANSWERED = "ANSWERED";
    public static final String LAPSED = "LAPSED";
    public static final String CLOSED = "CLOSED";

    @Column(name = "case_id")
    private Long caseId;
    private int roundNo;
    private String issuedBy;
    private LocalDate issuedOn;
    private LocalDate deadline;
    private String status;
    private LocalDateTime answeredAt;
    private boolean reminderSent;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "query_round_id", nullable = false)
    @Fetch(FetchMode.SELECT)
    @OrderBy("seq")
    private List<QueryQuestion> questions = new ArrayList<>();
}
