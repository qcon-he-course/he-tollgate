/**
 * SLA clocks and pauses, and the business-day arithmetic behind them.
 *
 * <p>{@link com.halvern.tollgate.sla.clock.SlaCalculator} is a pure function and holds the rules; everything else
 * here loads and saves. Business days use the holiday table (ADR-0007).
 */
package com.halvern.tollgate.sla.clock;
