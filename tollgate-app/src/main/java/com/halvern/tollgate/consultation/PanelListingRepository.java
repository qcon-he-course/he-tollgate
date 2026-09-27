package com.halvern.tollgate.consultation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PanelListingRepository extends JpaRepository<PanelListing, Long> {

    List<PanelListing> findByPanelSittingId(Long panelSittingId);

    List<PanelListing> findByCaseId(Long caseId);
}
