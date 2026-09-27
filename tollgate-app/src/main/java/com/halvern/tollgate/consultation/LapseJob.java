package com.halvern.tollgate.consultation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.audit.AuditEntry;
import com.halvern.tollgate.determination.CaseFile;
import com.halvern.tollgate.determination.CaseFileRepository;
import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.determination.CaseStates;
import com.halvern.tollgate.notify.MailNotifier;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.platform.config.TollgateProperties;
import com.halvern.tollgate.platform.flags.FlagService;
import com.halvern.tollgate.reference.HaulierDao;

import lombok.extern.slf4j.Slf4j;

/**
 * Nightly: recompute open query round deadlines, remind hauliers the day before, and lapse cases whose
 * deadline has passed (TG-155).
 *
 * @author Dana Kerr
 */
@Slf4j
@Component
public class LapseJob {

    @Autowired
    private QueryRoundRepository queryRoundRepository;

    @Autowired
    private WorkingDayCalendar workingDayCalendar;

    @Autowired
    private TollgateProperties tollgateProperties;

    @Autowired
    private CaseFileRepository caseFileRepository;

    @Autowired
    private CaseService caseService;

    @Autowired
    private MailNotifier mailNotifier;

    @Autowired
    private HaulierDao haulierDao;

    @Autowired
    private FlagService flagService;

    @Scheduled(cron = "0 15 0 * * *", zone = "Europe/London")
    @Transactional
    public void run() {
        LocalDate today = LocalDate.now();
        List<QueryRound> open = queryRoundRepository.findByStatus(QueryRound.OPEN);
        int reminded = 0;
        int lapsed = 0;
        for (QueryRound round : open) {
            // TG-156 holidays may have changed since the round was issued
            round.setDeadline(workingDayCalendar.addWorkingDays(round.getIssuedOn(),
                    tollgateProperties.getConsultation().getRoundDays()));
            CaseFile caseFile = caseFileRepository.findById(round.getCaseId()).get();
            // CR-133 optional grace day before lapsing
            LocalDate lapseAfter = flagService.isEnabled("consultation.lapse-grace-day") ? round.getDeadline().plusDays(1) : round.getDeadline();
            if (lapseAfter.isBefore(today)) {
                round.setStatus(QueryRound.LAPSED);
                queryRoundRepository.save(round);
                caseService.changeState(caseFile, CaseStates.LAPSED, AuditEntry.SYSTEM,
                        "No answer to query round " + round.getRoundNo() + " by " + round.getDeadline());
                send("case-lapsed", caseFile, round);
                lapsed++;
            } else {
                if (!round.isReminderSent() && today.equals(workingDayCalendar.previousWorkingDay(round.getDeadline()))) {
                    send("deadline-reminder", caseFile, round);
                    round.setReminderSent(true);
                    reminded++;
                }
                queryRoundRepository.save(round);
            }
        }
        log.info("Lapse run: {} open rounds, {} reminders, {} lapsed", open.size(), reminded, lapsed);
    }

    private void send(String template, CaseFile caseFile, QueryRound round) {
        Map<String, String> values = new HashMap<>();
        values.put("reference", caseFile.getReference());
        values.put("roundNo", String.valueOf(round.getRoundNo()));
        values.put("deadline", round.getDeadline().toString());
        try {
            mailNotifier.send(template, haulierDao.findById(caseFile.getHaulierId()).getContactEmail(), values,
                    caseFile.getReference());
        } catch (TollgateException e) {
            log.error("Could not send {} for {}", template, caseFile.getReference(), e);
        }
    }
}
