/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: VehicleDao.java 1024 2017-08-23 15:44:18Z rmistry $
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
 * Data access for vehicles.
 * Implements BR-02.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Repository
public class VehicleDao {

    private static final Logger log = LoggerFactory.getLogger(VehicleDao.class);

    private static final String SELECT_VEHICLE =
            "SELECT id, haulier_id, registration, category FROM vehicle ";

    private static final String FIND_BY_ID = SELECT_VEHICLE + "WHERE id = ?";

    private static final String FIND_BY_HAULIER =
            SELECT_VEHICLE + "WHERE haulier_id = ? ORDER BY registration";

    private static final String FIND_BY_REGISTRATION =
            SELECT_VEHICLE + "WHERE registration = ?";

    private static final String INSERT =
            "INSERT INTO vehicle (haulier_id, registration, category) VALUES (?, ?, ?)";

    private static final String UPDATE =
            "UPDATE vehicle SET registration = ?, category = ? WHERE id = ?";

    /** Vehicle categories accepted by Halvern (BR-02). */
    public static final String[] CATEGORIES = {
        "ARTIC", "BALLAST_TRACTOR", "LOW_LOADER", "MOBILE_CRANE", "MODULAR_TRAILER", "RIGID"
    };

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<Vehicle> MAPPER = new RowMapper<Vehicle>() {
        public Vehicle mapRow(ResultSet rs, int rowNum) throws SQLException {
            Vehicle v = new Vehicle();
            v.setId(rs.getLong("id"));
            v.setHaulierId(rs.getLong("haulier_id"));
            v.setRegistration(rs.getString("registration"));
            v.setCategory(rs.getString("category"));
            return v;
        }
    };

    /**
     * Find by id.
     *
     * @param id the id
     * @return the vehicle, or null if not found
     */
    public Vehicle findById(long id) {
        List<Vehicle> list = jdbcTemplate.query(FIND_BY_ID, MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Find by haulier.
     *
     * @param haulierId the haulier id
     * @return the haulier's vehicles
     */
    public List<Vehicle> findByHaulier(long haulierId) {
        return jdbcTemplate.query(FIND_BY_HAULIER, MAPPER, haulierId);
    }

    /**
     * Find by registration.
     *
     * @param registration the registration
     * @return the vehicle, or null if not found
     */
    public Vehicle findByRegistration(String registration) {
        List<Vehicle> list = jdbcTemplate.query(FIND_BY_REGISTRATION, MAPPER, registration);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Insert.
     *
     * @param vehicle the vehicle
     */
    public void insert(Vehicle vehicle) {
        jdbcTemplate.update(INSERT, vehicle.getHaulierId(), vehicle.getRegistration(), vehicle.getCategory());
        log.info("Added vehicle " + vehicle.getRegistration() + " for haulier " + vehicle.getHaulierId());
    }

    /**
     * Update.
     *
     * @param vehicle the vehicle
     */
    public void update(Vehicle vehicle) {
        jdbcTemplate.update(UPDATE, vehicle.getRegistration(), vehicle.getCategory(), vehicle.getId());
        log.info("Updated vehicle " + vehicle.getId());
    }
}
