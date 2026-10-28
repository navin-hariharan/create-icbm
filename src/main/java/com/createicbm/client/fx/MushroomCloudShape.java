package com.createicbm.client.fx;

/**
 * The size and position of the parts of a mushroom cloud over time. Plain math without Minecraft classes so it can be
 * unit tested, {@link MushroomCloudFx} turns it into particles.
 * <p>
 * All lengths are in blocks for a blast of radius {@code r}, {@code p} is the progress from 0 (detonation) to 1 (the
 * cloud has risen as far as it goes).
 */
public final class MushroomCloudShape
{
    /** Height the middle of the cap ends up at, in blast radii */
    public static final double CAP_HEIGHT = 3.0;
    /** Radius the cap ends up with, in blast radii */
    public static final double CAP_RADIUS = 0.85;
    /** Radius of the stem, in blast radii */
    public static final double STEM_RADIUS = 0.17;
    /** Fraction of the progress the fireball phase takes */
    public static final double FIREBALL_END = 0.22;

    private MushroomCloudShape()
    {
    }

    private static double clamp01(double p)
    {
        return p < 0 ? 0 : Math.min(p, 1);
    }

    /**
     * @return 0 to 1, how far the cap has risen: quickly at first then slowing down
     */
    public static double rise(double p)
    {
        return 1.0 - Math.pow(1.0 - clamp01(p), 2.4);
    }

    /**
     * @return how many blocks per progress unit the cap moves up at p, the derivative of {@link #rise}
     */
    public static double riseSpeed(double p, double radius)
    {
        return radius * CAP_HEIGHT * 2.4 * Math.pow(1.0 - clamp01(p), 1.4);
    }

    /** Height of the middle of the cap above the ground */
    public static double capHeight(double p, double radius)
    {
        return radius * CAP_HEIGHT * rise(p);
    }

    /** Radius of the cap, it opens up while it rises */
    public static double capRadius(double p, double radius)
    {
        return radius * CAP_RADIUS * Math.min(1.0, 0.25 + 1.2 * Math.pow(clamp01(p), 0.6));
    }

    /** Radius of the stem at progress p, it thins out as it ages */
    public static double stemRadius(double p, double radius)
    {
        return radius * STEM_RADIUS * (1.0 - 0.35 * clamp01(p));
    }

    /**
     * Radius of the stem {@code h} blocks above the ground when the stem is {@code height} tall: the foot flares out
     */
    public static double stemRadiusAt(double h, double height, double p, double radius)
    {
        if (height <= 0)
        {
            return stemRadius(p, radius);
        }
        final double inverse = 1.0 - Math.min(1.0, Math.max(0.0, h / height));
        return stemRadius(p, radius) * (1.0 + 1.6 * inverse * inverse * inverse);
    }

    /** Radius of the fireball sphere at progress p (0 once the fireball phase is over) */
    public static double fireballRadius(double p, double radius)
    {
        if (p >= FIREBALL_END)
        {
            return 0;
        }
        final double x = clamp01(p / FIREBALL_END);
        return radius * 0.55 * (1.0 - Math.pow(1.0 - x, 3.0));
    }

    /** Radius the dust ring on the ground has grown to */
    public static double skirtRadius(double p, double radius)
    {
        return radius * (0.3 + 1.4 * clamp01(p));
    }

    /**
     * @return 0 (cooled, grey) to 1 (glowing) heat of the cloud at progress p
     */
    public static double heat(double p)
    {
        return Math.max(0.0, 1.0 - clamp01(p) / 0.55);
    }
}
