package com.createicbm.content.missile.tracker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regression tests for far-away launches and simulated-flight hand-off. */
class MissileChunkLoaderPolicyTest
{
    private static final double EXIT_HEIGHT = 500.0;

    @Test
    void keepsPathTickingBeforeSimulationExitHeightEvenWithoutPlayers()
    {
        assertTrue(MissileChunkLoaderPolicy.shouldKeepArcFlightTickets(false, false, true, true, EXIT_HEIGHT - 1, EXIT_HEIGHT));
    }

    @Test
    void releasesPathOnlyWhenTrackerCanTakeAnUnwatchedTargetedMissileAtExitHeight()
    {
        assertFalse(MissileChunkLoaderPolicy.shouldKeepArcFlightTickets(false, false, true, true, EXIT_HEIGHT, EXIT_HEIGHT));
    }

    @Test
    void keepsPathWhenAnotherModOrMissingTargetRefusesSimulation()
    {
        assertTrue(MissileChunkLoaderPolicy.shouldKeepArcFlightTickets(true, false, true, true, EXIT_HEIGHT + 100, EXIT_HEIGHT));
        assertTrue(MissileChunkLoaderPolicy.shouldKeepArcFlightTickets(false, false, true, false, EXIT_HEIGHT + 100, EXIT_HEIGHT));
    }

    @Test
    void keepsWatchedMissilesAsEntitiesForVisualFlight()
    {
        assertTrue(MissileChunkLoaderPolicy.shouldKeepArcFlightTickets(false, true, true, true, EXIT_HEIGHT + 100, EXIT_HEIGHT));
    }

    @Test
    void keepsMissilesThatTheSimulationTrackerDoesNotSupport()
    {
        assertTrue(MissileChunkLoaderPolicy.shouldKeepArcFlightTickets(false, false, false, true, EXIT_HEIGHT + 100, EXIT_HEIGHT));
    }
}
