package com.halvern.tollgate.sla.dashboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.determination.CaseService;

/**
 * Open cases with their SLA status, most urgent first: red, then amber, then green, and within each by the fewest
 * business days remaining.
 */
@Component
public class SlaDashboardQuery {

    private static final String SQL = """
            SELECT c.reference, h.name AS haulier_name, c.track, c.state, c.assigned_officer,
                   s.status AS sla_status, s.target_days - s.elapsed_days AS days_remaining,
                   CAST(c.lodged_at AS DATE) AS lodged_on
              FROM case_file c
              JOIN haulier h ON h.id = c.haulier_id
              LEFT JOIN sla_clock s ON s.case_id = c.id
             WHERE c.state IN (:states)
               AND (:track = '' OR c.track = :track)
               AND (:status = '' OR s.status = :status)
               AND (:officer = '' OR c.assigned_officer = :officer)
             ORDER BY CASE s.status WHEN 'RED' THEN 0 WHEN 'AMBER' THEN 1 WHEN 'GREEN' THEN 2 ELSE 3 END,
                      days_remaining NULLS LAST, c.lodged_at, c.reference
            """;

    private final NamedParameterJdbcTemplate jdbc;
    private final CaseService caseService;

    public SlaDashboardQuery(NamedParameterJdbcTemplate jdbc, CaseService caseService) {
        this.jdbc = jdbc;
        this.caseService = caseService;
    }

    public List<DashboardRow> openCases(DashboardFilter filter) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("states", caseService.openStates())
                .addValue("track", valueOf(filter.track()))
                .addValue("status", valueOf(filter.status()))
                .addValue("officer", valueOf(filter.officer()));
        return jdbc.query(SQL, params, SlaDashboardQuery::row);
    }

    private static String valueOf(String filterValue) {
        return DashboardFilter.isSet(filterValue) ? filterValue.trim() : "";
    }

    private static DashboardRow row(ResultSet rs, int rowNum) throws SQLException {
        int days = rs.getInt("days_remaining");
        Integer daysRemaining = rs.wasNull() ? null : days;
        java.sql.Date lodged = rs.getDate("lodged_on");
        return new DashboardRow(rs.getString("reference"), rs.getString("haulier_name"), rs.getString("track"),
                rs.getString("state"), rs.getString("assigned_officer"), rs.getString("sla_status"), daysRemaining,
                lodged == null ? null : lodged.toLocalDate());
    }
}
