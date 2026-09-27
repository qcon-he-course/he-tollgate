package com.halvern.tollgate.determination;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermitConditionRepository extends JpaRepository<PermitCondition, Long> {

    List<PermitCondition> findByPermitId(Long permitId);
}
