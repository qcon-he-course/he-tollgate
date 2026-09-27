/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: NotificationLogDao.java 1154 2017-11-28 15:02:17Z rmistry $
 */
package com.halvern.tollgate.notify;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.platform.DbTime;

/**
 * Data access for the notification log.
 * Implements BR-42.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Repository
public class NotificationLogDao {

    private static final String INSERT =
            "INSERT INTO notification_log (reference, recipient, template, subject, status, error_message, created_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, " + DbTime.NOW_UTC + ")";

    private static final String FIND_FOR_APPLICATION =
            "SELECT id, reference, recipient, template, subject, status, error_message, created_at "
            + "FROM notification_log "
            + "WHERE reference = ? "
            + "   OR reference IN (SELECT p.reference FROM permit p "
            + "                    JOIN movement_application a ON a.id = p.application_id "
            + "                    WHERE a.reference = ?) "
            + "ORDER BY created_at, id";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<NotificationLogEntry> MAPPER = new RowMapper<NotificationLogEntry>() {
        public NotificationLogEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            NotificationLogEntry e = new NotificationLogEntry();
            e.setId(rs.getLong("id"));
            e.setReference(rs.getString("reference"));
            e.setRecipient(rs.getString("recipient"));
            e.setTemplate(rs.getString("template"));
            e.setSubject(rs.getString("subject"));
            e.setStatus(rs.getString("status"));
            e.setErrorMessage(rs.getString("error_message"));
            e.setCreatedAt(rs.getTimestamp("created_at", DbTime.utc()));
            return e;
        }
    };

    /**
     * Insert. Sent emails are logged with the caller's transaction.
     *
     * @param entry the entry
     */
    public void insert(NotificationLogEntry entry) {
        jdbcTemplate.update(INSERT, entry.getReference(), entry.getRecipient(), entry.getTemplate(),
                entry.getSubject(), entry.getStatus(), entry.getErrorMessage());
    }

    /**
     * Insert a failure. Uses its own transaction so the failure is kept when the caller's
     * transaction is rolled back (BR-42).
     *
     * @param entry the entry
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertFailure(NotificationLogEntry entry) {
        insert(entry);
    }

    /**
     * Find for application.
     *
     * @param applicationReference the application reference
     * @return emails for the application and its permit, oldest first
     */
    public List<NotificationLogEntry> findForApplication(String applicationReference) {
        return jdbcTemplate.query(FIND_FOR_APPLICATION, MAPPER, applicationReference, applicationReference);
    }
}
