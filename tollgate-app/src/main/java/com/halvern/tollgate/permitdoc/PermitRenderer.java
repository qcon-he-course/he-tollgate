package com.halvern.tollgate.permitdoc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

/**
 * Prints permits from the {@code permit/document} template (ADR-0010).
 *
 * <p>The layout lives in HTML so that wording changes do not need code changes. Replaces the iText writer, which is
 * kept for reprints of permits issued before 2023.
 */
@Component
public class PermitRenderer {

    public static final String CONTENT_TYPE = "application/pdf";
    private static final String TEMPLATE = "permit/document";

    private final ITemplateEngine templateEngine;

    public PermitRenderer(ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] render(PermitDocumentModel model) {
        String html = renderHtml(model);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException | RuntimeException e) {
            throw new PermitRenderingException("Could not print permit " + model.versionRef(), e);
        }
    }

    public String renderHtml(PermitDocumentModel model) {
        checkPrintable(model);
        Context context = new Context();
        context.setVariable("permit", model);
        return templateEngine.process(TEMPLATE, context);
    }

    /** A diversion is useless to the driver without the route to take. */
    private void checkPrintable(PermitDocumentModel model) {
        for (PermitDocumentModel.Condition condition : model.conditions()) {
            if (PermitDocumentModel.DIVERSION.equals(condition.type())
                    && (condition.detail() == null || condition.detail().isBlank())) {
                throw new PermitRenderingException("Permit " + model.versionRef()
                        + " has a diversion condition without the diversion route");
            }
        }
    }
}
