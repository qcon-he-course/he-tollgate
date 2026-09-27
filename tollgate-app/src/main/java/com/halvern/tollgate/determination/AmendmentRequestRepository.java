package com.halvern.tollgate.determination;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AmendmentRequestRepository extends JpaRepository<AmendmentRequest, Long> {

    List<AmendmentRequest> findByCaseIdOrderByRequestedAtDesc(Long caseId);

    List<AmendmentRequest> findByStatusOrderByRequestedAtAsc(String status);
}
