package com.halvern.tollgate.web;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.halvern.tollgate.determination.PermitStatusService;
import com.halvern.tollgate.platform.BusinessException;

/**
 * CR-122 permit actions posted from the case page.
 */
@Controller
public class PermitActionController {

    @Autowired
    private PermitStatusService permitStatusService;

    @PostMapping("/cases/{reference}/permit/revoke")
    public String revoke(@PathVariable String reference, @RequestParam(required = false) String reason,
            Principal principal, RedirectAttributes redirect) {
        try {
            permitStatusService.revoke(reference, reason, principal.getName());
            redirect.addFlashAttribute("message", "Permit revoked.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cases/" + reference;
    }
}
