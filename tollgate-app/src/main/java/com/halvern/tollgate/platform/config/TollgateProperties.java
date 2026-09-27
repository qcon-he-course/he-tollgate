package com.halvern.tollgate.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Tollgate settings from application.yml (TG-230).
 *
 * @author Dana Kerr
 */
@Data
@Component
@ConfigurationProperties(prefix = "tollgate")
public class TollgateProperties {

    private Intake intake = new Intake();

    private Consultation consultation = new Consultation();

    @Data
    public static class Intake {

        /** Fewer business days' notice than this is short notice. */
        private int shortNoticeDays = 10;
    }

    @Data
    public static class Consultation {

        /** Business days the haulier has to answer a query round. */
        private int roundDays = 5;
    }
}
