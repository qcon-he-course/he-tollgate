package com.halvern.tollgate.determination;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * A condition attached to a permit (TG-161).
 *
 * @author Priya Venkat
 */
@Data
@Entity
@Table(name = "permit_condition")
public class PermitCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public static final String ESCORT = "ESCORT";
    public static final String TIME_WINDOW = "TIME_WINDOW";
    public static final String SPEED_LIMIT = "SPEED_LIMIT";
    public static final String DIVERSION = "DIVERSION";
    public static final String NIGHT_BEACONS = "NIGHT_BEACONS";

    public static final String SOURCE_MANDATORY = "MANDATORY";
    public static final String SOURCE_OFFICER = "OFFICER";
    public static final String SOURCE_ENGINEER = "ENGINEER";
    public static final String SOURCE_PANEL = "PANEL";

    @Column(name = "case_id")
    private Long caseId;
    @Column(name = "permit_id")
    private Long permitId;
    private String conditionType;
    private String detail;
    private Integer escortVehicles;
    private String structureCode;
    private String source;
}
