package com.halvern.tollgate.web;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.halvern.tollgate.determination.CaseFile;
import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.determination.PermitCondition;
import com.halvern.tollgate.determination.PermitVersion;
import com.halvern.tollgate.determination.TimelineEntry;

/**
 * The case page (TG-205).
 *
 * @author Priya Venkat
 */
@Controller
public class CasePageController {

    @Autowired
    private CaseService caseService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TIMELINE_SQL = "select created_at as at, reference, old_state, new_state, actor, reason "
            + "from audit_entry where reference = ? and entity_type <> 'PERMIT' "
            + "union all "
            + "select (h.changed_at at time zone 'Europe/London') as at, h.permit_reference || '/' || h.version as reference, "
            + "h.old_status as old_state, h.new_status as new_state, h.changed_by as actor, null as reason "
            + "from permit_status_history h join permit p on p.id = h.permit_id where p.case_id = ? "
            + "order by at";

    @GetMapping("/cases/{reference}")
    public String view(@PathVariable String reference, Model model) {
        if (!caseService.exists(reference)) {
            return "redirect:/applications/" + reference;
        }
        CaseFile caseFile = caseService.findByRef(reference);
        PermitVersion permit = caseService.currentPermit(caseFile);
        model.addAttribute("caseFile", caseFile);
        model.addAttribute("app", caseService.getApplication(caseFile));
        model.addAttribute("haulierName", caseService.haulierName(caseFile));
        model.addAttribute("warnings", caseService.warningsOf(caseFile));
        model.addAttribute("assessment", caseService.currentAssessment(caseFile));
        model.addAttribute("triggers", caseService.currentTriggers(caseFile));
        model.addAttribute("engineerAssessment", caseService.latestEngineerAssessment(caseFile));
        model.addAttribute("openRound", caseService.openRound(caseFile));
        model.addAttribute("decision", caseService.latestDecision(caseFile));
        model.addAttribute("permit", permit);
        model.addAttribute("permitVersions", caseService.permitVersions(caseFile));
        model.addAttribute("conditions", permit == null ? null : caseService.conditionsFor(permit));
        model.addAttribute("amendments", caseService.amendments(caseFile));
        model.addAttribute("panelListings", caseService.panelListings(caseFile));
        // INC-40362 timeline was slow and missed amended permits, read it in one query
        model.addAttribute("timeline", jdbcTemplate.query(TIMELINE_SQL, (rs, i) -> new TimelineEntry(
                rs.getTimestamp("at").toLocalDateTime(), rs.getString("reference"), rs.getString("old_state"),
                rs.getString("new_state"), rs.getString("actor"), rs.getString("reason")), reference, caseFile.getId()));
        model.addAttribute("notifications", caseService.notifications(caseFile));
        model.addAttribute("engineers", caseService.engineers());
        model.addAttribute("officers", caseService.officers());
        model.addAttribute("conditionTypes", Arrays.asList(PermitCondition.ESCORT, PermitCondition.TIME_WINDOW,
                PermitCondition.SPEED_LIMIT, PermitCondition.DIVERSION, PermitCondition.NIGHT_BEACONS));
        model.addAttribute("canDecide", caseService.canDecide(caseFile));
        model.addAttribute("canRecordPanelDecision", caseService.canRecordPanelDecision(caseFile));
        model.addAttribute("canIssueQueryRound", caseService.canIssueQueryRound(caseFile));
        model.addAttribute("canEscalate", caseService.canEscalate(caseFile));
        model.addAttribute("canReassess", caseService.canReassess(caseFile));
        model.addAttribute("canRecordEngineerAssessment", caseService.canRecordEngineerAssessment(caseFile));
        model.addAttribute("canRevoke", caseService.canRevoke(caseFile));
        model.addAttribute("terminal", caseService.isTerminal(caseFile));
        // CR-178, INC-40412
        model.addAttribute("tasks", caseService.outstandingTasks(caseFile));
        model.addAttribute("duplicates", caseService.possibleDuplicates(caseFile));
        return "case/view";
    }
}
