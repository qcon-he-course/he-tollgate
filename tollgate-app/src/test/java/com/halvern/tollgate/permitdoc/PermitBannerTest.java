package com.halvern.tollgate.permitdoc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.halvern.tollgate.permitdoc.PermitDocumentModel.Condition;
import com.halvern.tollgate.permitdoc.PermitDocumentModel.Trip;

/**
 * The printed permit carries "ESCORT REQUIRED" if and only if the permit has an escort condition.
 */
class PermitBannerTest {

    private final PermitRenderer renderer = new PermitRenderer(templateEngine());

    @Test
    void aPermitWithAnEscortConditionCarriesTheBanner() {
        String html = renderer.renderHtml(permit("4.20", new Condition("ESCORT", "1 escort vehicle")));

        assertThat(html).contains("ESCORT REQUIRED");
    }

    @ParameterizedTest(name = "{0} m wide")
    @ValueSource(strings = { "3.50", "4.00" })
    void aPermitWithoutAnEscortConditionHasNoBannerWhateverTheWidth(String width) {
        String html = renderer.renderHtml(permit(width, new Condition("TIME_WINDOW", "Cross between 10:00 and 15:00")));

        assertThat(html).doesNotContain("ESCORT REQUIRED");
    }

    @Test
    void everyConditionIsPrinted() {
        String html = renderer.renderHtml(permit("4.20",
                new Condition("ESCORT", "1 escort vehicle"),
                new Condition("SPEED_LIMIT", "10 mph on NEB"),
                new Condition("DIVERSION", "Use the A38 via Tarn Cross")));

        assertThat(html).contains("ESCORT: 1 escort vehicle", "SPEED_LIMIT: 10 mph on NEB",
                "DIVERSION: Use the A38 via Tarn Cross");
    }

    @Test
    void thePermitIsIdentifiedByVersion() {
        assertThat(renderer.renderHtml(permit("3.50"))).contains("HX-P-2023-00042/2");
    }

    @Test
    void aDiversionWithoutTheRouteCannotBePrinted() {
        assertThatThrownBy(() -> renderer.render(permit("3.50", new Condition("DIVERSION", " "))))
                .isInstanceOf(PermitRenderingException.class)
                .hasMessageContaining("diversion");
    }

    @Test
    void theDocumentIsAPdf() {
        byte[] pdf = renderer.render(permit("4.20", new Condition("ESCORT", "1 escort vehicle")));

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    private static PermitDocumentModel permit(String width, Condition... conditions) {
        return new PermitDocumentModel(PermitVersionRef.of("HX-P-2023-00042", 2), "HX-A-2023-000101",
                "Marram Heavy Haul", "HX23 ABC (STGO_CAT2)", "TRANSFORMER: 60 t transformer", "60000 kg",
                "Width " + width + " m, length 22.00 m, height 4.00 m", "No", LocalDate.of(2023, 4, 3),
                LocalDate.of(2023, 4, 5), "R2: S01, NEB", List.of(new Trip(1, LocalDateTime.of(2023, 4, 3, 9, 0), null)),
                List.of(conditions), new java.math.BigDecimal(width));
    }

    private static SpringTemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.addDialect(new Java8TimeDialect());
        return engine;
    }
}
