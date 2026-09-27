/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: AuditDao.java 1151 2017-11-27 11:30:52Z rmistry $
 */
package com.halvern.tollgate.audit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.halvern.tollgate.platform.DbTime;

/**
 * Data access for the audit log. Entries are only ever added (BR-46).
 * Implements BR-45 and BR-46.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Repository
public class AuditDao {

    private static final String INSERT =
            "INSERT INTO audit_entry (reference, entity_type, old_state, new_state, actor, created_at, reason) "
            + "VALUES (?, ?, ?, ?, ?, " + DbTime.NOW_UTC + ", ?)";

    // BR-46: an application's history includes the history of its permit
    private static final String FIND_FOR_APPLICATION =
            "SELECT id, reference, entity_type, old_state, new_state, actor, created_at, reason "
            + "FROM audit_entry "
            + "WHERE reference = ? "
            + "   OR reference IN (SELECT p.reference FROM permit p "
            + "                    JOIN movement_application a ON a.id = p.application_id "
            + "                    WHERE a.reference = ?) "
            + "ORDER BY created_at, id";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<AuditEntry> MAPPER = new RowMapper<AuditEntry>() {
        public AuditEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            AuditEntry e = new AuditEntry();
            e.setId(rs.getLong("id"));
            e.setReference(rs.getString("reference"));
            e.setEntityType(rs.getString("entity_type"));
            e.setOldState(rs.getString("old_state"));
            e.setNewState(rs.getString("new_state"));
            e.setActor(rs.getString("actor"));
            e.setCreatedAt(rs.getTimestamp("created_at", DbTime.utc()));
            e.setReason(rs.getString("reason"));
            return e;
        }
    };

    /**
     * Insert.
     *
     * @param entry the entry
     */
    public void insert(AuditEntry entry) {
        jdbcTemplate.update(INSERT, entry.getReference(), entry.getEntityType(), entry.getOldState(),
                entry.getNewState(), entry.getActor(), entry.getReason());
    }

    /**
     * Find for application.
     *
     * @param applicationReference the application reference
     * @return the application's and its permit's history, oldest first
     */
    public List<AuditEntry> findForApplication(String applicationReference) {
        return jdbcTemplate.query(FIND_FOR_APPLICATION, MAPPER, applicationReference, applicationReference);
    }
}
