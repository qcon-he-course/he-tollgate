package com.halvern.tollgate.web;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.halvern.tollgate.determination.CaseService;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.TollgateException;

import lombok.extern.slf4j.Slf4j;

/**
 * CR-124 accept or return answers from the review page without reloading it (review.js).
 */
@Slf4j
@RestController
public class AnswerReviewController {

    @Autowired
    private CaseService caseService;

    @PostMapping("/cases/{reference}/answers/{caseQuestionId}/review")
    public Map<String, Object> review(@PathVariable String reference, @PathVariable long caseQuestionId,
            @RequestParam String action, @RequestParam(required = false) String note, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        try {
            if ("accept".equals(action)) {
                caseService.acceptAnswer(reference, caseQuestionId, principal.getName(), note);
                result.put("reviewStatus", "ACCEPTED");
            } else {
                caseService.returnAnswer(reference, caseQuestionId, principal.getName(), note);
                result.put("reviewStatus", "RETURNED");
            }
            result.put("ok", true);
            result.put("state", caseService.findByRef(reference).getState());
        } catch (BusinessException | TollgateException e) {
            result.put("ok", false);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            log.error("Review of answer " + caseQuestionId + " on " + reference + " failed", e);
            result.put("ok", false);
            result.put("message", "The review could not be saved. Please reload the page and try again.");
        }
        return result;
    }
}
