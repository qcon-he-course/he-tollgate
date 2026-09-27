/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: PermitDao.java 1187 2018-03-14 16:02:11Z gferrand $
 */
package com.halvern.tollgate.permitdoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.halvern.tollgate.platform.DbTime;

/**
 * Data access for permits and their printed documents.
 * Implements BR-30 to BR-35.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Repository
public class PermitDao {

    private static final Logger log = LoggerFactory.getLogger(PermitDao.class);

    private static final String SELECT_PERMIT =
            "SELECT p.id, p.reference, p.application_id, a.reference AS application_reference, "
            + "       a.haulier_id, h.name AS haulier_name, r.code AS route_code, "
            + "       a.first_move_date, a.last_move_date, p.status, p.issued_by, p.issued_at, "
            + "       p.status_reason, p.status_changed_at, p.suspended_by_closure_id, "
            + "       p.version, p.window_start, p.window_end "
            + "FROM permit p "
            + "JOIN movement_application a ON a.id = p.application_id "
            + "JOIN haulier h ON h.id = a.haulier_id "
            + "JOIN route r ON r.id = a.route_id ";

    private static final String FIND_BY_ID = SELECT_PERMIT + "WHERE p.id = ?";

    private static final String FIND_BY_REFERENCE = SELECT_PERMIT + "WHERE p.reference = ? ORDER BY p.version DESC";

    private static final String FIND_BY_APPLICATION = SELECT_PERMIT + "WHERE a.reference = ? ORDER BY p.version DESC";

    private static final String FIND_RECENT = SELECT_PERMIT + "ORDER BY p.issued_at DESC LIMIT ?";

    // BR-33: active permits whose route crosses the structure and whose movement dates
    // overlap the closure
    private static final String FIND_ACTIVE_CROSSING =
            SELECT_PERMIT
            + "WHERE p.status = 'ACTIVE' "
            + "AND a.first_move_date <= ? "
            + "AND a.last_move_date >= ? "
            + "AND EXISTS (SELECT 1 FROM route_segment rs "
            + "            WHERE rs.route_id = a.route_id AND rs.structure_id = ?)";

    private static final String FIND_SUSPENDED_BY_CLOSURE =
            SELECT_PERMIT + "WHERE p.status = 'SUSPENDED' AND p.suspended_by_closure_id = ?";

    // BR-35: movement dates have passed
    private static final String FIND_TO_EXPIRE =
            SELECT_PERMIT
            + "WHERE p.status IN ('ACTIVE', 'SUSPENDED') "
            + "AND p.window_end < ?";

    private static final String INSERT =
            "INSERT INTO permit (reference, application_id, status, issued_by, issued_at) "
            + "VALUES (?, ?, 'ACTIVE', ?, " + DbTime.NOW_UTC + ")";

    private static final String UPDATE_STATUS =
            "UPDATE permit "
            + "SET status = ?, status_reason = ?, status_changed_at = " + DbTime.NOW_UTC + ", "
            + "    suspended_by_closure_id = ? "
            + "WHERE id = ?";

    private static final String INSERT_DOCUMENT =
            "INSERT INTO permit_document (permit_id, content, content_type, generated_at) "
            + "VALUES (?, ?, ?, " + DbTime.NOW_UTC + ")";

    private static final String FIND_DOCUMENT =
            "SELECT content FROM permit_document WHERE permit_id = ? ORDER BY generated_at DESC LIMIT 1";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<Permit> MAPPER = new RowMapper<Permit>() {
        public Permit mapRow(ResultSet rs, int rowNum) throws SQLException {
            Permit p = new Permit();
            p.setId(rs.getLong("id"));
            p.setReference(rs.getString("reference"));
            p.setApplicationId(rs.getLong("application_id"));
            p.setApplicationReference(rs.getString("application_reference"));
            p.setHaulierId(rs.getLong("haulier_id"));
            p.setHaulierName(rs.getString("haulier_name"));
            p.setRouteCode(rs.getString("route_code"));
            p.setFirstMoveDate(rs.getDate("first_move_date"));
            p.setLastMoveDate(rs.getDate("last_move_date"));
            p.setStatus(rs.getString("status"));
            p.setIssuedBy(rs.getString("issued_by"));
            p.setIssuedAt(rs.getTimestamp("issued_at", DbTime.utc()));
            p.setStatusReason(rs.getString("status_reason"));
            p.setStatusChangedAt(rs.getTimestamp("status_changed_at", DbTime.utc()));
            long closureId = rs.getLong("suspended_by_closure_id");
            p.setSuspendedByClosureId(rs.wasNull() ? null : Long.valueOf(closureId));
            p.setVersion(rs.getInt("version"));
            p.setWindowStart(rs.getDate("window_start"));
            p.setWindowEnd(rs.getDate("window_end"));
            return p;
        }
    };

    /**
     * Find by id.
     *
     * @param id the id
     * @return the permit, or null if not found
     */
    public Permit findById(long id) {
        List<Permit> list = jdbcTemplate.query(FIND_BY_ID, MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Find by reference.
     *
     * @param reference the permit reference
     * @return the permit, or null if not found
     */
    public Permit findByReference(PermitReference reference) {
        List<Permit> list = jdbcTemplate.query(FIND_BY_REFERENCE, MAPPER, reference.toString());
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Find by application.
     *
     * @param applicationReference the application reference
     * @return the application's permit, or null if none has been issued
     */
    public Permit findByApplication(String applicationReference) {
        List<Permit> list = jdbcTemplate.query(FIND_BY_APPLICATION, MAPPER, applicationReference);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Find recent.
     *
     * @param limit the maximum number of permits
     * @return the most recently issued permits
     */
    public List<Permit> findRecent(int limit) {
        return jdbcTemplate.query(FIND_RECENT, MAPPER, limit);
    }

    /**
     * Find active crossing.
     *
     * @param structureId the closed structure
     * @param from the first day of the closure
     * @param to the last day of the closure
     * @return active permits crossing the structure during the closure
     */
    public List<Permit> findActiveCrossing(long structureId, Date from, Date to) {
        return jdbcTemplate.query(FIND_ACTIVE_CROSSING, MAPPER, new java.sql.Date(to.getTime()),
                new java.sql.Date(from.getTime()), structureId);
    }

    /**
     * Find suspended by closure.
     *
     * @param closureId the closure id
     * @return permits suspended by the closure
     */
    public List<Permit> findSuspendedByClosure(long closureId) {
        return jdbcTemplate.query(FIND_SUSPENDED_BY_CLOSURE, MAPPER, closureId);
    }

    /**
     * Find to expire.
     *
     * @param today today's date
     * @return active and suspended permits whose last move date is before today
     */
    public List<Permit> findToExpire(Date today) {
        return jdbcTemplate.query(FIND_TO_EXPIRE, MAPPER, new java.sql.Date(today.getTime()));
    }

    /**
     * Insert. The permit is created ACTIVE.
     *
     * @param reference the permit reference
     * @param applicationId the application id
     * @param issuedBy the officer issuing the permit
     * @return the new id
     */
    public long insert(final PermitReference reference, final long applicationId, final String issuedBy) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(INSERT, new String[] {"id"});
                ps.setString(1, reference.toString());
                ps.setLong(2, applicationId);
                ps.setString(3, issuedBy);
                return ps;
            }
        }, keyHolder);
        long id = keyHolder.getKey().longValue();
        log.info("Created permit " + reference + " for application " + applicationId);
        return id;
    }

    /**
     * Update status.
     *
     * @param permitId the permit id
     * @param status the new status
     * @param reason the reason, or null
     * @param closureId the closure that suspended the permit, or null
     */
    public void updateStatus(long permitId, String status, String reason, Long closureId) {
        jdbcTemplate.update(UPDATE_STATUS, status, reason, closureId, permitId);
        log.info("Permit " + permitId + " status set to " + status);
    }

    /**
     * Insert document.
     *
     * @param permitId the permit id
     * @param content the rendered document
     * @param contentType the content type
     */
    public void insertDocument(long permitId, byte[] content, String contentType) {
        jdbcTemplate.update(INSERT_DOCUMENT, permitId, content, contentType);
        log.info("Stored document for permit " + permitId + " (" + content.length + " bytes)");
    }

    /**
     * Find document.
     *
     * @param permitId the permit id
     * @return the latest document, or null if none
     */
    public byte[] findDocument(long permitId) {
        List<byte[]> list = jdbcTemplate.queryForList(FIND_DOCUMENT, byte[].class, permitId);
        return list.isEmpty() ? null : list.get(0);
    }
}
