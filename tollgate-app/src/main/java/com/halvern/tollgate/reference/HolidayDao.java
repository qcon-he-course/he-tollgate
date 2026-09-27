/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: HolidayDao.java 1013 2017-08-15 16:08:55Z rmistry $
 */
package com.halvern.tollgate.reference;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Data access for the holiday calendar.
 * Implements BR-14.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Repository
public class HolidayDao {

    private static final Logger log = LoggerFactory.getLogger(HolidayDao.class);

    private static final String SELECT_HOLIDAY =
            "SELECT id, holiday_date, description FROM holiday ";

    private static final String FIND_ALL = SELECT_HOLIDAY + "ORDER BY holiday_date";

    private static final String FIND_BY_YEAR =
            SELECT_HOLIDAY
            + "WHERE EXTRACT(YEAR FROM holiday_date) = ? "
            + "ORDER BY holiday_date";

    private static final String FIND_BETWEEN =
            SELECT_HOLIDAY
            + "WHERE holiday_date BETWEEN ? AND ? "
            + "ORDER BY holiday_date";

    private static final String COUNT_ON_DATE =
            "SELECT COUNT(*) FROM holiday WHERE holiday_date = ?";

    private static final String INSERT =
            "INSERT INTO holiday (holiday_date, description) VALUES (?, ?)";

    private static final String DELETE = "DELETE FROM holiday WHERE id = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<Holiday> MAPPER = new RowMapper<Holiday>() {
        public Holiday mapRow(ResultSet rs, int rowNum) throws SQLException {
            Holiday h = new Holiday();
            h.setId(rs.getLong("id"));
            h.setHolidayDate(rs.getDate("holiday_date"));
            h.setDescription(rs.getString("description"));
            return h;
        }
    };

    /**
     * Find all.
     *
     * @return all holidays in date order
     */
    public List<Holiday> findAll() {
        return jdbcTemplate.query(FIND_ALL, MAPPER);
    }

    /**
     * Find by year.
     *
     * @param year the year
     * @return the holidays in that year
     */
    public List<Holiday> findByYear(int year) {
        return jdbcTemplate.query(FIND_BY_YEAR, MAPPER, year);
    }

    /**
     * Find between.
     *
     * @param from the first date, inclusive
     * @param to the last date, inclusive
     * @return the holidays between the two dates
     */
    public List<Holiday> findBetween(Date from, Date to) {
        return jdbcTemplate.query(FIND_BETWEEN, MAPPER,
                new java.sql.Date(from.getTime()), new java.sql.Date(to.getTime()));
    }

    /**
     * Is holiday.
     *
     * @param date the date
     * @return true if the date is in the holiday calendar
     */
    public boolean isHoliday(Date date) {
        Integer count = jdbcTemplate.queryForObject(COUNT_ON_DATE, Integer.class,
                new java.sql.Date(date.getTime()));
        return count != null && count.intValue() > 0;
    }

    /**
     * Insert.
     *
     * @param holiday the holiday
     */
    public void insert(Holiday holiday) {
        jdbcTemplate.update(INSERT, new java.sql.Date(holiday.getHolidayDate().getTime()),
                holiday.getDescription());
        log.info("Added holiday " + holiday.getHolidayDate() + " " + holiday.getDescription());
    }

    /**
     * Delete.
     *
     * @param id the id
     */
    public void delete(long id) {
        jdbcTemplate.update(DELETE, id);
        log.info("Deleted holiday " + id);
    }
}
