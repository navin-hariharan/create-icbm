package com.createicbm.client.fx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MushroomCloudShapeTest
{
    private static final double R = 55;

    @Test
    void capRisesMonotonicallyToItsFullHeight()
    {
        double last = -1;
        for (int i = 0; i <= 100; i++)
        {
            final double height = MushroomCloudShape.capHeight(i / 100.0, R);
            assertTrue(height >= last);
            last = height;
        }
        assertEquals(R * MushroomCloudShape.CAP_HEIGHT, MushroomCloudShape.capHeight(1.0, R), 1e-9);
        assertEquals(0, MushroomCloudShape.capHeight(0, R), 1e-9);
        // Out of range progress is clamped
        assertEquals(0, MushroomCloudShape.capHeight(-3, R), 1e-9);
        assertEquals(R * MushroomCloudShape.CAP_HEIGHT, MushroomCloudShape.capHeight(7, R), 1e-9);
    }

    @Test
    void riseSpeedIsTheSlopeOfTheRise()
    {
        for (double p = 0.05; p < 0.95; p += 0.1)
        {
            final double h = 1e-6;
            final double slope = (MushroomCloudShape.capHeight(p + h, R) - MushroomCloudShape.capHeight(p - h, R)) / (2 * h);
            assertEquals(slope, MushroomCloudShape.riseSpeed(p, R), slope * 1e-4);
        }
        assertEquals(0, MushroomCloudShape.riseSpeed(1, R), 1e-9);
    }

    @Test
    void capIsWiderThanTheStemAndOpensUp()
    {
        assertTrue(MushroomCloudShape.capRadius(1, R) > 3 * MushroomCloudShape.stemRadius(1, R));
        assertTrue(MushroomCloudShape.capRadius(0.5, R) > MushroomCloudShape.capRadius(0.0, R));
        assertEquals(R * MushroomCloudShape.CAP_RADIUS, MushroomCloudShape.capRadius(1, R), 1e-9);
    }

    @Test
    void stemFlaresAtTheFoot()
    {
        final double foot = MushroomCloudShape.stemRadiusAt(0, 100, 0.3, R);
        final double middle = MushroomCloudShape.stemRadiusAt(50, 100, 0.3, R);
        final double top = MushroomCloudShape.stemRadiusAt(100, 100, 0.3, R);
        assertTrue(foot > middle);
        assertTrue(middle > top);
        assertEquals(MushroomCloudShape.stemRadius(0.3, R), top, 1e-9);
        // No height yet: no division by zero
        assertEquals(MushroomCloudShape.stemRadius(0.3, R), MushroomCloudShape.stemRadiusAt(0, 0, 0.3, R), 1e-9);
    }

    @Test
    void fireballGrowsThenIsGone()
    {
        assertEquals(0, MushroomCloudShape.fireballRadius(0, R), 1e-9);
        assertTrue(MushroomCloudShape.fireballRadius(0.1, R) > 0);
        assertTrue(MushroomCloudShape.fireballRadius(0.2, R) > MushroomCloudShape.fireballRadius(0.1, R));
        assertEquals(0, MushroomCloudShape.fireballRadius(MushroomCloudShape.FIREBALL_END, R), 1e-9);
    }

    @Test
    void heatCoolsDown()
    {
        assertEquals(1, MushroomCloudShape.heat(0), 1e-9);
        assertEquals(0, MushroomCloudShape.heat(0.6), 1e-9);
        assertEquals(0, MushroomCloudShape.heat(1), 1e-9);
    }
}
