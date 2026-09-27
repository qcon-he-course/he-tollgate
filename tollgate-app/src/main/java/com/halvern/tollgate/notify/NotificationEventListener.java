package com.halvern.tollgate.notify;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.platform.events.CaseDecided;
import com.halvern.tollgate.platform.events.PermitStatusChanged;
import com.halvern.tollgate.reference.HaulierDao;

/**
 * Emails the haulier when something they need to know about happens to their case or permit.
 *
 * <p>See ADR-0006. Notifications are moving here from the services that used to call {@link MailNotifier} directly.
 * Listeners run in the publisher's transaction, so a mail that cannot be sent still fails the action, as it did
 * before.
 */
@Component
public class NotificationEventListener {

    private final MailNotifier mailNotifier;
    private final HaulierDao haulierDao;

    public NotificationEventListener(MailNotifier mailNotifier, HaulierDao haulierDao) {
        this.mailNotifier = mailNotifier;
        this.haulierDao = haulierDao;
    }

    @EventListener
    public void on(CaseDecided event) {
        Map<String, String> values = new HashMap<>();
        values.put("reference", event.reference());
        values.put("haulierName", event.haulierName());
        values.put("routeCode", event.routeCode());
        String template;
        if (event.isRefused()) {
            template = "case-refused";
            values.put("reason", event.reason());
        } else {
            template = "application-approved";
            values.put("permitReference", event.permitReference());
            values.put("firstMoveDate", event.windowStart().toString());
            values.put("lastMoveDate", event.windowEnd().toString());
        }
        send(template, event.haulierId(), values, event.reference());
    }

    @EventListener
    public void on(PermitStatusChanged event) {
        // Suspension and reinstatement mails still come from ClosureManager, which has the closure details
        if (!"REVOKED".equals(event.newStatus())) {
            return;
        }
        Map<String, String> values = new HashMap<>();
        values.put("permitReference", event.permitReference());
        values.put("reason", event.reason());
        send("permit-revoked", event.haulierId(), values, event.permitReference());
    }

    private void send(String template, Long haulierId, Map<String, String> values, String reference) {
        try {
            mailNotifier.send(template, haulierDao.findById(haulierId).getContactEmail(), values, reference);
        } catch (TollgateException e) {
            throw new BusinessException("MAIL_FAILED", e.getMessage());
        }
    }
}
