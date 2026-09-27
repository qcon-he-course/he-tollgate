/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: StructureDao.java 1004 2017-08-08 10:12:45Z gferrand $
 */
package com.halvern.tollgate.reference;

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
 * Data access for structures.
 * Implements BR-05.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Repository
public class StructureDao {

    private static final Logger log = LoggerFactory.getLogger(StructureDao.class);

    private static final String SELECT_STRUCTURE =
            "SELECT id, code, name, structure_type, mass_rating_t, clearance_m, estuary "
            + "FROM structure ";

    private static final String FIND_ALL = SELECT_STRUCTURE + "ORDER BY code";

    private static final String FIND_BY_ID = SELECT_STRUCTURE + "WHERE id = ?";

    private static final String FIND_BY_CODE = SELECT_STRUCTURE + "WHERE code = ?";

    private static final String UPDATE =
            "UPDATE structure "
            + "SET name = ?, mass_rating_t = ?, clearance_m = ? "
            + "WHERE id = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** Maps a structure row. Shared with RouteDao for segment queries. */
    static final RowMapper<Structure> STRUCTURE_MAPPER = new RowMapper<Structure>() {
        public Structure mapRow(ResultSet rs, int rowNum) throws SQLException {
            Structure s = new Structure();
            s.setId(rs.getLong("id"));
            s.setCode(rs.getString("code"));
            s.setName(rs.getString("name"));
            s.setStructureType(rs.getString("structure_type"));
            int rating = rs.getInt("mass_rating_t");
            s.setMassRatingT(rs.wasNull() ? null : Integer.valueOf(rating));
            s.setClearanceM(rs.getBigDecimal("clearance_m"));
            s.setEstuary(rs.getBoolean("estuary"));
            return s;
        }
    };

    /**
     * Find all.
     *
     * @return all structures ordered by code
     */
    public List<Structure> findAll() {
        return jdbcTemplate.query(FIND_ALL, STRUCTURE_MAPPER);
    }

    /**
     * Find by id.
     *
     * @param id the id
     * @return the structure, or null if not found
     */
    public Structure findById(long id) {
        List<Structure> list = jdbcTemplate.query(FIND_BY_ID, STRUCTURE_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Find by code.
     *
     * @param code the code
     * @return the structure, or null if not found
     */
    public Structure findByCode(String code) {
        List<Structure> list = jdbcTemplate.query(FIND_BY_CODE, STRUCTURE_MAPPER, code);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Update.
     *
     * @param structure the structure
     */
    public void update(Structure structure) {
        jdbcTemplate.update(UPDATE, structure.getName(), structure.getMassRatingT(),
                structure.getClearanceM(), structure.getId());
        log.info("Updated structure " + structure.getCode());
    }
}
