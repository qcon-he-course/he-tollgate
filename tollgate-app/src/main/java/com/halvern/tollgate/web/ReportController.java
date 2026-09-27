package com.halvern.tollgate.web;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.YearMonth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

/**
 * CR-141, CR-149, CR-155, CR-165, CR-169 reports for the permit office.
 */
@Controller
public class ReportController {

    @Autowired
    private CaseService caseService;

    @GetMapping("/reports")
    public String reports(@RequestParam(required = false) String month, Model model) {
        YearMonth ym;
        try {
            ym = month == null || month.isEmpty() ? YearMonth.now() : YearMonth.parse(month);
        } catch (Exception e) {
            ym = YearMonth.now();
        }
        model.addAttribute("kpis", caseService.monthlyKpis(ym));
        model.addAttribute("byTrack", caseService.countsByTrack());
        model.addAttribute("byOutcome", caseService.countsByOutcome());
        model.addAttribute("ages", caseService.ageBuckets());
        model.addAttribute("dueSoon", caseService.movesDueSoon(10));
        model.addAttribute("overdue", caseService.overdueRounds());
        model.addAttribute("stale", caseService.staleCases(10));
        model.addAttribute("activity", caseService.recentActivity(20));
        return "reports/index";
    }

    @GetMapping("/reports/cases.csv")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String state, @RequestParam(required = false) String track,
            @RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        String csv = caseService.exportCases(state, track, caseService.parseReportDate(from), caseService.parseReportDate(to));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tollgate-cases.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/reports/hauliers/{id}")
    public String haulier(@PathVariable Long id, Model model) {
        model.addAttribute("history", caseService.haulierHistory(id));
        return "reports/haulier";
    }

    @PostMapping("/reports/digest")
    public String digest(@RequestParam String to, Principal principal, RedirectAttributes redirect) {
        try {
            caseService.emailKpiDigest(to, principal.getName());
            redirect.addFlashAttribute("message", "Digest sent to " + to + ".");
        } catch (com.halvern.tollgate.platform.BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "The digest could not be sent.");
        }
        return "redirect:/reports";
    }
}
