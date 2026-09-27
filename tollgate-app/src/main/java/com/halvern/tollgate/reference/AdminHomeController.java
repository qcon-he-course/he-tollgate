/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: AdminHomeController.java 1052 2017-09-14 09:40:03Z koduya $
 */
package com.halvern.tollgate.reference;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Admin home page with links to each reference data screen.
 * Implements BR-40.
 *
 * @author K. Oduya
 * @since 1.0
 */
@Controller
public class AdminHomeController {

    /**
     * Index.
     *
     * @return the view name
     */
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String index() {
        return "admin/index";
    }
}
