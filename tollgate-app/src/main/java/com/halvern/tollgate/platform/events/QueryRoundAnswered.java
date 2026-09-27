package com.halvern.tollgate.platform.events;

import java.time.Instant;
import java.time.LocalDate;

public record QueryRoundAnswered(String reference, Long caseId, int roundNo, LocalDate answeredOn, Instant occurredAt) {
}
