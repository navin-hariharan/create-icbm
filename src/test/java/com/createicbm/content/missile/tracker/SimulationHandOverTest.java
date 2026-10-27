package com.createicbm.content.missile.tracker;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A missile the tracker refuses once must be offered again later, it used to be blocked for good
 */
class SimulationHandOverTest
{
    /** Stands in for the missile entity, the hand over never looks at it */
    private static final Object MISSILE = new Object();

    @Test
    void acceptedHandOverIsReported()
    {
        final SimulationHandOver<Object> handOver = new SimulationHandOver<>(entity -> true, entity -> {});
        assertTrue(handOver.tick(MISSILE, entity -> true));
        assertFalse(handOver.isRefused());
    }

    @Test
    void nothingIsOfferedWhileTheMissileCanStillTick()
    {
        final AtomicInteger offers = new AtomicInteger();
        final SimulationHandOver<Object> handOver = new SimulationHandOver<>(entity -> {
            offers.incrementAndGet();
            return true;
        }, entity -> {});
        for (int tick = 0; tick < 100; tick++)
        {
            assertFalse(handOver.tick(MISSILE, entity -> false));
        }
        assertEquals(0, offers.get());
    }

    @Test
    void refusedHandOverIsOfferedAgainAfterTheRetryDelay()
    {
        final AtomicInteger offers = new AtomicInteger();
        final AtomicInteger warnings = new AtomicInteger();
        final SimulationHandOver<Object> handOver = new SimulationHandOver<>(entity -> {
            offers.incrementAndGet();
            return false;
        }, entity -> warnings.incrementAndGet());

        assertFalse(handOver.tick(MISSILE, entity -> true));
        assertTrue(handOver.isRefused());
        assertEquals(1, offers.get());

        // Waits out the delay instead of asking every tick
        for (int tick = 0; tick < SimulationHandOver.RETRY_TICKS; tick++)
        {
            handOver.tick(MISSILE, entity -> true);
        }
        assertEquals(1, offers.get());

        // Then asks again, without logging a second time
        handOver.tick(MISSILE, entity -> true);
        assertEquals(2, offers.get());
        assertEquals(1, warnings.get());
    }

    @Test
    void laterAcceptedHandOverClearsTheRefusal()
    {
        final AtomicBoolean accept = new AtomicBoolean(false);
        final SimulationHandOver<Object> handOver = new SimulationHandOver<>(entity -> accept.get(), entity -> {});

        handOver.tick(MISSILE, entity -> true);
        assertTrue(handOver.isRefused());

        accept.set(true);
        for (int tick = 0; tick < SimulationHandOver.RETRY_TICKS; tick++)
        {
            handOver.tick(MISSILE, entity -> true);
        }
        assertTrue(handOver.tick(MISSILE, entity -> true));
        assertFalse(handOver.isRefused());
    }
}
