package com.createicbm.lib.transform;

/**
 * Scales the time that explosions may take per tick down while the server is struggling and back up when it recovers.
 * <p>
 * Fed the time every server tick took. The smoothed tick time has a dead band (a limit to go down at and a lower one to
 * go back up at) so the budget does not flip back and forth, and it changes a few percent per tick, never to zero so a
 * blast always finishes eventually. Plain Java, no Minecraft classes, so it can be unit tested.
 */
public final class AdaptiveBudget
{
    /** Above this smoothed tick time (ms) the budget shrinks */
    private final double highMs;
    /** Below this smoothed tick time (ms) the budget grows again */
    private final double lowMs;
    private final double minFactor;
    private double smoothedMs = Double.NaN;
    private double factor = 1.0;

    public AdaptiveBudget()
    {
        this(45.0, 35.0, 0.1);
    }

    public AdaptiveBudget(double highMs, double lowMs, double minFactor)
    {
        this.highMs = highMs;
        this.lowMs = lowMs;
        this.minFactor = minFactor;
    }

    /**
     * @param tickMs how long the server tick took to run (not counting the wait for the next one)
     */
    public void recordTick(double tickMs)
    {
        if (!(tickMs >= 0) || Double.isInfinite(tickMs))
        {
            return;
        }
        smoothedMs = Double.isNaN(smoothedMs) ? tickMs : smoothedMs + (tickMs - smoothedMs) * 0.05;
        if (smoothedMs > highMs)
        {
            factor = Math.max(minFactor, factor * 0.97);
        }
        else if (smoothedMs < lowMs)
        {
            factor = Math.min(1.0, factor * 1.02);
        }
    }

    /**
     * @return 1 for an idle server, down to the minimum for an overloaded one
     */
    public double factor()
    {
        return factor;
    }

    /**
     * @return the nanoseconds scaled down by the current factor, at least 1 ms worth when asked for that much
     */
    public long scale(long nanos)
    {
        return Math.max(Math.min(nanos, 1_000_000L), (long) (nanos * factor));
    }

    public void reset()
    {
        smoothedMs = Double.NaN;
        factor = 1.0;
    }
}
