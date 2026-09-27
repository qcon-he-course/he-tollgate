package com.halvern.tollgate.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.determination.PermitVersionRepository;

/**
 * Amendment requests waiting for an officer (TG-182).
 *
 * @author Priya Venkat
 */
@Controller
public class AmendmentController {

    @Autowired
    private CaseService caseService;

    @Autowired
    private PermitVersionRepository permitVersionRepository;

    @GetMapping("/amendments")
    public String pending(Model model) {
        model.addAttribute("amendments", caseService.pendingAmendments());
        model.addAttribute("permits", permitVersionRepository.findAll());
        return "amendments/index";
    }
}
