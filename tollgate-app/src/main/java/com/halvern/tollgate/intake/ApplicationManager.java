/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: ApplicationManager.java 1158 2017-12-01 11:04:26Z rmistry $
 */
package com.halvern.tollgate.intake;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.audit.AuditService;
import com.halvern.tollgate.platform.ErrorCodes;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.questionnaire.QuestionnaireManager;
import com.halvern.tollgate.reference.Route;
import com.halvern.tollgate.reference.RouteDao;
import com.halvern.tollgate.reference.RouteSegment;

/**
 * Manages applications from draft to lodgement.
 * Implements BR-01, BR-09 to BR-13.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Service
public class ApplicationManager {

    private static final Logger log = LoggerFactory.getLogger(ApplicationManager.class);

    /** BR-13: attachment limits. */
    public static final int MAX_ATTACHMENTS = 5;
    public static final long MAX_ATTACHMENT_BYTES = 10L * 1024 * 1024;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private ApplicationReferenceGenerator referenceGenerator;

    @Autowired
    private AttachmentStore attachmentStore;

    @Autowired
    private PreCheckService preCheckService;

    @Autowired
    private ApplicationValidator applicationValidator;

    @Autowired
    private RouteDao routeDao;

    @Autowired
    private QuestionnaireManager questionnaireManager;

    @Autowired
    private AuditService auditService;


    /**
     * Find by reference.
     *
     * @param reference the application reference
     * @return the application
     * @throws TollgateException if not found
     */
    public MovementApplication findByReference(String reference) throws TollgateException {
        MovementApplication app = applicationDao.findByReference(reference);
        if (app == null) {
            throw new TollgateException(ErrorCodes.APPLICATION_NOT_FOUND, "No application " + reference);
        }
        return app;
    }

    /**
     * Create draft.
     *
     * @param app the application details
     * @param username the user creating the draft
     * @return the new application reference
     */
    @Transactional
    public String createDraft(MovementApplication app, String username) {
        app.setReference(referenceGenerator.nextReference());
        app.setStatus(MovementApplication.STATUS_DRAFT);
        app.setCreatedBy(username);
        long id = applicationDao.insert(app);
        List<ApplicationTrip> trips = app.getTrips();
        applicationDao.replaceTrips(id, trips == null ? new ArrayList<ApplicationTrip>() : trips);
        return app.getReference();
    }

    /**
     * Update draft.
     *
     * @param app the application details, with its id and reference
     * @throws TollgateException if the application is no longer a draft
     */
    @Transactional
    public void updateDraft(MovementApplication app) throws TollgateException {
        if (!applicationDao.update(app)) {
            throw new TollgateException(ErrorCodes.APPLICATION_NOT_DRAFT,
                    "Application " + app.getReference() + " has been lodged and cannot be changed");
        }
        List<ApplicationTrip> trips = app.getTrips();
        applicationDao.replaceTrips(app.getId().longValue(), trips == null ? new ArrayList<ApplicationTrip>() : trips);
    }

    /**
     * Add attachment.
     *
     * @param reference the application reference
     * @param fileName the original file name
     * @param type the attachment type
     * @param size the size in bytes
     * @param content the file content
     * @param username the user uploading
     * @throws TollgateException if the attachment is refused
     */
    @Transactional
    public void addAttachment(String reference, String fileName, String type, long size, InputStream content,
            String username) throws TollgateException {
        MovementApplication app = findByReference(reference);
        if (!app.isDraft()) {
            throw new TollgateException(ErrorCodes.APPLICATION_NOT_DRAFT,
                    "Attachments can only be added to a draft");
        }
        if (app.getAttachments().size() >= MAX_ATTACHMENTS) {
            throw new TollgateException(ErrorCodes.ATTACHMENT_LIMIT,
                    "An application can have at most " + MAX_ATTACHMENTS + " attachments");
        }
        if (size > MAX_ATTACHMENT_BYTES) {
            throw new TollgateException(ErrorCodes.ATTACHMENT_TOO_LARGE, "Attachments must be 10 MB or less");
        }
        if (!ArrayUtils.contains(Attachment.TYPES, type)) {
            throw new TollgateException(ErrorCodes.ATTACHMENT_NOT_PDF, "Choose an attachment type");
        }
        InputStream in = new BufferedInputStream(content);
        if (!StringUtils.endsWithIgnoreCase(fileName, ".pdf") || !isPdf(in)) {
            throw new TollgateException(ErrorCodes.ATTACHMENT_NOT_PDF, "Only PDF documents can be attached");
        }
        String path = attachmentStore.save(reference, in);
        Attachment attachment = new Attachment();
        attachment.setApplicationId(app.getId());
        attachment.setFileName(safeFileName(fileName));
        attachment.setAttachmentType(type);
        attachment.setSizeBytes(size);
        attachment.setStoragePath(path);
        attachment.setUploadedBy(username);
        applicationDao.insertAttachment(attachment);
    }

    /**
     * Lodge. Runs the lodgement checks; if any fail the application stays a draft.
     *
     * @param reference the application reference
     * @param username the user lodging the application
     * @throws TollgateException if the application is not a draft
     * @throws LodgementRefusedException if a check fails
     * @return the warnings to record on the case
     */
    @Transactional(rollbackFor = TollgateException.class)
    public List<String> lodge(String reference, String username) throws TollgateException {
        MovementApplication app = findByReference(reference);
        if (!app.isDraft()) {
            throw new TollgateException(ErrorCodes.APPLICATION_NOT_DRAFT,
                    "Application " + reference + " has already been lodged");
        }
        List<CheckFailure> failures = new ArrayList<CheckFailure>();
        List<String> warnings = new ArrayList<>();
        checkComplete(app, failures);
        if (failures.isEmpty()) {
            checkTrips(app, failures);
            // TG-231 pre-checks replace the separate licence and dimension checks
            PreCheckResult preChecks = preCheckService.check(app);
            failures.addAll(preChecks.getFailures());
            warnings = preChecks.getWarnings();
        }
        if (!failures.isEmpty()) {
            log.info("Lodgement of " + reference + " refused: " + failures);
            throw new LodgementRefusedException(reference, failures);
        }
        applicationValidator.validate(app);
        applicationDao.markLodged(app.getId().longValue(), username);
        // BR-22: the application gets its own copy of the current questions
        questionnaireManager.copyQuestions(app.getId().longValue(), app.getLoadTypeId().longValue());
        auditService.recordApplication(reference, MovementApplication.STATUS_DRAFT,
                MovementApplication.STATUS_SUBMITTED, username, null);
        log.info("Application " + reference + " lodged by " + username);
        return warnings;
    }

    private void checkComplete(MovementApplication app, List<CheckFailure> failures) {
        List<String> missing = new ArrayList<String>();
        if (app.getVehicleId() == null) {
            missing.add("vehicle");
        }
        if (app.getLoadTypeId() == null) {
            missing.add("load type");
        }
        if (StringUtils.isBlank(app.getLoadDescription())) {
            missing.add("load description");
        }
        if (app.getGrossMassKg() == null) {
            missing.add("gross mass");
        }
        if (app.getWidthM() == null || app.getLengthM() == null || app.getHeightM() == null) {
            missing.add("dimensions");
        }
        if (app.isHazardous() && StringUtils.isBlank(app.getUnNumber())) {
            missing.add("UN number");
        }
        if (app.getRouteId() == null) {
            missing.add("route");
        }
        if (app.getFirstMoveDate() == null || app.getLastMoveDate() == null) {
            missing.add("movement dates");
        }
        if (app.getTrips() == null || app.getTrips().isEmpty()) {
            missing.add("at least one trip");
        }
        if (!missing.isEmpty()) {
            failures.add(new CheckFailure(ErrorCodes.APPLICATION_INCOMPLETE,
                    "The application is incomplete: " + StringUtils.join(missing, ", ")));
        }
    }

    private void checkTrips(MovementApplication app, List<CheckFailure> failures) {
        Route route = routeDao.findById(app.getRouteId().longValue());
        boolean estuary = false;
        for (RouteSegment segment : route.getSegments()) {
            if (segment.getStructure().isEstuary()) {
                estuary = true;
            }
        }
        Date windowStart = app.getFirstMoveDate();
        Date windowEnd = endOfDay(app.getLastMoveDate());
        for (ApplicationTrip trip : app.getTrips()) {
            if (trip.getDepartureAt().before(windowStart) || trip.getDepartureAt().after(windowEnd)) {
                failures.add(new CheckFailure(ErrorCodes.TRIP_OUTSIDE_WINDOW,
                        "Trip " + trip.getTripNo() + " departs outside the movement dates"));
            }
            // BR-10: crossing time is needed for the estuary bridges
            if (estuary && trip.getCrossingAt() == null) {
                failures.add(new CheckFailure(ErrorCodes.CROSSING_TIME_MISSING,
                        "Trip " + trip.getTripNo() + " needs a planned estuary crossing time for route "
                        + app.getRouteCode()));
            }
        }
    }

    /**
     * Keeps only the file name part (some browsers send the full client path) and replaces any
     * character that is unsafe in a download header with an underscore.
     */
    private String safeFileName(String fileName) {
        String name = StringUtils.defaultString(fileName);
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        StringBuffer safe = new StringBuffer();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '.' || c == '_' || c == '-' || c == ' ') {
                safe.append(c);
            } else {
                safe.append('_');
            }
        }
        return safe.length() == 0 ? "attachment.pdf" : safe.toString();
    }

    private boolean isPdf(InputStream in) {
        try {
            in.mark(8);
            byte[] header = new byte[4];
            int read = in.read(header);
            in.reset();
            return read == 4 && header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F';
        } catch (IOException e) {
            log.warn("Could not read attachment header: " + e.getMessage());
            return false;
        }
    }

    private Date endOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}
