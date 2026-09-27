package com.halvern.tollgate.web;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.halvern.tollgate.determination.CaseService;

/**
 * CR-146 reassign an officer's cases, INC-40330 support tools.
 */
@Controller
public class SupportAdminController {

    @Autowired
    private CaseService caseService;

    @GetMapping("/admin/reassign")
    public String reassign(Model model) {
        model.addAttribute("workload", caseService.officersByWorkload());
        model.addAttribute("openByOfficer", caseService.openCasesByOfficer());
        return "admin/reassign";
    }

    @PostMapping("/admin/reassign")
    public String doReassign(@RequestParam String from, @RequestParam String to, Principal principal, RedirectAttributes redirect) {
        try {
            int n = caseService.reassignOfficer(from, to, principal.getName()).size();
            redirect.addFlashAttribute("message", n + " open case(s) moved from " + from + " to " + to + ".");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reassign";
    }

    @PostMapping("/admin/support/resync-status")
    public String resync(RedirectAttributes redirect) {
        redirect.addFlashAttribute("message", caseService.resyncLegacyStatuses() + " cases resynced.");
        return "redirect:/admin/reassign";
    }
}
