package com.halvern.tollgate.intake;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.notify.MailNotifier;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.reference.HaulierDao;

import lombok.extern.slf4j.Slf4j;

/**
 * Validates an application at lodgement (TG-233).
 *
 * @author Dana Kerr
 */
@Slf4j
@Component
public class ApplicationValidator {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private HaulierDao haulierDao;

    @Autowired
    private MailNotifier mailNotifier;

    public void validate(MovementApplication app) throws TollgateException {
        if (app.getTrips() == null || app.getTrips().size() > ApplicationForm.MAX_TRIPS) {
            throw new BusinessException("TOO_MANY_TRIPS", "An application can have at most 10 trips");
        }
        if (app.isHazardous() && (app.getUnNumber() == null || !app.getUnNumber().matches("\\d{4}"))) {
            throw new BusinessException("UN_NUMBER_REQUIRED", "A hazardous load needs a four digit UN number");
        }

        // TG-241 keep the draft as lodged
        applicationDao.update(app);

        Map<String, String> values = new HashMap<>();
        values.put("reference", app.getReference());
        values.put("haulierName", app.getHaulierName());
        values.put("routeCode", app.getRouteCode());
        values.put("firstMoveDate", LegacyDateUtil.formatDate(app.getFirstMoveDate()));
        values.put("lastMoveDate", LegacyDateUtil.formatDate(app.getLastMoveDate()));
        mailNotifier.send("application-lodged", haulierDao.findById(app.getHaulierId()).getContactEmail(), values,
                app.getReference());
        log.info("Application {} validated", app.getReference());
    }
}
