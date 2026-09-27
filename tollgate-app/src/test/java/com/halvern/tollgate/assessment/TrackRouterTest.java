package com.halvern.tollgate.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TrackRouterTest {

    private final TrackRouter router = new TrackRouter();

    @Test
    void scoreTracks() {
        assertThat(router.scoreTrack(0)).isEqualTo(Tracks.EXPRESS);
        assertThat(router.scoreTrack(19)).isEqualTo(Tracks.EXPRESS);
        assertThat(router.scoreTrack(20)).isEqualTo(Tracks.DESK);
        assertThat(router.scoreTrack(40)).isEqualTo(Tracks.ENGINEER);
        assertThat(router.scoreTrack(60)).isEqualTo(Tracks.PANEL);
    }

    @Test
    void higherTrackWins() {
        assertThat(router.higher(Tracks.EXPRESS, Tracks.ENGINEER)).isEqualTo(Tracks.ENGINEER);
        assertThat(router.higher(Tracks.PANEL, Tracks.DESK)).isEqualTo(Tracks.PANEL);
    }

    @Test
    void nextTrack() {
        assertThat(router.next(Tracks.DESK)).isEqualTo(Tracks.ENGINEER);
        assertThat(router.next(Tracks.PANEL)).isEqualTo(Tracks.PANEL);
    }
}
