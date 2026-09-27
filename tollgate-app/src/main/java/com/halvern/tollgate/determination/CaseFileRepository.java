package com.halvern.tollgate.determination;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseFileRepository extends JpaRepository<CaseFile, Long> {

    Optional<CaseFile> findByReference(String reference);

    List<CaseFile> findByStateIn(List<String> states);
}
