package com.halvern.tollgate.sla.clock;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaPauseRepository extends JpaRepository<SlaPause, Long> {

    List<SlaPause> findByCaseIdOrderByStartedOnAsc(Long caseId);
}
