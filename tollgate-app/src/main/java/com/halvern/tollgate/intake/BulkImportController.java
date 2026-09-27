/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: BulkImportController.java 1164 2018-01-16 17:48:03Z koduya $
 */
package com.halvern.tollgate.intake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.halvern.tollgate.reference.LoadType;
import com.halvern.tollgate.reference.LoadTypeDao;
import com.halvern.tollgate.reference.Route;
import com.halvern.tollgate.reference.RouteDao;
import com.halvern.tollgate.reference.Vehicle;
import com.halvern.tollgate.reference.VehicleDao;

/**
 * Imports draft applications from the CSV export of the legacy permit spreadsheet, so the permit
 * office can move its open applications into Tollgate at go-live. One row per application with a
 * single trip. Imported drafts are checked by an officer and lodged in the normal way.
 * Implements BR-52.
 *
 * Columns: haulier_id, vehicle_registration, load_type, description, gross_mass_kg, width_m, length_m,
 * height_m, hazardous (Y/N), un_number, route, first_move_date, last_move_date, departure.
 *
 * @author K. Oduya
 * @since 1.0
 */
@Controller
@RequestMapping("/admin/import")
public class BulkImportController {

    private static final Logger log = LoggerFactory.getLogger(BulkImportController.class);

    private static final int COLUMNS = 14;

    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    private VehicleDao vehicleDao;

    @Autowired
    private LoadTypeDao loadTypeDao;

    @Autowired
    private RouteDao routeDao;

    /**
     * Show the import page.
     *
     * @return the view name
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show() {
        return "admin/import";
    }

    /**
     * Import a CSV file of draft applications.
     *
     * @param file the CSV file
     * @param principal the user importing
     * @param model the model
     * @return the view name
     */
    @RequestMapping(method = RequestMethod.POST)
    public String importFile(@RequestParam("file") MultipartFile file, Principal principal, Model model) {
        List<String> created = new ArrayList<String>();
        List<String> skipped = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (lineNo == 1 || StringUtils.isBlank(line)) {
                    continue;
                }
                String[] cols = line.split(",");
                if (cols.length != COLUMNS) {
                    skipped.add("Line " + lineNo + ": expected " + COLUMNS + " columns");
                    continue;
                }
                try {
                    MovementApplication app = toApplication(cols);
                    created.add(applicationManager.createDraft(app, principal.getName()));
                } catch (Exception e) {
                    log.warn("Import line " + lineNo + " skipped: " + e.getMessage());
                    skipped.add("Line " + lineNo + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Import failed", e);
            model.addAttribute("error", "The file could not be read.");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Could not close import file");
                }
            }
        }
        log.info("Import by " + principal.getName() + ": " + created.size() + " created, " + skipped.size()
                + " skipped");
        model.addAttribute("created", created);
        model.addAttribute("skipped", skipped);
        return "admin/import";
    }

    private MovementApplication toApplication(String[] cols) {
        MovementApplication app = new MovementApplication();
        app.setHaulierId(Long.valueOf(cols[0].trim()));
        Vehicle vehicle = vehicleDao.findByRegistration(cols[1].trim().toUpperCase());
        if (vehicle != null) {
            app.setVehicleId(vehicle.getId());
        }
        LoadType loadType = loadTypeDao.findByCode(cols[2].trim());
        if (loadType != null) {
            app.setLoadTypeId(loadType.getId());
        }
        app.setLoadDescription(StringUtils.trimToNull(cols[3]));
        app.setGrossMassKg(Integer.valueOf(cols[4].trim()));
        app.setWidthM(new BigDecimal(cols[5].trim()));
        app.setLengthM(new BigDecimal(cols[6].trim()));
        app.setHeightM(new BigDecimal(cols[7].trim()));
        app.setHazardous("Y".equalsIgnoreCase(cols[8].trim()));
        app.setUnNumber(StringUtils.trimToNull(cols[9]));
        Route route = routeDao.findByCode(cols[10].trim());
        if (route != null) {
            app.setRouteId(route.getId());
        }
        app.setFirstMoveDate(LegacyDateUtil.parseDate(cols[11]));
        app.setLastMoveDate(LegacyDateUtil.parseDate(cols[12]));

        List<ApplicationTrip> trips = new ArrayList<ApplicationTrip>();
        Date departure = LegacyDateUtil.parseDateTime(cols[13]);
        if (departure != null) {
            ApplicationTrip trip = new ApplicationTrip();
            trip.setTripNo(1);
            trip.setDepartureAt(departure);
            trips.add(trip);
        }
        app.setTrips(trips);
        return app;
    }
}
