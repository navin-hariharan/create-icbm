package com.createicbm.content.missile.logic.flight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Targets straight above or below the launcher used to put NaN into the missile's motion
 */
class FlightMathTest
{
    @Test
    void zeroFlatDistanceHasNoArc()
    {
        assertNull(FlightMath.solveArc(0f));
    }

    @Test
    void verySmallFlatDistanceHasNoArc()
    {
        assertNull(FlightMath.solveArc(0.0001f));
        assertNull(FlightMath.solveArc(FlightMath.MIN_ARC_DISTANCE - 0.01f));
    }

    @Test
    void notANumberAndInfinityHaveNoArc()
    {
        assertNull(FlightMath.solveArc(Float.NaN));
        assertNull(FlightMath.solveArc(Float.POSITIVE_INFINITY));
        assertNull(FlightMath.solveArc(-5f));
    }

    @Test
    void normalDistancesGiveFiniteArcs()
    {
        for (float distance : new float[]{1f, 2.5f, 50f, 100f, 101f, 499f, 1000f, 1001f, 20000f})
        {
            final FlightMath.ArcSolution arc = FlightMath.solveArc(distance);
            assertNotNull(arc, "distance " + distance);
            assertTrue(arc.flightTime() >= 1, "distance " + distance);
            assertTrue(Float.isFinite(arc.acceleration()), "distance " + distance);
            assertTrue(arc.acceleration() >= 0, "distance " + distance);
        }
    }

    @Test
    void flightTimeIsTwoTicksPerMeterRoundedUp()
    {
        assertEquals(200, FlightMath.solveArc(100f).flightTime());
        assertEquals(3, FlightMath.solveArc(1.2f).flightTime());
    }

    @Test
    void finiteCheckCatchesNaNAndInfinity()
    {
        assertTrue(FlightMath.isFinite(0, -1, 2.5));
        assertFalse(FlightMath.isFinite(0, Double.NaN, 1));
        assertFalse(FlightMath.isFinite(Double.POSITIVE_INFINITY));
    }

    @Test
    void stationaryMotionHasNoDirectionAndZeroPitch()
    {
        assertFalse(FlightMath.hasDirection(0, 0, 0));
        assertEquals(0, FlightMath.pitchDegrees(0, 0, 0));
        assertFalse(Double.isNaN(FlightMath.pitchDegrees(0, 0, 0)));
    }

    @Test
    void pitchPointsUpDownAndLevel()
    {
        assertEquals(90, FlightMath.pitchDegrees(0, 2, 0), 1e-9);
        assertEquals(-90, FlightMath.pitchDegrees(0, -2, 0), 1e-9);
        assertEquals(45, FlightMath.pitchDegrees(1, 1, 0), 1e-9);
        assertEquals(0, FlightMath.pitchDegrees(3, 0, 4), 1e-9);
    }

    @Test
    void yawIgnoresInvalidMotion()
    {
        assertEquals(90, FlightMath.yawDegrees(1, 0), 1e-9);
        assertEquals(0, FlightMath.yawDegrees(Double.NaN, 1));
    }
}
