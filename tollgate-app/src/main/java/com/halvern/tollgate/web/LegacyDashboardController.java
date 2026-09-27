package com.halvern.tollgate.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.halvern.tollgate.determination.CaseService;

/**
 * Officers' worklist of open cases (TG-206).
 *
 * @author Priya Venkat
 */
@Controller
public class LegacyDashboardController {

    @Autowired
    private CaseService caseService;

    @GetMapping("/dashboard-old")
    public String dashboard(@RequestParam(required = false) String track, @RequestParam(required = false) String state,
            @RequestParam(required = false) String officer, Model model) {
        model.addAttribute("cases", caseService.findOpenCases(track, state, officer));
        model.addAttribute("counts", caseService.countsByState());
        model.addAttribute("officers", caseService.officers());
        model.addAttribute("track", track);
        model.addAttribute("state", state);
        model.addAttribute("officer", officer);
        return "dashboard-old";
    }
}
