package com.halvern.tollgate.consultation;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * A sitting of the structures panel (TG-170).
 *
 * @author Dana Kerr
 */
@Data
@Entity
@Table(name = "panel_sitting")
public class PanelSitting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public static final String SCHEDULED = "SCHEDULED";
    public static final String HELD = "HELD";

    private LocalDate sittingDate;
    private String status;
}
