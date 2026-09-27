package com.halvern.tollgate.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.halvern.tollgate.determination.CaseFile;
import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.platform.flags.FlagService;
import com.halvern.tollgate.sla.dashboard.DashboardFilter;
import com.halvern.tollgate.sla.dashboard.DashboardRow;
import com.halvern.tollgate.sla.dashboard.SlaDashboardQuery;

/**
 * Officers' dashboard: open cases by SLA status (ADR-0009).
 */
@Controller
public class DashboardController {

    private final SlaDashboardQuery dashboardQuery;
    private final CaseService caseService;

    @Autowired
    private FlagService flagService;

    public DashboardController(SlaDashboardQuery dashboardQuery, CaseService caseService) {
        this.dashboardQuery = dashboardQuery;
        this.caseService = caseService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String track,
            @RequestParam(required = false) String status, @RequestParam(required = false) String officer,
            Model model) {
        if (!flagService.isEnabled("dashboard.v2")) {
            return "redirect:/dashboard-old";
        }
        DashboardFilter filter = new DashboardFilter(track, status, officer);
        List<DashboardRow> rows = dashboardQuery.openCases(filter);
        model.addAttribute("rows", rows);
        // CR-150 route and engineer on each row
        Map<String, String> routes = new HashMap<>();
        Map<String, String> engineers = new HashMap<>();
        for (DashboardRow row : rows) {
            CaseFile caseFile = caseService.findByRef(row.reference());
            routes.put(row.reference(), caseService.getApplication(caseFile).getRouteCode());
            engineers.put(row.reference(), caseFile.getAssignedEngineer());
        }
        model.addAttribute("routes", routes);
        model.addAttribute("engineers", engineers);
        model.addAttribute("filter", filter);
        model.addAttribute("officers", caseService.officers());
        model.addAttribute("red", rows.stream().filter(r -> "RED".equals(r.slaStatus())).count());
        model.addAttribute("amber", rows.stream().filter(r -> "AMBER".equals(r.slaStatus())).count());
        model.addAttribute("green", rows.stream().filter(r -> "GREEN".equals(r.slaStatus())).count());
        return "dashboard";
    }
}
