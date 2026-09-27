/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: TollgateApplication.java 912 2017-06-02 10:14:37Z gferrand $
 */
package com.halvern.tollgate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Tollgate application entry point.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@SpringBootApplication
@EnableScheduling
public class TollgateApplication {

    /**
     * Main.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(TollgateApplication.class, args);
    }
}
