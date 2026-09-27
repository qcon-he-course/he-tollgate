package com.halvern.tollgate.web;

import java.security.Principal;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.platform.BusinessException;

/**
 * CR-144, CR-168, CR-180, CR-181, CR-187, INC-40250 case tools added for the permit office.
 */
@Controller
public class CaseToolsController {

    @Autowired
    private CaseService caseService;

    @GetMapping("/cases/search")
    public String search(@RequestParam(required = false) String q, @RequestParam(required = false) String state,
            @RequestParam(required = false) String track, @RequestParam(required = false) String from,
            @RequestParam(required = false) String to, Model model) {
        model.addAttribute("q", q);
        model.addAttribute("state", state);
        model.addAttribute("track", track);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        try {
            model.addAttribute("results", caseService.searchCases(q, state, track, caseService.parseReportDate(from),
                    caseService.parseReportDate(to)));
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "cases/search";
    }

    @GetMapping("/my-cases")
    public String myCases(Principal principal, Model model) {
        model.addAttribute("cases", caseService.myCases(principal.getName()));
        model.addAttribute("engineers", caseService.engineersByWorkload());
        return "cases/mine";
    }

    @PostMapping("/cases/assign-engineer")
    public String assignEngineer(@RequestParam String references, @RequestParam String engineer, Principal principal,
            RedirectAttributes redirect) {
        try {
            int n = caseService.assignEngineerToCases(Arrays.asList(references.split("[,\\s]+")), engineer, principal.getName()).size();
            redirect.addFlashAttribute("message", engineer + " assigned to " + n + " case(s).");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/my-cases";
    }

    @PostMapping("/cases/{reference}/resend-decision")
    public String resend(@PathVariable String reference, Principal principal, RedirectAttributes redirect) {
        try {
            caseService.resendDecisionEmail(reference, principal.getName());
            redirect.addFlashAttribute("message", "Decision email sent again.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    @PostMapping("/cases/{reference}/reprint")
    public String reprint(@PathVariable String reference, Principal principal, RedirectAttributes redirect) {
        try {
            caseService.reprintPermit(reference, principal.getName());
            redirect.addFlashAttribute("message", "Permit reprinted.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }

    @GetMapping("/cases/{reference}/text")
    public ResponseEntity<String> asText(@PathVariable String reference) {
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(caseService.caseAsText(reference));
    }
}
