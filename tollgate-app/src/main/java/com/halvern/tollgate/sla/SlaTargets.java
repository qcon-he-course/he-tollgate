package com.halvern.tollgate.sla;

import java.util.Map;

/**
 * Target business days from lodgement to decision for each track, as agreed with the Permit Office in 2022.
 *
 * <p>A case that has not been assessed yet has no track and is held to the Desk target. See ADR-0009.
 */
public final class SlaTargets {

    private static final String DEFAULT_TRACK = "DESK";

    private static final Map<String, Integer> TARGETS = Map.of(
            "EXPRESS", 2,
            "DESK", 5,
            "ENGINEER", 15,
            "PANEL", 25);

    private SlaTargets() {
    }

    public static int forTrack(String track) {
        return TARGETS.get(track == null ? DEFAULT_TRACK : track);
    }
}
