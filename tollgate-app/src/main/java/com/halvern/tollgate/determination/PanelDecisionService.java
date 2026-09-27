package com.halvern.tollgate.determination;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halvern.tollgate.consultation.PanelListing;
import com.halvern.tollgate.consultation.PanelListingRepository;
import com.halvern.tollgate.consultation.PanelScheduler;
import com.halvern.tollgate.consultation.PanelSitting;
import com.halvern.tollgate.consultation.PanelSittingRepository;
import com.halvern.tollgate.consultation.WorkingDayCalendar;
import com.halvern.tollgate.platform.BusinessException;

import lombok.extern.slf4j.Slf4j;

/**
 * Panel chair decisions at a sitting: issue, refuse or defer to the next sitting (TG-173).
 *
 * @author Dana Kerr
 */
@Slf4j
@Service
public class PanelDecisionService {

    public static final String ISSUE = "ISSUE";
    public static final String REFUSE = "REFUSE";
    public static final String DEFER = "DEFER";

    private static final String IS_PANEL_CHAIR = "SELECT is_panel_chair FROM app_user WHERE username = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CaseService caseService;

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private PanelListingRepository panelListingRepository;

    @Autowired
    private PanelSittingRepository panelSittingRepository;

    @Autowired
    private PanelScheduler panelScheduler;

    @Autowired
    private WorkingDayCalendar workingDayCalendar;

    @Transactional
    public Decision record(String reference, String action, String reason, List<PermitCondition> added, String username) {
        Boolean chair = jdbcTemplate.queryForObject(IS_PANEL_CHAIR, Boolean.class, username);
        if (chair == null || !chair) {
            throw new BusinessException("NOT_PANEL_CHAIR", "Only a panel chair can record a panel decision");
        }
        CaseFile caseFile = caseService.findByRef(reference);
        if (!CaseStates.AWAITING_PANEL.equals(caseFile.getState())) {
            throw new BusinessException("NOT_LISTED", "Case " + reference + " is not awaiting the panel");
        }
        PanelListing listing = currentListing(caseFile);
        if (DEFER.equals(action)) {
            defer(caseFile, listing);
            return null;
        }
        if (!ISSUE.equals(action) && !REFUSE.equals(action)) {
            throw new BusinessException("BAD_ACTION", "Choose issue, refuse or defer");
        }
        listing.setResult(PanelListing.DECIDED);
        panelListingRepository.save(listing);
        return decisionService.decideForPanel(caseFile, ISSUE.equals(action), reason, added, username);
    }

    private void defer(CaseFile caseFile, PanelListing listing) {
        listing.setResult(PanelListing.DEFERRED);
        panelListingRepository.save(listing);
        PanelSitting sitting = panelSittingRepository.findById(listing.getPanelSittingId()).get();
        panelScheduler.list(caseFile, workingDayCalendar.nextWorkingDay(sitting.getSittingDate().plusWeeks(1)));
        log.info("Case {} deferred from {} to the next sitting", caseFile.getReference(), sitting.getSittingDate());
    }

    private PanelListing currentListing(CaseFile caseFile) {
        return panelListingRepository.findByCaseId(caseFile.getId()).stream()
                .filter(l -> l.getResult() == null)
                .reduce((a, b) -> b)
                .get();
    }
}
