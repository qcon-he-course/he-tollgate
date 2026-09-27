/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: UserDao.java 1031 2017-08-29 10:05:33Z koduya $
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
 * Data access for staff users and their roles.
 * Implements BR-40.
 *
 * @author K. Oduya
 * @since 1.0
 */
@Repository
public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final String SELECT_USER =
            "SELECT id, username, password_hash, full_name, email, enabled, created_at, is_panel_chair "
            + "FROM app_user ";

    private static final String FIND_ALL = SELECT_USER + "ORDER BY username";

    private static final String FIND_BY_ID = SELECT_USER + "WHERE id = ?";

    private static final String FIND_BY_USERNAME = SELECT_USER + "WHERE username = ?";

    private static final String FIND_ROLES =
            "SELECT role FROM user_role WHERE user_id = ? ORDER BY role";

    private static final String INSERT =
            "INSERT INTO app_user (username, password_hash, full_name, email, enabled, created_at) "
            + "VALUES (?, ?, ?, ?, ?, " + DbTime.NOW_UTC + ")";

    private static final String UPDATE =
            "UPDATE app_user SET full_name = ?, email = ?, enabled = ?, is_panel_chair = ? WHERE id = ?";

    private static final String UPDATE_PASSWORD =
            "UPDATE app_user SET password_hash = ? WHERE id = ?";

    private static final String DELETE_ROLES = "DELETE FROM user_role WHERE user_id = ?";

    private static final String INSERT_ROLE = "INSERT INTO user_role (user_id, role) VALUES (?, ?)";

    /** Staff roles (BR-40). */
    public static final String[] ROLES = {"OFFICER", "ENGINEER", "ADMIN"};

    private static final String FIND_PANEL_CHAIR_EMAILS =
            "SELECT email FROM app_user WHERE is_panel_chair AND enabled AND email IS NOT NULL ORDER BY username";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<StaffUser> MAPPER = new RowMapper<StaffUser>() {
        public StaffUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            StaffUser u = new StaffUser();
            u.setId(rs.getLong("id"));
            u.setUsername(rs.getString("username"));
            u.setPasswordHash(rs.getString("password_hash"));
            u.setFullName(rs.getString("full_name"));
            u.setEmail(rs.getString("email"));
            u.setEnabled(rs.getBoolean("enabled"));
            u.setCreatedAt(rs.getTimestamp("created_at", DbTime.utc()));
            u.setPanelChair(rs.getBoolean("is_panel_chair"));
            return u;
        }
    };

    /**
     * Find all. Roles are loaded.
     *
     * @return all users ordered by username
     */
    public List<StaffUser> findAll() {
        List<StaffUser> users = jdbcTemplate.query(FIND_ALL, MAPPER);
        for (StaffUser u : users) {
            u.setRoles(findRoles(u.getId()));
        }
        return users;
    }

    /**
     * Find by id. Roles are loaded.
     *
     * @param id the id
     * @return the user, or null if not found
     */
    public StaffUser findById(long id) {
        List<StaffUser> list = jdbcTemplate.query(FIND_BY_ID, MAPPER, id);
        if (list.isEmpty()) {
            return null;
        }
        StaffUser u = list.get(0);
        u.setRoles(findRoles(u.getId()));
        return u;
    }

    /**
     * Find by username. Roles are loaded.
     *
     * @param username the username
     * @return the user, or null if not found
     */
    public StaffUser findByUsername(String username) {
        List<StaffUser> list = jdbcTemplate.query(FIND_BY_USERNAME, MAPPER, username);
        if (list.isEmpty()) {
            return null;
        }
        StaffUser u = list.get(0);
        u.setRoles(findRoles(u.getId()));
        return u;
    }

    /**
     * Find roles.
     *
     * @param userId the user id
     * @return the user's roles
     */
    public List<String> findRoles(long userId) {
        return jdbcTemplate.queryForList(FIND_ROLES, String.class, userId);
    }

    /**
     * Insert. The password must already be hashed.
     *
     * @param user the user
     * @return the new id
     */
    public long insert(final StaffUser user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(INSERT, new String[] {"id"});
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPasswordHash());
                ps.setString(3, user.getFullName());
                ps.setString(4, user.getEmail());
                ps.setBoolean(5, user.isEnabled());
                return ps;
            }
        }, keyHolder);
        long id = keyHolder.getKey().longValue();
        log.info("Created user " + user.getUsername());
        return id;
    }

    /**
     * Update.
     *
     * @param user the user
     */
    public void update(StaffUser user) {
        jdbcTemplate.update(UPDATE, user.getFullName(), user.getEmail(), user.isEnabled(), user.isPanelChair(), user.getId());
        log.info("Updated user " + user.getId());
    }

    public List<String> findPanelChairEmails() {
        return jdbcTemplate.queryForList(FIND_PANEL_CHAIR_EMAILS, String.class);
    }

    /**
     * Update password.
     *
     * @param userId the user id
     * @param passwordHash the hashed password
     */
    public void updatePassword(long userId, String passwordHash) {
        jdbcTemplate.update(UPDATE_PASSWORD, passwordHash, userId);
        log.info("Password reset for user " + userId);
    }

    /**
     * Set roles. Replaces the user's roles.
     *
     * @param userId the user id
     * @param roles the roles
     */
    public void setRoles(long userId, List<String> roles) {
        jdbcTemplate.update(DELETE_ROLES, userId);
        for (int i = 0; i < roles.size(); i++) {
            jdbcTemplate.update(INSERT_ROLE, userId, roles.get(i));
        }
        log.info("Set roles for user " + userId + " to " + roles);
    }
}
