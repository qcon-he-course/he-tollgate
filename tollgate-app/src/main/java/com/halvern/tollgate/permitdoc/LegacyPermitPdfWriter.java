/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: LegacyPermitPdfWriter.java 1176 2018-01-29 13:15:40Z gferrand $
 */
package com.halvern.tollgate.permitdoc;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.intake.ApplicationTrip;
import com.halvern.tollgate.intake.MovementApplication;
import com.halvern.tollgate.platform.ErrorCodes;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.reference.RouteSegment;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Writes the printed permit as a PDF.
 * Implements BR-36.
 *
 * @author G. Ferrand
 * @since 1.0
 */
@Component
public class LegacyPermitPdfWriter {

    private static final Logger log = LoggerFactory.getLogger(LegacyPermitPdfWriter.class);

    /** Content type of the printed permit. */
    public static final String CONTENT_TYPE = "application/pdf";

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font HEADING = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font BODY = FontFactory.getFont(FontFactory.HELVETICA, 10);

    /**
     * Write.
     *
     * @param reference the permit reference
     * @param version the permit version
     * @param app the application, with trips loaded
     * @param windowStart first move date of this version
     * @param windowEnd last move date of this version
     * @param segments the structures crossed on the route
     * @param conditions the permit conditions, one line each
     * @param escort whether the permit has an escort condition
     * @return the PDF
     * @throws TollgateException if the document cannot be produced
     */
    public byte[] write(PermitReference reference, int version, MovementApplication app, java.util.Date windowStart,
            java.util.Date windowEnd, List<RouteSegment> segments, List<String> conditions, boolean escort)
            throws TollgateException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("HALVERN CROSSINGS PLC\nABNORMAL LOAD MOVEMENT PERMIT", TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            if (escort) {
                Paragraph banner = new Paragraph("ESCORT REQUIRED", TITLE);
                banner.setAlignment(Element.ALIGN_CENTER);
                document.add(banner);
            }
            Paragraph ref = new Paragraph("Permit " + reference + " version " + version + "    Application "
                    + app.getReference(), HEADING);
            ref.setAlignment(Element.ALIGN_CENTER);
            ref.setSpacingAfter(12);
            document.add(ref);

            PdfPTable details = new PdfPTable(2);
            details.setWidthPercentage(100);
            addRow(details, "Haulier", app.getHaulierName());
            addRow(details, "Vehicle", app.getVehicleRegistration() + " (" + app.getVehicleCategory() + ")");
            addRow(details, "Load", app.getLoadTypeCode() + ": " + app.getLoadDescription());
            addRow(details, "Gross mass", app.getGrossMassKg() + " kg");
            addRow(details, "Dimensions", "Width " + app.getWidthM() + " m, length " + app.getLengthM()
                    + " m, height " + app.getHeightM() + " m");
            addRow(details, "Hazardous", app.isHazardous() ? "Yes, UN " + app.getUnNumber() : "No");
            addRow(details, "Movement dates", dateFormat.format(windowStart) + " to " + dateFormat.format(windowEnd));
            StringBuffer route = new StringBuffer(app.getRouteCode());
            route.append(": ");
            for (int i = 0; i < segments.size(); i++) {
                if (i > 0) {
                    route.append(", ");
                }
                route.append(segments.get(i).getStructure().getCode());
            }
            addRow(details, "Route", route.toString());
            document.add(details);

            document.add(new Paragraph("Trips", HEADING));
            PdfPTable trips = new PdfPTable(3);
            trips.setWidthPercentage(100);
            addRow(trips, "Trip", "Departure", "Estuary crossing");
            for (ApplicationTrip trip : app.getTrips()) {
                addRow(trips, String.valueOf(trip.getTripNo()), dateTimeFormat.format(trip.getDepartureAt()),
                        trip.getCrossingAt() == null ? "" : dateTimeFormat.format(trip.getCrossingAt()));
            }
            document.add(trips);

            document.add(new Paragraph("Conditions", HEADING));
            document.add(new Paragraph("The move must follow the route and times above.", BODY));
            for (int i = 0; i < conditions.size(); i++) {
                document.add(new Paragraph((i + 1) + ". " + conditions.get(i), BODY));
            }

            document.close();
        } catch (DocumentException e) {
            log.error("Could not write permit " + reference, e);
            throw new TollgateException(ErrorCodes.PERMIT_DOCUMENT_FAILED, "Could not produce the permit document", e);
        }
        return out.toByteArray();
    }

    private void addRow(PdfPTable table, String... cells) {
        for (int i = 0; i < cells.length; i++) {
            table.addCell(new Paragraph(cells[i] == null ? "" : cells[i], BODY));
        }
    }
}
