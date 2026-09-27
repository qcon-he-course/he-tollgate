package com.halvern.tollgate.assessment;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Maps scores to tracks and compares tracks.
 */
@Component
public class TrackRouter {

    private static final List<String> ORDER = Arrays.asList(Tracks.EXPRESS, Tracks.DESK, Tracks.ENGINEER, Tracks.PANEL);

    public String scoreTrack(int score) {
        // TG-213 track boundaries
        if (score >= 60) {
            return Tracks.PANEL;
        }
        if (score >= 40) {
            return Tracks.ENGINEER;
        }
        if (score >= 20) {
            return Tracks.DESK;
        }
        return Tracks.EXPRESS;
    }

    public String higher(String a, String b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return rank(a) >= rank(b) ? a : b;
    }

    public String next(String track) {
        int i = rank(track);
        return i < ORDER.size() - 1 ? ORDER.get(i + 1) : track;
    }

    public int rank(String track) {
        return ORDER.indexOf(track);
    }
}
