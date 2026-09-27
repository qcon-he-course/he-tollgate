package com.halvern.tollgate.web;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.halvern.tollgate.consultation.PanelListing;
import com.halvern.tollgate.consultation.PanelListingRepository;
import com.halvern.tollgate.consultation.PanelSitting;
import com.halvern.tollgate.consultation.PanelSittingRepository;
import com.halvern.tollgate.determination.CaseFile;
import com.halvern.tollgate.determination.CaseFileRepository;

/**
 * Panel sittings and the cases listed for them (TG-174).
 *
 * @author Dana Kerr
 */
@Controller
public class PanelController {

    @Autowired
    private PanelSittingRepository panelSittingRepository;

    @Autowired
    private PanelListingRepository panelListingRepository;

    @Autowired
    private CaseFileRepository caseFileRepository;

    @GetMapping("/panel")
    public String panel(Model model) {
        Map<PanelSitting, List<CaseFile>> sittings = new LinkedHashMap<>();
        for (PanelSitting sitting : panelSittingRepository.findBySittingDateGreaterThanEqualOrderBySittingDateAsc(LocalDate.now().minusDays(7))) {
            List<CaseFile> cases = new ArrayList<>();
            for (PanelListing listing : panelListingRepository.findByPanelSittingId(sitting.getId())) {
                if (listing.getResult() == null) {
                    cases.add(caseFileRepository.findById(listing.getCaseId()).get());
                }
            }
            sittings.put(sitting, cases);
        }
        model.addAttribute("sittings", sittings);
        return "panel/index";
    }
}
