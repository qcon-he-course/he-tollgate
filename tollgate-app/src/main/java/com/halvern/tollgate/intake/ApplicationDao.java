/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: ApplicationDao.java 1102 2017-10-19 13:47:52Z gferrand $
 */
package com.halvern.tollgate.intake;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
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
 * Data access for applications, their trips and attachments.
 * Implements BR-09, BR-10 and BR-13.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Repository
public class ApplicationDao {

    private static final Logger log = LoggerFactory.getLogger(ApplicationDao.class);

    private static final String SELECT_APPLICATION =
            "SELECT a.id, a.reference, a.haulier_id, h.name AS haulier_name, "
            + "       a.vehicle_id, v.registration AS vehicle_registration, v.category AS vehicle_category, "
            + "       a.load_type_id, lt.code AS load_type_code, a.load_description, "
            + "       a.gross_mass_kg, a.width_m, a.length_m, a.height_m, a.hazardous, a.un_number, "
            + "       a.route_id, r.code AS route_code, a.first_move_date, a.last_move_date, a.status, "
            + "       a.created_by, a.created_at, a.lodged_by, a.lodged_at "
            + "FROM movement_application a "
            + "JOIN haulier h ON h.id = a.haulier_id "
            + "LEFT JOIN vehicle v ON v.id = a.vehicle_id "
            + "LEFT JOIN load_type lt ON lt.id = a.load_type_id "
            + "LEFT JOIN route r ON r.id = a.route_id ";

    private static final String FIND_BY_REFERENCE = SELECT_APPLICATION + "WHERE a.reference = ?";

    private static final String FIND_BY_ID = SELECT_APPLICATION + "WHERE a.id = ?";

    private static final String FIND_RECENT =
            SELECT_APPLICATION
            + "ORDER BY a.created_at DESC "
            + "LIMIT ?";

    private static final String INSERT =
            "INSERT INTO movement_application (reference, haulier_id, vehicle_id, load_type_id, load_description, "
            + "    gross_mass_kg, width_m, length_m, height_m, hazardous, un_number, route_id, "
            + "    first_move_date, last_move_date, status, created_by, created_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + DbTime.NOW_UTC + ")";

    private static final String UPDATE =
            "UPDATE movement_application "
            + "SET haulier_id = ?, vehicle_id = ?, load_type_id = ?, load_description = ?, "
            + "    gross_mass_kg = ?, width_m = ?, length_m = ?, height_m = ?, hazardous = ?, un_number = ?, "
            + "    route_id = ?, first_move_date = ?, last_move_date = ? "
            + "WHERE id = ? AND status = 'DRAFT'";

    private static final String MARK_LODGED =
            "UPDATE movement_application "
            + "SET status = 'SUBMITTED', lodged_by = ?, lodged_at = " + DbTime.NOW_UTC + " "
            + "WHERE id = ? AND status = 'DRAFT'";

    private static final String UPDATE_STATUS =
            "UPDATE movement_application SET status = ? WHERE id = ?";

    private static final String FIND_TRIPS =
            "SELECT id, application_id, trip_no, departure_at, crossing_at "
            + "FROM application_trip "
            + "WHERE application_id = ? "
            + "ORDER BY trip_no";

    private static final String DELETE_TRIPS = "DELETE FROM application_trip WHERE application_id = ?";

    private static final String INSERT_TRIP =
            "INSERT INTO application_trip (application_id, trip_no, departure_at, crossing_at) "
            + "VALUES (?, ?, ?, ?)";

    private static final String FIND_ATTACHMENTS =
            "SELECT id, application_id, file_name, attachment_type, size_bytes, storage_path, uploaded_by, uploaded_at "
            + "FROM attachment ";

    private static final String INSERT_ATTACHMENT =
            "INSERT INTO attachment (application_id, file_name, attachment_type, size_bytes, storage_path, "
            + "    uploaded_by, uploaded_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, " + DbTime.NOW_UTC + ")";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<MovementApplication> APPLICATION_MAPPER = new RowMapper<MovementApplication>() {
        public MovementApplication mapRow(ResultSet rs, int rowNum) throws SQLException {
            MovementApplication a = new MovementApplication();
            a.setId(rs.getLong("id"));
            a.setReference(rs.getString("reference"));
            a.setHaulierId(rs.getLong("haulier_id"));
            a.setHaulierName(rs.getString("haulier_name"));
            a.setVehicleId(getLong(rs, "vehicle_id"));
            a.setVehicleRegistration(rs.getString("vehicle_registration"));
            a.setVehicleCategory(rs.getString("vehicle_category"));
            a.setLoadTypeId(getLong(rs, "load_type_id"));
            a.setLoadTypeCode(rs.getString("load_type_code"));
            a.setLoadDescription(rs.getString("load_description"));
            int mass = rs.getInt("gross_mass_kg");
            a.setGrossMassKg(rs.wasNull() ? null : Integer.valueOf(mass));
            a.setWidthM(rs.getBigDecimal("width_m"));
            a.setLengthM(rs.getBigDecimal("length_m"));
            a.setHeightM(rs.getBigDecimal("height_m"));
            a.setHazardous(rs.getBoolean("hazardous"));
            a.setUnNumber(rs.getString("un_number"));
            a.setRouteId(getLong(rs, "route_id"));
            a.setRouteCode(rs.getString("route_code"));
            a.setFirstMoveDate(rs.getDate("first_move_date"));
            a.setLastMoveDate(rs.getDate("last_move_date"));
            a.setStatus(rs.getString("status"));
            a.setCreatedBy(rs.getString("created_by"));
            a.setCreatedAt(rs.getTimestamp("created_at", DbTime.utc()));
            a.setLodgedBy(rs.getString("lodged_by"));
            a.setLodgedAt(rs.getTimestamp("lodged_at", DbTime.utc()));
            return a;
        }
    };

    private static final RowMapper<ApplicationTrip> TRIP_MAPPER = new RowMapper<ApplicationTrip>() {
        public ApplicationTrip mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApplicationTrip t = new ApplicationTrip();
            t.setId(rs.getLong("id"));
            t.setApplicationId(rs.getLong("application_id"));
            t.setTripNo(rs.getInt("trip_no"));
            t.setDepartureAt(rs.getTimestamp("departure_at", DbTime.utc()));
            t.setCrossingAt(rs.getTimestamp("crossing_at", DbTime.utc()));
            return t;
        }
    };

    private static final RowMapper<Attachment> ATTACHMENT_MAPPER = new RowMapper<Attachment>() {
        public Attachment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Attachment a = new Attachment();
            a.setId(rs.getLong("id"));
            a.setApplicationId(rs.getLong("application_id"));
            a.setFileName(rs.getString("file_name"));
            a.setAttachmentType(rs.getString("attachment_type"));
            a.setSizeBytes(rs.getLong("size_bytes"));
            a.setStoragePath(rs.getString("storage_path"));
            a.setUploadedBy(rs.getString("uploaded_by"));
            a.setUploadedAt(rs.getTimestamp("uploaded_at", DbTime.utc()));
            return a;
        }
    };

    private static Long getLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : Long.valueOf(value);
    }

    /**
     * Find by reference. Trips and attachments are loaded.
     *
     * @param reference the application reference
     * @return the application, or null if not found
     */
    public MovementApplication findByReference(String reference) {
        List<MovementApplication> list = jdbcTemplate.query(FIND_BY_REFERENCE, APPLICATION_MAPPER, reference);
        if (list.isEmpty()) {
            return null;
        }
        MovementApplication app = list.get(0);
        app.setTrips(findTrips(app.getId().longValue()));
        app.setAttachments(findAttachments(app.getId().longValue()));
        return app;
    }

    /**
     * Find by id. Trips and attachments are loaded.
     *
     * @param id the id
     * @return the application, or null if not found
     */
    public MovementApplication findById(long id) {
        List<MovementApplication> list = jdbcTemplate.query(FIND_BY_ID, APPLICATION_MAPPER, id);
        if (list.isEmpty()) {
            return null;
        }
        MovementApplication app = list.get(0);
        app.setTrips(findTrips(id));
        app.setAttachments(findAttachments(id));
        return app;
    }

    /**
     * Find recent. Trips and attachments are not loaded.
     *
     * @param limit the maximum number of applications
     * @return the most recently created applications
     */
    public List<MovementApplication> findRecent(int limit) {
        return jdbcTemplate.query(FIND_RECENT, APPLICATION_MAPPER, limit);
    }

    /**
     * Insert a draft. Trips are saved separately.
     *
     * @param app the application
     * @return the new id
     */
    public long insert(final MovementApplication app) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(INSERT, new String[] {"id"});
                ps.setString(1, app.getReference());
                setApplicationFields(ps, 2, app);
                ps.setString(15, app.getStatus());
                ps.setString(16, app.getCreatedBy());
                return ps;
            }
        }, keyHolder);
        long id = keyHolder.getKey().longValue();
        log.info("Created application " + app.getReference() + " by " + app.getCreatedBy());
        return id;
    }

    /**
     * Update a draft. Lodged applications are not changed.
     *
     * @param app the application
     * @return true if the draft was updated
     */
    public boolean update(final MovementApplication app) {
        int rows = jdbcTemplate.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(UPDATE);
                setApplicationFields(ps, 1, app);
                ps.setLong(14, app.getId().longValue());
                return ps;
            }
        });
        log.info("Updated application " + app.getReference() + " (" + rows + " row)");
        return rows == 1;
    }

    /**
     * Mark lodged. Sets the status to SUBMITTED.
     *
     * @param id the application id
     * @param username the user lodging the application
     * @return true if the draft was lodged
     */
    public boolean markLodged(long id, String username) {
        return jdbcTemplate.update(MARK_LODGED, username, id) == 1;
    }

    /**
     * Update status.
     *
     * @param id the application id
     * @param status the new status
     */
    public void updateStatus(long id, String status) {
        jdbcTemplate.update(UPDATE_STATUS, status, id);
        log.info("Application " + id + " status set to " + status);
    }

    /**
     * Find trips.
     *
     * @param applicationId the application id
     * @return the trips in order
     */
    public List<ApplicationTrip> findTrips(long applicationId) {
        return jdbcTemplate.query(FIND_TRIPS, TRIP_MAPPER, applicationId);
    }

    /**
     * Replace trips. Trips are numbered in the order given.
     *
     * @param applicationId the application id
     * @param trips the trips
     */
    public void replaceTrips(final long applicationId, List<ApplicationTrip> trips) {
        jdbcTemplate.update(DELETE_TRIPS, applicationId);
        for (int i = 0; i < trips.size(); i++) {
            final ApplicationTrip trip = trips.get(i);
            final int tripNo = i + 1;
            jdbcTemplate.update(new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement ps = con.prepareStatement(INSERT_TRIP);
                    ps.setLong(1, applicationId);
                    ps.setInt(2, tripNo);
                    ps.setTimestamp(3, toTimestamp(trip.getDepartureAt()), DbTime.utc());
                    ps.setTimestamp(4, toTimestamp(trip.getCrossingAt()), DbTime.utc());
                    return ps;
                }
            });
        }
    }

    /**
     * Find attachments.
     *
     * @param applicationId the application id
     * @return the attachments in upload order
     */
    public List<Attachment> findAttachments(long applicationId) {
        return jdbcTemplate.query(FIND_ATTACHMENTS + "WHERE application_id = ? ORDER BY id",
                ATTACHMENT_MAPPER, applicationId);
    }

    /**
     * Find attachment.
     *
     * @param attachmentId the attachment id
     * @return the attachment, or null if not found
     */
    public Attachment findAttachment(long attachmentId) {
        List<Attachment> list = jdbcTemplate.query(FIND_ATTACHMENTS + "WHERE id = ?", ATTACHMENT_MAPPER,
                attachmentId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Insert attachment.
     *
     * @param attachment the attachment
     */
    public void insertAttachment(Attachment attachment) {
        jdbcTemplate.update(INSERT_ATTACHMENT, attachment.getApplicationId(), attachment.getFileName(),
                attachment.getAttachmentType(), attachment.getSizeBytes(), attachment.getStoragePath(),
                attachment.getUploadedBy());
        log.info("Attachment " + attachment.getFileName() + " added to application " + attachment.getApplicationId());
    }

    private static void setApplicationFields(PreparedStatement ps, int start, MovementApplication app)
            throws SQLException {
        ps.setLong(start, app.getHaulierId().longValue());
        ps.setObject(start + 1, app.getVehicleId(), Types.BIGINT);
        ps.setObject(start + 2, app.getLoadTypeId(), Types.BIGINT);
        ps.setString(start + 3, app.getLoadDescription());
        ps.setObject(start + 4, app.getGrossMassKg(), Types.INTEGER);
        ps.setBigDecimal(start + 5, app.getWidthM());
        ps.setBigDecimal(start + 6, app.getLengthM());
        ps.setBigDecimal(start + 7, app.getHeightM());
        ps.setBoolean(start + 8, app.isHazardous());
        ps.setString(start + 9, app.getUnNumber());
        ps.setObject(start + 10, app.getRouteId(), Types.BIGINT);
        ps.setDate(start + 11, toSqlDate(app.getFirstMoveDate()));
        ps.setDate(start + 12, toSqlDate(app.getLastMoveDate()));
    }

    private static java.sql.Date toSqlDate(java.util.Date date) {
        return date == null ? null : new java.sql.Date(date.getTime());
    }

    private static Timestamp toTimestamp(java.util.Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }
}
