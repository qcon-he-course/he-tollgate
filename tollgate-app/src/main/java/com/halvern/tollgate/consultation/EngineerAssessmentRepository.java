package com.halvern.tollgate.consultation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EngineerAssessmentRepository extends JpaRepository<EngineerAssessment, Long> {

    List<EngineerAssessment> findByCaseIdOrderByRecordedAtAsc(Long caseId);
}
