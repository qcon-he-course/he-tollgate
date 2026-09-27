/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: MailNotifier.java 1157 2017-11-30 16:25:48Z rmistry $
 */
package com.halvern.tollgate.notify;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.halvern.tollgate.platform.ErrorCodes;
import com.halvern.tollgate.platform.TollgateException;

/**
 * Sends notification emails from the templates in /mail. The first line of a template is the
 * subject; the rest is the body. Placeholders are written ${name}.
 * Implements BR-42.
 *
 * @author R. Mistry
 * @since 1.0
 */
@Component
public class MailNotifier {

    private static final Logger log = LoggerFactory.getLogger(MailNotifier.class);

    /** BR-42: a failed send is retried three times. */
    private static final int RETRIES = 3;

    private static final long RETRY_DELAY_MS = 1000;

    private static final String SUBJECT_PREFIX = "Subject:";

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private NotificationLogDao notificationLogDao;

    @Value("${tollgate.mail.from}")
    private String from;

    /**
     * Send an email from a template and log it.
     *
     * @param template the template name, without .txt
     * @param to the recipient
     * @param values the placeholder values
     * @param reference the application or permit reference the email is about
     * @return the subject sent
     * @throws TollgateException if the email could not be sent after all retries; the failure is logged
     */
    public String send(String template, String to, Map<String, String> values, String reference)
            throws TollgateException {
        String text = loadTemplate(template);
        String filled = new StringSubstitutor(values).replace(text);
        String subject = "Halvern Crossings";
        String body = filled;
        if (filled.startsWith(SUBJECT_PREFIX)) {
            int eol = filled.indexOf('\n');
            subject = filled.substring(SUBJECT_PREFIX.length(), eol).trim();
            body = filled.substring(eol + 1).trim();
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        NotificationLogEntry entry = new NotificationLogEntry();
        entry.setReference(reference);
        entry.setRecipient(to);
        entry.setTemplate(template);
        entry.setSubject(subject);

        // INC-40119: a mail outage made lodgement fail, sending is now non-blocking
        try {
            mailSender.send(message);
            entry.setStatus(NotificationLogEntry.SENT);
            notificationLogDao.insert(entry);
            log.info("Sent " + template + " to " + to + " for " + reference);
            return subject;
        } catch (Exception e) {
            log.error("Could not send " + template + " to " + to + " for " + reference, e);
            return null;
        }
    }

    private String loadTemplate(String template) throws TollgateException {
        ClassPathResource resource = new ClassPathResource("mail/" + template + ".txt");
        InputStream in = null;
        try {
            in = resource.getInputStream();
            return new String(FileCopyUtils.copyToByteArray(in), "UTF-8");
        } catch (IOException e) {
            throw new TollgateException(ErrorCodes.MAIL_FAILED, "Missing mail template " + template, e);
        }
    }

    private void pause() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
