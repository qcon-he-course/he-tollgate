/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: HomeController.java 1074 2017-09-28 16:30:18Z koduya $
 */
package com.halvern.tollgate.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Home page shown after signing in.
 *
 * @author K. Oduya
 * @since 1.0
 */
@Controller
public class HomeController {

    /**
     * Home.
     *
     * @return the view name
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        return "home";
    }
}
