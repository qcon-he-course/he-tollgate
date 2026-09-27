package com.halvern.tollgate.assessment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TriggerHitRepository extends JpaRepository<TriggerHit, Long> {

    List<TriggerHit> findByAssessmentId(Long assessmentId);
}
