package com.halvern.tollgate.platform.settings;

import java.security.Principal;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.halvern.tollgate.platform.flags.FlagService;

/**
 * CR-121 settings screen. Values take effect as soon as they are saved.
 */
@Controller
public class SettingsController {

    @Autowired
    private SettingsDao settingsDao;

    @Autowired
    private SettingsOverrideLoader settingsOverrideLoader;

    @Autowired
    private FlagService flagService;

    @GetMapping("/admin/settings")
    public String settings(Model model) {
        model.addAttribute("settings", settingsDao.findAll());
        model.addAttribute("flags", flagService.findAll());
        return "admin/settings";
    }

    @PostMapping("/admin/settings")
    public String save(@RequestParam Map<String, String> params, Principal principal, RedirectAttributes redirect) {
        int saved = 0;
        for (SystemSetting s : settingsDao.findAll()) {
            String value = params.get(s.getKey());
            if (value == null || value.trim().equals(s.getValue())) {
                continue;
            }
            if (!StringUtils.isNumeric(value.trim())) {
                redirect.addFlashAttribute("error", s.getDescription() + " must be a whole number");
                return "redirect:/admin/settings";
            }
            settingsDao.update(s.getKey(), value.trim(), principal.getName());
            settingsOverrideLoader.apply(s.getKey(), value.trim());
            saved++;
        }
        redirect.addFlashAttribute("message", saved + " setting(s) saved.");
        return "redirect:/admin/settings";
    }
}
