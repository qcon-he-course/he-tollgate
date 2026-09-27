/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: RouteSegment.java 941 2017-06-20 09:15:51Z gferrand $
 */
package com.halvern.tollgate.reference;

/**
 * One structure crossed on a route, in order.
 * Implements BR-06.
 *
 * @author G. Ferrand
 * @since 1.0
 */
public class RouteSegment {

    private Long id;
    private Long routeId;
    private int seq;
    private Structure structure;

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
     * Gets the route id.
     *
     * @return the route id
     */
    public Long getRouteId() {
        return routeId;
    }

    /**
     * Sets the route id.
     *
     * @param routeId the route id
     */
    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    /**
     * Gets the seq.
     *
     * @return the seq
     */
    public int getSeq() {
        return seq;
    }

    /**
     * Sets the seq.
     *
     * @param seq the seq
     */
    public void setSeq(int seq) {
        this.seq = seq;
    }

    /**
     * Gets the structure.
     *
     * @return the structure
     */
    public Structure getStructure() {
        return structure;
    }

    /**
     * Sets the structure.
     *
     * @param structure the structure
     */
    public void setStructure(Structure structure) {
        this.structure = structure;
    }
}
