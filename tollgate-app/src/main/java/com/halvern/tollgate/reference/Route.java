/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: Route.java 941 2017-06-20 09:15:51Z gferrand $
 */
package com.halvern.tollgate.reference;

import java.util.List;

/**
 * A route across the network from an entry junction to an exit junction.
 * Implements BR-06.
 *
 * @author G. Ferrand
 * @since 1.0
 */
public class Route {

    private Long id;
    private String code;
    private String entryJunction;
    private String exitJunction;
    private String description;
    private List<RouteSegment> segments;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the code.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the code.
     *
     * @param code the code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Gets the entry junction.
     *
     * @return the entry junction
     */
    public String getEntryJunction() {
        return entryJunction;
    }

    /**
     * Sets the entry junction.
     *
     * @param entryJunction the entry junction
     */
    public void setEntryJunction(String entryJunction) {
        this.entryJunction = entryJunction;
    }

    /**
     * Gets the exit junction.
     *
     * @return the exit junction
     */
    public String getExitJunction() {
        return exitJunction;
    }

    /**
     * Sets the exit junction.
     *
     * @param exitJunction the exit junction
     */
    public void setExitJunction(String exitJunction) {
        this.exitJunction = exitJunction;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the segments.
     *
     * @return the segments
     */
    public List<RouteSegment> getSegments() {
        return segments;
    }

    /**
     * Sets the segments.
     *
     * @param segments the segments
     */
    public void setSegments(List<RouteSegment> segments) {
        this.segments = segments;
    }
}
