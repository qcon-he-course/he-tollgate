/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: ClosureDao.java 1184 2018-02-07 10:55:44Z rmistry $
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
 * Data access for structure closures.
 * Implements BR-33.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Repository
public class ClosureDao {

    private static final Logger log = LoggerFactory.getLogger(ClosureDao.class);

    private static final String SELECT_CLOSURE =
            "SELECT c.id, c.structure_id, s.code AS structure_code, s.name AS structure_name, "
            + "       c.start_date, c.end_date, c.reason, c.status, c.recorded_by, c.recorded_at, "
            + "       c.ended_by, c.ended_at "
            + "FROM structure_closure c "
            + "JOIN structure s ON s.id = c.structure_id ";

    private static final String FIND_BY_ID = SELECT_CLOSURE + "WHERE c.id = ?";

    private static final String FIND_RECENT = SELECT_CLOSURE + "ORDER BY c.start_date DESC LIMIT ?";

    private static final String FIND_ACTIVE_ENDING_BEFORE =
            SELECT_CLOSURE + "WHERE c.status = 'ACTIVE' AND c.end_date < ?";

    private static final String INSERT =
            "INSERT INTO structure_closure (structure_id, start_date, end_date, reason, status, recorded_by, "
            + "    recorded_at) "
            + "VALUES (?, ?, ?, ?, 'ACTIVE', ?, " + DbTime.NOW_UTC + ")";

    private static final String END =
            "UPDATE structure_closure "
            + "SET status = 'ENDED', ended_by = ?, ended_at = " + DbTime.NOW_UTC + " "
            + "WHERE id = ? AND status = 'ACTIVE'";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<StructureClosure> MAPPER = new RowMapper<StructureClosure>() {
        public StructureClosure mapRow(ResultSet rs, int rowNum) throws SQLException {
            StructureClosure c = new StructureClosure();
            c.setId(rs.getLong("id"));
            c.setStructureId(rs.getLong("structure_id"));
            c.setStructureCode(rs.getString("structure_code"));
            c.setStructureName(rs.getString("structure_name"));
            c.setStartDate(rs.getDate("start_date"));
            c.setEndDate(rs.getDate("end_date"));
            c.setReason(rs.getString("reason"));
            c.setStatus(rs.getString("status"));
            c.setRecordedBy(rs.getString("recorded_by"));
            c.setRecordedAt(rs.getTimestamp("recorded_at", DbTime.utc()));
            c.setEndedBy(rs.getString("ended_by"));
            c.setEndedAt(rs.getTimestamp("ended_at", DbTime.utc()));
            return c;
        }
    };

    /**
     * Find by id.
     *
     * @param id the id
     * @return the closure, or null if not found
     */
    public StructureClosure findById(long id) {
        List<StructureClosure> list = jdbcTemplate.query(FIND_BY_ID, MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Find recent.
     *
     * @param limit the maximum number of closures
     * @return closures, latest start first
     */
    public List<StructureClosure> findRecent(int limit) {
        return jdbcTemplate.query(FIND_RECENT, MAPPER, limit);
    }

    /**
     * Find active ending before.
     *
     * @param date the date
     * @return active closures whose last day is before the date
     */
    public List<StructureClosure> findActiveEndingBefore(Date date) {
        return jdbcTemplate.query(FIND_ACTIVE_ENDING_BEFORE, MAPPER, new java.sql.Date(date.getTime()));
    }

    /**
     * Insert.
     *
     * @param closure the closure
     * @return the new id
     */
    public long insert(final StructureClosure closure) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(INSERT, new String[] {"id"});
                ps.setLong(1, closure.getStructureId().longValue());
                ps.setDate(2, new java.sql.Date(closure.getStartDate().getTime()));
                ps.setDate(3, new java.sql.Date(closure.getEndDate().getTime()));
                ps.setString(4, closure.getReason());
                ps.setString(5, closure.getRecordedBy());
                return ps;
            }
        }, keyHolder);
        long id = keyHolder.getKey().longValue();
        log.info("Recorded closure " + id + " of structure " + closure.getStructureId());
        return id;
    }

    /**
     * End.
     *
     * @param id the closure id
     * @param username the user ending the closure
     * @return true if the closure was active and is now ended
     */
    public boolean end(long id, String username) {
        return jdbcTemplate.update(END, username, id) == 1;
    }
}
