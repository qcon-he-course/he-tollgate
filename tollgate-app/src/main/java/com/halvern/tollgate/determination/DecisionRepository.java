package com.halvern.tollgate.determination;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisionRepository extends JpaRepository<Decision, Long> {

    List<Decision> findByCaseIdOrderByDecidedAtAsc(Long caseId);
}
