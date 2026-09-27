package com.halvern.tollgate.assessment;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    long countByHaulierIdAndIncidentDateBetween(Long haulierId, LocalDate from, LocalDate to);
}
