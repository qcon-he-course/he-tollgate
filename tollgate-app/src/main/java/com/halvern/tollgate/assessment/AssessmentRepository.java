package com.halvern.tollgate.assessment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByCaseIdAndSupersededFalse(Long caseId);
}
