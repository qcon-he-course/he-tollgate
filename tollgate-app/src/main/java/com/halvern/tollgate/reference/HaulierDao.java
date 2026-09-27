/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: HaulierDao.java 1021 2017-08-22 11:26:40Z rmistry $
 */
package com.halvern.tollgate.reference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * Data access for hauliers, their operator licences and insurance policies.
 * Implements BR-02 and BR-03.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Repository
public class HaulierDao {

    private static final Logger log = LoggerFactory.getLogger(HaulierDao.class);

    private static final String SELECT_HAULIER =
            "SELECT id, name, contact_name, contact_email, phone, address_line1, town, postcode, created_at "
            + "FROM haulier ";

    private static final String FIND_ALL = SELECT_HAULIER + "ORDER BY name";

    private static final String FIND_BY_ID = SELECT_HAULIER + "WHERE id = ?";

    private static final String INSERT =
            "INSERT INTO haulier (name, contact_name, contact_email, phone, address_line1, town, postcode, created_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, " + DbTime.NOW_UTC + ")";

    private static final String UPDATE =
            "UPDATE haulier "
            + "SET name = ?, contact_name = ?, contact_email = ?, phone = ?, "
            + "    address_line1 = ?, town = ?, postcode = ? "
            + "WHERE id = ?";

    // Latest licence on file is the current one (BR-02)
    private static final String FIND_LICENCE =
            "SELECT id, haulier_id, licence_number, valid_from, expiry_date "
            + "FROM operator_licence "
            + "WHERE haulier_id = ? "
            + "ORDER BY expiry_date DESC "
            + "LIMIT 1";

    private static final String INSERT_LICENCE =
            "INSERT INTO operator_licence (haulier_id, licence_number, valid_from, expiry_date) "
            + "VALUES (?, ?, ?, ?)";

    private static final String UPDATE_LICENCE =
            "UPDATE operator_licence "
            + "SET licence_number = ?, valid_from = ?, expiry_date = ? "
            + "WHERE id = ?";

    private static final String FIND_INSURANCE =
            "SELECT id, haulier_id, insurer, policy_number, start_date, end_date "
            + "FROM insurance_policy "
            + "WHERE haulier_id = ? "
            + "ORDER BY end_date DESC "
            + "LIMIT 1";

    private static final String INSERT_INSURANCE =
            "INSERT INTO insurance_policy (haulier_id, insurer, policy_number, start_date, end_date) "
            + "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_INSURANCE =
            "UPDATE insurance_policy "
            + "SET insurer = ?, policy_number = ?, start_date = ?, end_date = ? "
            + "WHERE id = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<Haulier> HAULIER_MAPPER = new RowMapper<Haulier>() {
        public Haulier mapRow(ResultSet rs, int rowNum) throws SQLException {
            Haulier h = new Haulier();
            h.setId(rs.getLong("id"));
            h.setName(rs.getString("name"));
            h.setContactName(rs.getString("contact_name"));
            h.setContactEmail(rs.getString("contact_email"));
            h.setPhone(rs.getString("phone"));
            h.setAddressLine1(rs.getString("address_line1"));
            h.setTown(rs.getString("town"));
            h.setPostcode(rs.getString("postcode"));
            h.setCreatedAt(rs.getTimestamp("created_at", DbTime.utc()));
            return h;
        }
    };

    private static final RowMapper<OperatorLicence> LICENCE_MAPPER = new RowMapper<OperatorLicence>() {
        public OperatorLicence mapRow(ResultSet rs, int rowNum) throws SQLException {
            OperatorLicence l = new OperatorLicence();
            l.setId(rs.getLong("id"));
            l.setHaulierId(rs.getLong("haulier_id"));
            l.setLicenceNumber(rs.getString("licence_number"));
            l.setValidFrom(rs.getDate("valid_from"));
            l.setExpiryDate(rs.getDate("expiry_date"));
            return l;
        }
    };

    private static final RowMapper<InsurancePolicy> INSURANCE_MAPPER = new RowMapper<InsurancePolicy>() {
        public InsurancePolicy mapRow(ResultSet rs, int rowNum) throws SQLException {
            InsurancePolicy p = new InsurancePolicy();
            p.setId(rs.getLong("id"));
            p.setHaulierId(rs.getLong("haulier_id"));
            p.setInsurer(rs.getString("insurer"));
            p.setPolicyNumber(rs.getString("policy_number"));
            p.setStartDate(rs.getDate("start_date"));
            p.setEndDate(rs.getDate("end_date"));
            return p;
        }
    };

    /**
     * Find all.
     *
     * @return all hauliers ordered by name
     */
    public List<Haulier> findAll() {
        return jdbcTemplate.query(FIND_ALL, HAULIER_MAPPER);
    }

    /**
     * Find by id.
     *
     * @param id the id
     * @return the haulier, or null if not found
     */
    public Haulier findById(long id) {
        List<Haulier> list = jdbcTemplate.query(FIND_BY_ID, HAULIER_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Insert.
     *
     * @param haulier the haulier
     * @return the new id
     */
    public long insert(final Haulier haulier) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(INSERT, new String[] {"id"});
                ps.setString(1, haulier.getName());
                ps.setString(2, haulier.getContactName());
                ps.setString(3, haulier.getContactEmail());
                ps.setString(4, haulier.getPhone());
                ps.setString(5, haulier.getAddressLine1());
                ps.setString(6, haulier.getTown());
                ps.setString(7, haulier.getPostcode());
                return ps;
            }
        }, keyHolder);
        long id = keyHolder.getKey().longValue();
        log.info("Created haulier " + id + " " + haulier.getName());
        return id;
    }

    /**
     * Update.
     *
     * @param haulier the haulier
     */
    public void update(Haulier haulier) {
        jdbcTemplate.update(UPDATE, haulier.getName(), haulier.getContactName(), haulier.getContactEmail(),
                haulier.getPhone(), haulier.getAddressLine1(), haulier.getTown(), haulier.getPostcode(),
                haulier.getId());
        log.info("Updated haulier " + haulier.getId());
    }

    /**
     * Find licence.
     *
     * @param haulierId the haulier id
     * @return the current operator licence, or null if none is on file
     */
    public OperatorLicence findLicence(long haulierId) {
        List<OperatorLicence> list = jdbcTemplate.query(FIND_LICENCE, LICENCE_MAPPER, haulierId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Save licence. Inserts when the licence has no id, otherwise updates.
     *
     * @param licence the licence
     */
    public void saveLicence(OperatorLicence licence) {
        java.sql.Date validFrom = new java.sql.Date(licence.getValidFrom().getTime());
        java.sql.Date expiry = new java.sql.Date(licence.getExpiryDate().getTime());
        if (licence.getId() == null) {
            jdbcTemplate.update(INSERT_LICENCE, licence.getHaulierId(), licence.getLicenceNumber(),
                    validFrom, expiry);
        } else {
            jdbcTemplate.update(UPDATE_LICENCE, licence.getLicenceNumber(), validFrom, expiry,
                    licence.getId());
        }
        log.info("Saved licence " + licence.getLicenceNumber() + " for haulier " + licence.getHaulierId());
    }

    /**
     * Find insurance.
     *
     * @param haulierId the haulier id
     * @return the current insurance policy, or null if none is on file
     */
    public InsurancePolicy findInsurance(long haulierId) {
        List<InsurancePolicy> list = jdbcTemplate.query(FIND_INSURANCE, INSURANCE_MAPPER, haulierId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Save insurance. Inserts when the policy has no id, otherwise updates.
     *
     * @param policy the policy
     */
    public void saveInsurance(InsurancePolicy policy) {
        java.sql.Date start = new java.sql.Date(policy.getStartDate().getTime());
        java.sql.Date end = new java.sql.Date(policy.getEndDate().getTime());
        if (policy.getId() == null) {
            jdbcTemplate.update(INSERT_INSURANCE, policy.getHaulierId(), policy.getInsurer(),
                    policy.getPolicyNumber(), start, end);
        } else {
            jdbcTemplate.update(UPDATE_INSURANCE, policy.getInsurer(), policy.getPolicyNumber(), start, end,
                    policy.getId());
        }
        log.info("Saved insurance " + policy.getPolicyNumber() + " for haulier " + policy.getHaulierId());
    }
}
