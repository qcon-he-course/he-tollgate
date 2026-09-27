package com.halvern.tollgate.assessment;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * An incident recorded against a haulier, used in scoring (TG-142).
 *
 * @author Tom Hollis
 */
@Data
@Entity
@Table(name = "incident")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long haulierId;
    private LocalDate incidentDate;
    private String description;
    private String recordedBy;
    private LocalDateTime recordedAt;
}
