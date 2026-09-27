package com.halvern.tollgate.permitdoc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Reads the permit status history written by {@code trg_permit_status_history}. Nothing in the application writes
 * to it.
 */
@Repository
public class PermitStatusHistoryDao {

    private static final String FIND_FOR_APPLICATION = """
            SELECT h.permit_reference, h.version, h.old_status, h.new_status, h.changed_by, h.changed_at
              FROM permit_status_history h
              JOIN permit p ON p.id = h.permit_id
             WHERE p.application_id = ?
             ORDER BY h.changed_at, h.id
            """;

    private final JdbcTemplate jdbc;

    public PermitStatusHistoryDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<PermitStatusChange> findForApplication(long applicationId) {
        return jdbc.query(FIND_FOR_APPLICATION, (rs, i) -> new PermitStatusChange(
                PermitVersionRef.of(rs.getString("permit_reference"), rs.getInt("version")),
                rs.getString("old_status"),
                rs.getString("new_status"),
                rs.getString("changed_by"),
                rs.getTimestamp("changed_at").toInstant()), applicationId);
    }
}
