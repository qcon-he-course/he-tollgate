/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: RouteDao.java 1006 2017-08-09 14:37:02Z gferrand $
 */
package com.halvern.tollgate.reference;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Data access for routes and route segments.
 * Implements BR-06.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Repository
public class RouteDao {

    private static final Logger log = LoggerFactory.getLogger(RouteDao.class);

    private static final String SELECT_ROUTE =
            "SELECT id, code, entry_junction, exit_junction, description "
            + "FROM route ";

    private static final String FIND_ALL = SELECT_ROUTE + "ORDER BY code";

    private static final String FIND_BY_ID = SELECT_ROUTE + "WHERE id = ?";

    private static final String FIND_BY_CODE = SELECT_ROUTE + "WHERE code = ?";

    private static final String FIND_SEGMENTS =
            "SELECT rs.id AS segment_id, rs.route_id, rs.seq, "
            + "       s.id, s.code, s.name, s.structure_type, s.mass_rating_t, s.clearance_m, s.estuary "
            + "FROM route_segment rs "
            + "JOIN structure s ON s.id = rs.structure_id "
            + "WHERE rs.route_id = ? "
            + "ORDER BY rs.seq";

    // BR-07: lowest clearance of any overbridge on the route
    private static final String FIND_MIN_CLEARANCE =
            "SELECT MIN(s.clearance_m) "
            + "FROM route_segment rs "
            + "JOIN structure s ON s.id = rs.structure_id "
            + "WHERE rs.route_id = ? "
            + "AND s.structure_type = 'OVERBRIDGE'";

    private static final String UPDATE_DESCRIPTION =
            "UPDATE route SET description = ? WHERE id = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<Route> ROUTE_MAPPER = new RowMapper<Route>() {
        public Route mapRow(ResultSet rs, int rowNum) throws SQLException {
            Route r = new Route();
            r.setId(rs.getLong("id"));
            r.setCode(rs.getString("code"));
            r.setEntryJunction(rs.getString("entry_junction"));
            r.setExitJunction(rs.getString("exit_junction"));
            r.setDescription(rs.getString("description"));
            return r;
        }
    };

    private static final RowMapper<RouteSegment> SEGMENT_MAPPER = new RowMapper<RouteSegment>() {
        public RouteSegment mapRow(ResultSet rs, int rowNum) throws SQLException {
            RouteSegment seg = new RouteSegment();
            seg.setId(rs.getLong("segment_id"));
            seg.setRouteId(rs.getLong("route_id"));
            seg.setSeq(rs.getInt("seq"));
            seg.setStructure(StructureDao.STRUCTURE_MAPPER.mapRow(rs, rowNum));
            return seg;
        }
    };

    /**
     * Find all.
     *
     * @return all routes ordered by code, without segments
     */
    public List<Route> findAll() {
        return jdbcTemplate.query(FIND_ALL, ROUTE_MAPPER);
    }

    /**
     * Find by id. Segments are loaded.
     *
     * @param id the id
     * @return the route, or null if not found
     */
    public Route findById(long id) {
        List<Route> list = jdbcTemplate.query(FIND_BY_ID, ROUTE_MAPPER, id);
        if (list.isEmpty()) {
            return null;
        }
        Route route = list.get(0);
        route.setSegments(findSegments(route.getId()));
        return route;
    }

    /**
     * Find by code. Segments are loaded.
     *
     * @param code the code
     * @return the route, or null if not found
     */
    public Route findByCode(String code) {
        List<Route> list = jdbcTemplate.query(FIND_BY_CODE, ROUTE_MAPPER, code);
        if (list.isEmpty()) {
            return null;
        }
        Route route = list.get(0);
        route.setSegments(findSegments(route.getId()));
        return route;
    }

    /**
     * Find segments.
     *
     * @param routeId the route id
     * @return the segments in order
     */
    public List<RouteSegment> findSegments(long routeId) {
        return jdbcTemplate.query(FIND_SEGMENTS, SEGMENT_MAPPER, routeId);
    }

    /**
     * Find minimum clearance.
     *
     * @param routeId the route id
     * @return the lowest overbridge clearance in metres, or null if the route has no overbridge
     */
    public BigDecimal findMinimumClearance(long routeId) {
        return jdbcTemplate.queryForObject(FIND_MIN_CLEARANCE, BigDecimal.class, routeId);
    }

    /**
     * Update description.
     *
     * @param id the id
     * @param description the description
     */
    public void updateDescription(long id, String description) {
        jdbcTemplate.update(UPDATE_DESCRIPTION, description, id);
        log.info("Updated description of route " + id);
    }
}
