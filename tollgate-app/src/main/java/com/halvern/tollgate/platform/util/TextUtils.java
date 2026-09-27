package com.halvern.tollgate.platform.util;

import org.springframework.stereotype.Component;

/**
 * CR-127 text helpers for templates. Registered as a bean because Thymeleaf does not allow T() in th:utext,
 * use ${@textUtils.nl2br(...)}.
 */
@Component("textUtils")
public class TextUtils {

    /**
     * Keep the haulier's line breaks when showing their text. Use with th:utext.
     */
    public static String nl2br(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n").replace("\n", "<br/>");
    }
}
