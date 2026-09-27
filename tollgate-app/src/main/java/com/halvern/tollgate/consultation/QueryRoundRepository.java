package com.halvern.tollgate.consultation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QueryRoundRepository extends JpaRepository<QueryRound, Long> {

    List<QueryRound> findByCaseIdOrderByRoundNoAsc(Long caseId);

    List<QueryRound> findByStatus(String status);
}
