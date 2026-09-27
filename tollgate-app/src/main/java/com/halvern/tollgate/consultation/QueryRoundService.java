package com.halvern.tollgate.consultation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.assessment.Tracks;
import com.halvern.tollgate.determination.CaseFile;
import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.determination.CaseStates;
import com.halvern.tollgate.notify.MailNotifier;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.CurrentUser;
import com.halvern.tollgate.platform.TollgateException;
import com.halvern.tollgate.platform.config.TollgateProperties;
import com.halvern.tollgate.reference.HaulierDao;

import lombok.extern.slf4j.Slf4j;

/**
 * Query rounds: questions to the haulier with a deadline (TG-152).
 *
 * @author Dana Kerr
 */
@Slf4j
@Service
public class QueryRoundService {

    private static final int MAX_ROUNDS = 3;

    @Autowired
    private QueryRoundRepository queryRoundRepository;

    @Autowired
    private WorkingDayCalendar workingDayCalendar;

    @Autowired
    private TollgateProperties tollgateProperties;

    @Autowired
    private CaseService caseService;

    @Autowired
    private PanelScheduler panelScheduler;

    @Autowired
    private MailNotifier mailNotifier;

    @Autowired
    private HaulierDao haulierDao;

    @Transactional(rollbackFor = TollgateException.class)
    public QueryRound issueRound(String reference, List<String> questions, String username) throws TollgateException {
        if (!CurrentUser.hasRole("OFFICER") && !CurrentUser.hasRole("ENGINEER")) {
            throw new BusinessException("NOT_ALLOWED", "Only officers and engineers can issue query rounds");
        }
        CaseFile caseFile = caseService.findByRef(reference);
        if (!CaseStates.IN_REVIEW.equals(caseFile.getState())) {
            throw new BusinessException("NOT_IN_REVIEW", "Query rounds can only be issued on a case in review");
        }
        if (!Arrays.asList(Tracks.DESK, Tracks.ENGINEER, Tracks.PANEL).contains(caseFile.getTrack())) {
            throw new BusinessException("WRONG_TRACK", "Query rounds are not used on the " + caseFile.getTrack() + " track");
        }
        List<QueryRound> rounds = queryRoundRepository.findByCaseIdOrderByRoundNoAsc(caseFile.getId());
        if (rounds.size() >= MAX_ROUNDS) {
            throw new BusinessException("MAX_ROUNDS", "A case can have at most " + MAX_ROUNDS + " query rounds");
        }
        if (questions == null || questions.stream().allMatch(q -> q == null || q.trim().isEmpty())) {
            throw new BusinessException("NO_QUESTIONS", "Enter at least one question");
        }

        LocalDate today = LocalDate.now();
        QueryRound round = new QueryRound();
        round.setCaseId(caseFile.getId());
        round.setRoundNo(rounds.size() + 1);
        round.setIssuedBy(username);
        round.setIssuedOn(today);
        round.setDeadline(workingDayCalendar.addWorkingDays(today, tollgateProperties.getConsultation().getRoundDays()));
        round.setStatus(QueryRound.OPEN);
        int seq = 1;
        StringBuilder text = new StringBuilder();
        for (String q : questions) {
            if (q == null || q.trim().isEmpty()) {
                continue;
            }
            QueryQuestion question = new QueryQuestion();
            question.setSeq(seq++);
            question.setQuestionText(q.trim());
            round.getQuestions().add(question);
            text.append(question.getSeq()).append(". ").append(question.getQuestionText()).append("\n");
        }
        queryRoundRepository.save(round);
        caseService.changeState(caseFile, CaseStates.AWAITING_HAULIER, username, "Query round " + round.getRoundNo() + " issued");
        caseService.queryRoundIssued(caseFile, round);

        Map<String, String> values = new HashMap<>();
        values.put("reference", reference);
        values.put("roundNo", String.valueOf(round.getRoundNo()));
        values.put("deadline", round.getDeadline().toString());
        values.put("questions", text.toString().trim());
        mailNotifier.send("query-round-issued", haulierDao.findById(caseFile.getHaulierId()).getContactEmail(), values, reference);
        log.info("Query round {} issued on {} due {}", round.getRoundNo(), reference, round.getDeadline());
        return round;
    }

    /**
     * Record the haulier's answers. Hauliers send answers by email; officers enter them.
     */
    @Transactional
    public QueryRound answerRound(String reference, int roundNo, List<String> answers, String username) {
        CaseFile caseFile = caseService.findByRef(reference);
        QueryRound round = queryRoundRepository.findByCaseIdOrderByRoundNoAsc(caseFile.getId()).stream()
                .filter(r -> r.getRoundNo() == roundNo).findFirst().get();
        if (!QueryRound.OPEN.equals(round.getStatus())) {
            throw new BusinessException("ROUND_CLOSED", "Query round " + roundNo + " is not open");
        }
        for (int i = 0; i < round.getQuestions().size() && answers != null && i < answers.size(); i++) {
            round.getQuestions().get(i).setAnswerText(answers.get(i));
        }
        round.setStatus(QueryRound.ANSWERED);
        round.setAnsweredAt(LocalDateTime.now());
        queryRoundRepository.save(round);
        caseService.queryRoundAnswered(caseFile, round);
        if (CaseStates.AWAITING_HAULIER.equals(caseFile.getState())) {
            caseService.changeState(caseFile, CaseStates.IN_REVIEW, username, "Query round " + roundNo + " answered");
            panelScheduler.listIfReady(caseFile);
        }
        return round;
    }

    /**
     * Close open rounds when a case is withdrawn or cancelled.
     */
    public void closeOpenRounds(CaseFile caseFile) {
        for (QueryRound round : queryRoundRepository.findByCaseIdOrderByRoundNoAsc(caseFile.getId())) {
            if (QueryRound.OPEN.equals(round.getStatus())) {
                round.setStatus(QueryRound.CLOSED);
                queryRoundRepository.save(round);
            }
        }
    }

    public List<QueryRound> findRounds(Long caseId) {
        return queryRoundRepository.findByCaseIdOrderByRoundNoAsc(caseId);
    }
}
