package com.halvern.tollgate.determination;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * A haulier's request for new movement dates on an issued permit (TG-180).
 *
 * @author Priya Venkat
 */
@Data
@Entity
@Table(name = "amendment_request")
public class AmendmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public static final String REQUESTED = "REQUESTED";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    private Long caseId;
    private Long permitId;
    private LocalDate newFirstMoveDate;
    private LocalDate newLastMoveDate;
    private String status;
    private String warnings;
    private String requestedBy;
    private LocalDateTime requestedAt;
    private String decidedBy;
    private LocalDateTime decidedAt;
    private String reason;
}
