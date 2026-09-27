/**
 * Service level agreement for deciding cases.
 *
 * <p>Each case has an SLA clock that counts business days from lodgement to decision against a target for its
 * track. The clock pauses while the case is waiting for the haulier and stops when the case reaches a final state.
 * Clocks are kept up to date from the case events in {@code platform.events} and by a nightly recalculation, so
 * the dashboard can read status straight from the table.
 *
 * <ul>
 *   <li>{@code sla.clock}: the clock and pause records and the business-day arithmetic</li>
 *   <li>{@code sla.status}: RAG status and the result of a calculation</li>
 *   <li>{@code sla.dashboard}: the officers' dashboard query</li>
 * </ul>
 *
 * <p>See ADR-0009.
 */
package com.halvern.tollgate.sla;
