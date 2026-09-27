package com.halvern.tollgate.consultation;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * A case listed for a panel sitting.
 *
 * @author Dana Kerr
 */
@Data
@Entity
@Table(name = "panel_listing")
public class PanelListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public static final String DECIDED = "DECIDED";
    public static final String DEFERRED = "DEFERRED";

    private Long panelSittingId;
    private Long caseId;
    private LocalDateTime listedAt;
    private String result;
}
