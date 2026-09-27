package com.halvern.tollgate.determination;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermitVersionRepository extends JpaRepository<PermitVersion, Long> {

    List<PermitVersion> findByCaseIdOrderByVersionAsc(Long caseId);

    Optional<PermitVersion> findFirstByReferenceOrderByVersionDesc(String reference);
}
