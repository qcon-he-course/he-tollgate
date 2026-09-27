package com.halvern.tollgate.consultation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.halvern.tollgate.assessment.Tracks;
import com.halvern.tollgate.audit.AuditEntry;
import com.halvern.tollgate.determination.CaseFile;
import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.determination.CaseStates;
import com.halvern.tollgate.notify.MailNotifier;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.reference.UserDao;

import lombok.extern.slf4j.Slf4j;

/**
 * Lists Panel-track cases for panel sittings (TG-170).
 *
 * @author Dana Kerr
 */
@Slf4j
@Component
public class PanelScheduler {

    @Autowired
    private WorkingDayCalendar workingDayCalendar;

    @Autowired
    private PanelSittingRepository panelSittingRepository;

    @Autowired
    private PanelListingRepository panelListingRepository;

    @Autowired
    private EngineerAssessmentRepository engineerAssessmentRepository;

    @Autowired
    private QueryRoundRepository queryRoundRepository;

    @Autowired
    private CaseService caseService;

    @Autowired
    private MailNotifier mailNotifier;

    @Autowired
    private UserDao userDao;

    /**
     * The sitting a case ready at the given time is listed for.
     */
    public LocalDate sittingDateFor(LocalDateTime readyAt) {
        // Panel standing orders, para 4.2
        LocalDate tuesday = readyAt.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusDays(1);
        LocalDateTime cutOff = LocalDateTime.of(tuesday, LocalTime.of(17, 0));
        LocalDate thursday = tuesday.plusDays(2);
        if (readyAt.isAfter(cutOff)) {
            thursday = thursday.plusWeeks(1);
        }
        return workingDayCalendar.nextWorkingDay(thursday);
    }

    public boolean isReady(CaseFile caseFile) {
        boolean assessed = !engineerAssessmentRepository.findByCaseIdOrderByRecordedAtAsc(caseFile.getId()).isEmpty();
        boolean openRound = queryRoundRepository.findByCaseIdOrderByRoundNoAsc(caseFile.getId()).stream()
                .anyMatch(r -> QueryRound.OPEN.equals(r.getStatus()));
        return assessed && !openRound;
    }

    /**
     * List a Panel-track case in review for the next sitting it can make, if it is ready.
     */
    public PanelListing listIfReady(CaseFile caseFile) {
        if (!Tracks.PANEL.equals(caseFile.getTrack()) || !CaseStates.IN_REVIEW.equals(caseFile.getState())
                || !isReady(caseFile)) {
            return null;
        }
        return list(caseFile, sittingDateFor(LocalDateTime.now()));
    }

    public PanelListing list(CaseFile caseFile, LocalDate sittingDate) {
        PanelSitting sitting = panelSittingRepository.findBySittingDate(sittingDate).orElseGet(() -> {
            PanelSitting s = new PanelSitting();
            s.setSittingDate(sittingDate);
            s.setStatus(PanelSitting.SCHEDULED);
            return panelSittingRepository.save(s);
        });
        PanelListing listing = new PanelListing();
        listing.setPanelSittingId(sitting.getId());
        listing.setCaseId(caseFile.getId());
        listing.setListedAt(LocalDateTime.now());
        panelListingRepository.save(listing);
        if (!CaseStates.AWAITING_PANEL.equals(caseFile.getState())) {
            caseService.changeState(caseFile, CaseStates.AWAITING_PANEL, AuditEntry.SYSTEM, "Listed for panel " + sittingDate);
        }
        notifyChairs(caseFile, sittingDate);
        return listing;
    }

    private void notifyChairs(CaseFile caseFile, LocalDate sittingDate) {
        List<String> chairs = userDao.findPanelChairEmails();
        Map<String, String> values = new HashMap<>();
        values.put("reference", caseFile.getReference());
        values.put("sittingDate", sittingDate.toString());
        for (String email : chairs) {
            try {
                mailNotifier.send("case-listed-for-panel", email, values, caseFile.getReference());
            } catch (TollgateException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }
}
