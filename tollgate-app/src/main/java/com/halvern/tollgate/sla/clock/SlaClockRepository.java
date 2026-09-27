package com.halvern.tollgate.sla.clock;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaClockRepository extends JpaRepository<SlaClock, Long> {

    Optional<SlaClock> findByCaseId(Long caseId);

    List<SlaClock> findByStoppedOnIsNull();
}
