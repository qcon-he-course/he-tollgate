package com.halvern.tollgate.platform.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.platform.config.TollgateProperties;

/**
 * CR-121: values saved on the settings screen override application.yml.
 * Copies system_setting into TollgateProperties once the application has started, and again whenever an admin saves.
 */
@Component
public class SettingsOverrideLoader {

    private static final Logger log = LoggerFactory.getLogger(SettingsOverrideLoader.class);

    @Autowired
    private SettingsDao settingsDao;

    @Autowired
    private TollgateProperties tollgateProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        try {
            for (SystemSetting s : settingsDao.findAll()) {
                apply(s.getKey(), s.getValue());
            }
            log.info("Settings loaded: short notice {} days, query rounds {} days",
                    tollgateProperties.getIntake().getShortNoticeDays(), tollgateProperties.getConsultation().getRoundDays());
        } catch (Exception e) {
            log.error("Could not load settings, using application.yml values", e);
        }
    }

    public void apply(String key, String value) {
        if ("intake.short-notice-days".equals(key)) {
            tollgateProperties.getIntake().setShortNoticeDays(Integer.parseInt(value));
        } else if ("consultation.round-days".equals(key)) {
            tollgateProperties.getConsultation().setRoundDays(Integer.parseInt(value));
        }
    }
}
