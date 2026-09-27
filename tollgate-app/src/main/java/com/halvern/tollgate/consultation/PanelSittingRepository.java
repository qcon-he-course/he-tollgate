package com.halvern.tollgate.consultation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PanelSittingRepository extends JpaRepository<PanelSitting, Long> {

    Optional<PanelSitting> findBySittingDate(LocalDate sittingDate);

    List<PanelSitting> findBySittingDateGreaterThanEqualOrderBySittingDateAsc(LocalDate from);
}
