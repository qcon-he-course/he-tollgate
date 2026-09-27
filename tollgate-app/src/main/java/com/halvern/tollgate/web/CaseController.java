package com.halvern.tollgate.web;

import java.security.Principal;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.halvern.tollgate.consultation.EngineerAssessmentService;
import com.halvern.tollgate.consultation.QueryRoundService;
import com.halvern.tollgate.determination.AmendmentRequest;
import com.halvern.tollgate.determination.AmendmentService;
import com.halvern.tollgate.determination.CaseFile;
import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.determination.DecisionService;
import com.halvern.tollgate.determination.EscalationService;
import com.halvern.tollgate.determination.PanelDecisionService;
import com.halvern.tollgate.determination.PermitCondition;
import com.halvern.tollgate.determination.ReassessmentService;
import com.halvern.tollgate.platform.CurrentUser;
import com.halvern.tollgate.questionnaire.AnswerForm;
import com.halvern.tollgate.intake.CheckFailure;
import com.halvern.tollgate.intake.LodgementRefusedException;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.TollgateException;

import lombok.extern.slf4j.Slf4j;

/**
 * Case actions for staff.
 *
 * @author Priya Venkat
 */
@Slf4j
@Controller
public class CaseController {

    @Autowired
    private CaseService caseService;

    @Autowired
    private QueryRoundService queryRoundService;

    @Autowired
    private EngineerAssessmentService engineerAssessmentService;

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private PanelDecisionService panelDecisionService;

    @Autowired
    private EscalationService escalationService;

    @Autowired
    private ReassessmentService reassessmentService;

    @Autowired
    private AmendmentService amendmentService;

    @PostMapping("/cases/{reference}/lodge")
    public String lodge(@PathVariable String reference, Principal principal, RedirectAttributes redirect) {
        try {
            caseService.lodge(reference, principal.getName());
            redirect.addFlashAttribute("message", "Application " + reference + " lodged.");
        } catch (LodgementRefusedException e) {
            List<String> messages = new ArrayList<>();
            for (CheckFailure failure : e.getFailures()) {
                messages.add(failure.getMessage());
            }
            redirect.addFlashAttribute("failures", messages);
            return "redirect:/applications/" + reference;
        } catch (TollgateException | BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/applications/" + reference;
        }
        return "redirect:/cases/" + reference;
    }

    @PostMapping("/cases/{reference}/answers/{caseQuestionId}/accept")
    public String acceptAnswer(@PathVariable String reference, @PathVariable long caseQuestionId,
            @RequestParam(required = false) String note, Principal principal, RedirectAttributes redirect) {
        try {
            caseService.acceptAnswer(reference, caseQuestionId, principal.getName(), note);
            redirect.addFlashAttribute("message", "Answer accepted.");
        } catch (TollgateException | BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/applications/" + reference + "/review";
    }

    @PostMapping("/cases/{reference}/engineer")
    public String assignEngineer(@PathVariable String reference, @RequestParam String engineer, Principal principal,
            RedirectAttributes redirect) {
        try {
            engineerAssessmentService.assignEngineer(reference, engineer, principal.getName());
            redirect.addFlashAttribute("message", "Engineer " + engineer + " assigned.");
        } catch (TollgateException | BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    @PostMapping("/cases/{reference}/query-rounds")
    public String issueQueryRound(@PathVariable String reference, @RequestParam(required = false) List<String> questions,
            Principal principal, RedirectAttributes redirect) {
        try {
            queryRoundService.issueRound(reference, questions, principal.getName());
            redirect.addFlashAttribute("message", "Query round issued.");
        } catch (TollgateException | BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    @PostMapping("/cases/{reference}/query-rounds/{roundNo}/answers")
    public String answerQueryRound(@PathVariable String reference, @PathVariable int roundNo,
            @RequestParam(required = false) List<String> answers, Principal principal, RedirectAttributes redirect) {
        try {
            queryRoundService.answerRound(reference, roundNo, answers, principal.getName());
            redirect.addFlashAttribute("message", "Answers recorded.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    @PostMapping("/cases/{reference}/engineer-assessment")
    public String recordEngineerAssessment(@PathVariable String reference, @RequestParam(required = false) String recommendation,
            @RequestParam(required = false) String notes, @RequestParam(required = false) String proposedConditions,
            Principal principal, RedirectAttributes redirect) {
        try {
            engineerAssessmentService.record(reference, recommendation, notes, proposedConditions, principal.getName());
            redirect.addFlashAttribute("message", "Engineer assessment recorded.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    @PostMapping("/cases/{reference}/answers")
    public String reviseAnswers(@PathVariable String reference, @ModelAttribute("answerForm") AnswerForm form,
            Principal principal, RedirectAttributes redirect) {
        try {
            int saved = caseService.reviseAnswers(reference, form, principal.getName());
            redirect.addFlashAttribute("message", saved + " answers saved.");
        } catch (TollgateException | BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/applications/" + reference + "/questionnaire";
        }
        return "redirect:/applications/" + reference + "/review";
    }

    @PostMapping("/cases/{reference}/decision")
    public String decide(@PathVariable String reference, @RequestParam String outcome,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) List<String> conditionType,
            @RequestParam(required = false) List<String> conditionDetail, Principal principal, RedirectAttributes redirect) {
        try {
            // CR-147 refusals go through refuse()
            if ("REFUSE".equals(outcome)) {
                decisionService.refuse(reference, reason);
            } else {
                decisionService.decide(reference, "ISSUE".equals(outcome), reason, conditions(conditionType, conditionDetail),
                        principal.getName());
            }
            redirect.addFlashAttribute("message", "Decision recorded.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    @PostMapping("/cases/{reference}/panel-decision")
    public String panelDecision(@PathVariable String reference, @RequestParam String action,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) List<String> conditionType,
            @RequestParam(required = false) List<String> conditionDetail, Principal principal, RedirectAttributes redirect) {
        try {
            panelDecisionService.record(reference, action, reason, conditions(conditionType, conditionDetail),
                    principal.getName());
            redirect.addFlashAttribute("message", PanelDecisionService.DEFER.equals(action)
                    ? "Deferred to the next sitting." : "Panel decision recorded.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    @PostMapping("/cases/{reference}/escalate")
    public String escalate(@PathVariable String reference, @RequestParam(required = false) String reason,
            Principal principal, RedirectAttributes redirect) {
        if (!CurrentUser.hasRole("OFFICER")) {
            redirect.addFlashAttribute("error", "Only officers can escalate a case");
            return "redirect:/cases/" + reference;
        }
        try {
            CaseFile caseFile = escalationService.escalate(reference, principal.getName(), reason);
            redirect.addFlashAttribute("message", "Escalated to " + caseFile.getTrack() + ".");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    @PostMapping("/cases/{reference}/reassess")
    public String reassess(@PathVariable String reference, Principal principal, RedirectAttributes redirect) {
        try {
            CaseFile caseFile = reassessmentService.reassess(reference, principal.getName());
            redirect.addFlashAttribute("message", "Reassessed: track " + caseFile.getTrack() + ".");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    @PostMapping("/cases/{reference}/withdraw")
    public String withdraw(@PathVariable String reference, @RequestParam(required = false) String reason,
            Principal principal, RedirectAttributes redirect) {
        try {
            caseService.withdraw(reference, principal.getName(), reason);
            redirect.addFlashAttribute("message", "Case withdrawn.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    @PostMapping("/cases/{reference}/cancel")
    public String cancel(@PathVariable String reference, @RequestParam(required = false) String reason,
            Principal principal, RedirectAttributes redirect) {
        try {
            caseService.cancel(reference, reason, principal.getName());
            redirect.addFlashAttribute("message", "Case cancelled.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    /**
     * Revoke from the permit modal on the case page (AJAX).
     */
    @PostMapping("/cases/{reference}/revoke")
    @ResponseBody
    public Map<String, Object> revoke(@PathVariable String reference, @RequestParam(required = false) String reason,
            Principal principal) {
        Map<String, Object> result = new HashMap<>();
        try {
            caseService.revokePermit(reference, reason, principal.getName());
            result.put("ok", true);
            result.put("message", "Permit revoked.");
        } catch (BusinessException e) {
            result.put("ok", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/permits/{permitReference}/amendments")
    public String requestAmendment(@PathVariable String permitReference, @RequestParam String newFirstMoveDate,
            @RequestParam String newLastMoveDate, Principal principal, RedirectAttributes redirect) {
        try {
            AmendmentRequest req = amendmentService.request(permitReference, LocalDate.parse(newFirstMoveDate),
                    LocalDate.parse(newLastMoveDate), principal.getName());
            redirect.addFlashAttribute("message", "Amendment " + req.getId() + " requested.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/permits/" + permitReference;
    }

    @PostMapping("/amendments/{id}/approve")
    public String approveAmendment(@PathVariable Long id, @RequestParam String permitReference, Principal principal,
            RedirectAttributes redirect) {
        try {
            amendmentService.approve(id, principal.getName());
            redirect.addFlashAttribute("message", "Amendment approved.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/permits/" + permitReference;
    }

    @PostMapping("/amendments/{id}/reject")
    public String rejectAmendment(@PathVariable Long id, @RequestParam String permitReference,
            @RequestParam(required = false) String reason, Principal principal, RedirectAttributes redirect) {
        try {
            amendmentService.reject(id, reason, principal.getName());
            redirect.addFlashAttribute("message", "Amendment rejected.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/permits/" + permitReference;
    }

    @PostMapping("/cases/{reference}/officer")
    public String assignOfficer(@PathVariable String reference, @RequestParam String officer, Principal principal,
            RedirectAttributes redirect) {
        try {
            caseService.assignOfficer(reference, officer, principal.getName());
            redirect.addFlashAttribute("message", "Case assigned to " + officer + ".");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    @PostMapping("/cases/{reference}/comments")
    public String comments(@PathVariable String reference, @RequestParam(required = false) String comments,
            Principal principal, RedirectAttributes redirect) {
        try {
            caseService.updateComments(reference, comments, principal.getName());
            redirect.addFlashAttribute("message", "Comments saved.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    private List<PermitCondition> conditions(List<String> types, List<String> details) {
        List<PermitCondition> conditions = new ArrayList<>();
        if (types == null) {
            return conditions;
        }
        for (int i = 0; i < types.size(); i++) {
            if (types.get(i) == null || types.get(i).isEmpty()) {
                continue;
            }
            PermitCondition c = new PermitCondition();
            c.setConditionType(types.get(i));
            c.setDetail(details != null && i < details.size() ? details.get(i) : null);
            conditions.add(c);
        }
        return conditions;
    }

    @PostMapping("/cases/{reference}/answers/{caseQuestionId}/return")
    public String returnAnswer(@PathVariable String reference, @PathVariable long caseQuestionId,
            @RequestParam(required = false) String note, Principal principal, RedirectAttributes redirect) {
        try {
            caseService.returnAnswer(reference, caseQuestionId, principal.getName(), note);
            redirect.addFlashAttribute("message", "Answer returned to the haulier.");
        } catch (TollgateException | BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/applications/" + reference + "/review";
    }
}
