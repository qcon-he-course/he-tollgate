/**
 * The officers' dashboard: open cases with their SLA status, read in one query.
 *
 * <p>The worklist before this read each case through JPA, which loads every collection on the case. The dashboard
 * reads only the columns it shows.
 */
package com.halvern.tollgate.sla.dashboard;
