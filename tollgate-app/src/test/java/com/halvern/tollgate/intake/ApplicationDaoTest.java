/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: ApplicationDaoTest.java 1110 2017-10-25 17:02:39Z gferrand $
 */
package com.halvern.tollgate.intake;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * Tests for ApplicationDao.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationDaoTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ApplicationDao applicationDao;

    /**
     * Unknown reference returns null.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testFindByReferenceNotFound() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("HX-A-2018-000001")))
                .thenReturn(Collections.emptyList());

        assertNull(applicationDao.findByReference("HX-A-2018-000001"));
    }

    /**
     * Found application has its trips and attachments loaded.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testFindByReferenceLoadsTrips() {
        MovementApplication app = new MovementApplication();
        app.setId(Long.valueOf(42));
        app.setReference("HX-A-2018-000042");
        List<MovementApplication> apps = new ArrayList<MovementApplication>();
        apps.add(app);
        List<ApplicationTrip> trips = new ArrayList<ApplicationTrip>();
        trips.add(new ApplicationTrip());
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("HX-A-2018-000042"))).thenReturn((List) apps);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(42L))).thenReturn((List) trips);

        MovementApplication found = applicationDao.findByReference("HX-A-2018-000042");

        assertEquals("HX-A-2018-000042", found.getReference());
        assertEquals(1, found.getTrips().size());
    }

    /**
     * Lodging updates one draft.
     */
    @Test
    public void testMarkLodged() {
        when(jdbcTemplate.update(anyString(), eq("officer1"), eq(42L))).thenReturn(Integer.valueOf(1));

        assertTrue(applicationDao.markLodged(42L, "officer1"));
    }

    /**
     * Lodging an application that is not a draft updates nothing.
     */
    @Test
    public void testMarkLodgedNotDraft() {
        when(jdbcTemplate.update(anyString(), eq("officer1"), eq(42L))).thenReturn(Integer.valueOf(0));

        assertFalse(applicationDao.markLodged(42L, "officer1"));
    }

    /**
     * Status update passes the new status.
     */
    @Test
    public void testUpdateStatus() {
        applicationDao.updateStatus(42L, MovementApplication.STATUS_APPROVED);

        verify(jdbcTemplate).update(anyString(), eq("APPROVED"), eq(42L));
    }
}
