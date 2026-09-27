/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: LoadTypeDao.java 1009 2017-08-10 09:51:30Z gferrand $
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
 * Data access for load types.
 * Implements BR-08.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Repository
public class LoadTypeDao {

    private static final Logger log = LoggerFactory.getLogger(LoadTypeDao.class);

    private static final String FIND_ALL =
            "SELECT id, code, description FROM load_type ORDER BY code";

    private static final String FIND_BY_ID =
            "SELECT id, code, description FROM load_type WHERE id = ?";

    private static final String FIND_BY_CODE =
            "SELECT id, code, description FROM load_type WHERE code = ?";

    private static final String UPDATE_DESCRIPTION =
            "UPDATE load_type SET description = ? WHERE id = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<LoadType> MAPPER = new RowMapper<LoadType>() {
        public LoadType mapRow(ResultSet rs, int rowNum) throws SQLException {
            LoadType lt = new LoadType();
            lt.setId(rs.getLong("id"));
            lt.setCode(rs.getString("code"));
            lt.setDescription(rs.getString("description"));
            return lt;
        }
    };

    /**
     * Find all.
     *
     * @return all load types
     */
    public List<LoadType> findAll() {
        return jdbcTemplate.query(FIND_ALL, MAPPER);
    }

    /**
     * Find by id.
     *
     * @param id the id
     * @return the load type, or null if not found
     */
    public LoadType findById(long id) {
        List<LoadType> list = jdbcTemplate.query(FIND_BY_ID, MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Find by code.
     *
     * @param code the code
     * @return the load type, or null if not found
     */
    public LoadType findByCode(String code) {
        List<LoadType> list = jdbcTemplate.query(FIND_BY_CODE, MAPPER, code);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Update description.
     *
     * @param id the id
     * @param description the description
     */
    public void updateDescription(long id, String description) {
        jdbcTemplate.update(UPDATE_DESCRIPTION, description, id);
        log.info("Updated description of load type " + id);
    }
}
