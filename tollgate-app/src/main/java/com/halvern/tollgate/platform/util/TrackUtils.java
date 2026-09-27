package com.halvern.tollgate.platform.util;

/**
 * Track rules the screens need (TG-205).
 *
 * @author Tom Hollis
 */
public final class TrackUtils {

    private TrackUtils() {
    }

    public static String label(String track) {
        if (track == null) {
            return "Not assessed";
        }
        switch (track) {
            case "EXPRESS":
                return "Express";
            case "DESK":
                return "Desk";
            case "ENGINEER":
                return "Engineer";
            case "PANEL":
                return "Panel";
            default:
                return track;
        }
    }

    /** Engineer and Panel cases need an engineer assessment before a decision. */
    public static boolean needsEngineer(String track) {
        return "ENGINEER".equals(track) || "PANEL".equals(track);
    }

    /** Panel cases are decided by the panel chair, not an officer. */
    public static boolean decidedByPanel(String track) {
        return "PANEL".equals(track);
    }

    /** Query rounds are not used on Express. */
    public static boolean allowsQueryRounds(String track) {
        return "DESK".equals(track) || "ENGINEER".equals(track) || "PANEL".equals(track);
    }

    public static String badge(String track) {
        if ("PANEL".equals(track)) {
            return "label-danger";
        }
        if ("ENGINEER".equals(track)) {
            return "label-warning";
        }
        if ("DESK".equals(track)) {
            return "label-info";
        }
        return "label-default";
    }
}
