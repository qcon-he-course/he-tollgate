/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: PermitReferenceGeneratorTest.java 1172 2018-01-24 09:51:13Z gferrand $
 */
package com.halvern.tollgate.permitdoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Tests for PermitReferenceGenerator and PermitReference.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class PermitReferenceGeneratorTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PermitReferenceGenerator generator;

    private final int year = Calendar.getInstance().get(Calendar.YEAR);

    /**
     * First reference of the year is number 1.
     */
    @Test
    public void testFirstReferenceOfYear() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(year))).thenReturn(Integer.valueOf(0));

        PermitReference ref = generator.nextReference();

        assertEquals(year, ref.getYear());
        assertEquals(1, ref.getNumber());
        assertEquals("HX-P-" + year + "-00001", ref.toString());
    }

    /**
     * Next reference follows the last one issued and the counter is updated.
     */
    @Test
    public void testNextReferenceFollowsCounter() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(year))).thenReturn(Integer.valueOf(1872));

        PermitReference ref = generator.nextReference();

        assertEquals("HX-P-" + year + "-01873", ref.toString());
        verify(jdbcTemplate).update(anyString(), eq(1873), eq(year));
    }

    /**
     * Parse a printed reference.
     */
    @Test
    public void testParse() {
        PermitReference ref = PermitReference.parse("HX-P-2018-01873");

        assertEquals(2018, ref.getYear());
        assertEquals(1873, ref.getNumber());
        assertEquals(new PermitReference(2018, 1873), ref);
    }

    /**
     * Parse rejects other formats.
     */
    @Test
    public void testParseInvalid() {
        assertNull(PermitReference.parse("HX-A-2018-000001"));
        assertNull(PermitReference.parse("HX-P-2018-187"));
        assertNull(PermitReference.parse(null));
    }

    /**
     * Numbers above five digits are refused.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNumberOutOfRange() {
        new PermitReference(2018, 100000);
    }
}
