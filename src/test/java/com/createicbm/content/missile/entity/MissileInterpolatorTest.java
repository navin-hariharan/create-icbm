package com.createicbm.content.missile.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MissileInterpolatorTest
{
    /** Server missile flying at a constant velocity, the client gets its position every `interval` ticks `lag` ticks late */
    private static double[] simulate(double speed, int interval, int lag, int ticks, double[] stepSizes)
    {
        final MissileInterpolator interpolator = new MissileInterpolator();
        double clientX = 0;
        double lastClientX = 0;
        double clientY = 0;
        double clientZ = 0;
        double worst = 0;
        for (int tick = 1; tick <= ticks; tick++)
        {
            // server position of tick - lag arrives when the server sent one
            final int serverTick = tick - lag;
            if (serverTick >= 1 && serverTick % interval == 0)
            {
                interpolator.setTarget(speed * serverTick, 0, 0, 0, 0);
                if (!interpolator.hasTarget())
                {
                    throw new AssertionError();
                }
            }
            interpolator.step(clientX, clientY, clientZ, speed, 0, 0);
            clientX = interpolator.x();
            clientY = interpolator.y();
            clientZ = interpolator.z();
            stepSizes[tick - 1] = clientX - lastClientX;
            lastClientX = clientX;
            if (tick > 20)
            {
                worst = Math.max(worst, Math.abs(clientX - speed * (tick - lag + 1)));
            }
        }
        return new double[]{clientX, worst};
    }

    @Test
    void followsTheServerWithoutDrifting()
    {
        final double[] steps = new double[200];
        final double[] result = simulate(3.0, 1, 2, 200, steps);
        // The client ends up where the server was a bit ago, the update that is lag ticks old plus this tick
        assertEquals(3.0 * (200 - 2 + 1), result[0], 0.5);
        assertTrue(result[1] < 1.0, "error " + result[1]);
    }

    @Test
    void movesEvenlyEveryTickWithSparseUpdates()
    {
        final double[] steps = new double[200];
        simulate(3.0, 2, 3, 200, steps);
        // A jump to every server update would show up as steps of 0 and 6 blocks, this stays close to 3
        for (int i = 30; i < 200; i++)
        {
            assertEquals(3.0, steps[i], 0.6, "tick " + i);
        }
    }

    @Test
    void firstUpdateWithoutTargetJustFliesStraight()
    {
        final MissileInterpolator interpolator = new MissileInterpolator();
        interpolator.step(10, 20, 30, 1, 2, 3);
        assertEquals(11, interpolator.x(), 1e-9);
        assertEquals(22, interpolator.y(), 1e-9);
        assertEquals(33, interpolator.z(), 1e-9);
        assertFalse(interpolator.snapped());
    }

    @Test
    void snapsOnTeleport()
    {
        final MissileInterpolator interpolator = new MissileInterpolator();
        interpolator.setTarget(1000, 64, 1000, 0, 0);
        interpolator.step(0, 64, 0, 1, 0, 0);
        assertTrue(interpolator.snapped());
        assertEquals(1001, interpolator.x(), 1e-9);
        assertEquals(1000, interpolator.z(), 1e-9);
    }

    @Test
    void notANumberSnapsInsteadOfPoisoningThePosition()
    {
        final MissileInterpolator interpolator = new MissileInterpolator();
        interpolator.setTarget(5, 5, 5, 0, 0);
        interpolator.step(Double.NaN, 0, 0, 1, 0, 0);
        assertTrue(interpolator.snapped());
        assertEquals(6, interpolator.x(), 1e-9);
    }

    @Test
    void stalledConnectionStopsExtrapolatingForever()
    {
        final MissileInterpolator interpolator = new MissileInterpolator();
        interpolator.setTarget(0, 0, 0, 0, 0);
        double x = 0;
        for (int i = 0; i < 400; i++)
        {
            interpolator.step(x, 0, 0, 1, 0, 0);
            x = interpolator.x();
        }
        // Flies on by itself (nothing says otherwise) but the pull towards the stale position is capped
        assertTrue(x > 350, "x " + x);
    }

    @Test
    void turnsTheShortWayAroundTheCircle()
    {
        final MissileInterpolator interpolator = new MissileInterpolator();
        interpolator.setTarget(0, 0, 0, -170F, 10F);
        final float yaw = interpolator.yaw(170F);
        // 170 to -170 is 20 degrees through 180, not 340 the long way
        assertEquals(170F + 20F * MissileInterpolator.ROTATION_BLEND, yaw, 1e-3F);
        assertEquals(0F + 10F * MissileInterpolator.ROTATION_BLEND, interpolator.pitch(0F), 1e-3F);
    }
}
