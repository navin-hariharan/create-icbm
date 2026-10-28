package com.createicbm.content.missile.logic.flight;

/**
 * Maths shared by the arc flight logics. Plain Java on purpose (no Minecraft classes) so it can be unit tested without
 * the game.
 * <p>
 * The arc is worked out from the flat (x/z) distance, so a target straight above or below the launcher has a flat
 * distance of zero and every division in it produced NaN or infinity, which was then written into the missile's
 * motion and made it vanish. Callers check {@link #solveArc(float)} and fall back to flying straight up, the same
 * way targets out of arc range are handled.
 */
public final class FlightMath
{
    /** Flat distance (meters) below which there is no arc to fly */
    public static final float MIN_ARC_DISTANCE = 1f;

    private FlightMath()
    {
    }

    /**
     * Result of {@link #solveArc(float)}
     *
     * @param flightTime   ticks the arc takes, at least 1
     * @param acceleration downward acceleration per tick that makes the arc land on the target
     */
    public record ArcSolution(int flightTime, float acceleration)
    {
    }

    /**
     * @param flatDistance distance to the target ignoring height
     * @return the arc to fly, or null when there is no valid arc (target straight above or below, or the maths gave
     * a value that is not finite) and the missile should fly straight up instead
     */
    public static ArcSolution solveArc(float flatDistance)
    {
        if (!(flatDistance >= MIN_ARC_DISTANCE) || !Float.isFinite(flatDistance))
        {
            return null;
        }

        //Path constants
        final float ticksPerMeterFlat = 2f;
        final float heightToDistanceScale = flatDistance > 1000 ? 3f : 1f;
        final float maxHeight = 1000f;
        final float initialArcHeight = flatDistance > 100 ? 160f : 0;

        // Parabolic Height
        final float arcHeightMax = Math.min(maxHeight, initialArcHeight + (flatDistance * heightToDistanceScale));

        // Flight time
        final int flightTime = (int) Math.ceil(ticksPerMeterFlat * flatDistance);
        if (flightTime < 1)
        {
            return null;
        }

        // Acceleration
        final double heightToDistance = arcHeightMax / flatDistance;
        final double heightToTime = arcHeightMax / flightTime;
        final double timeToDistance = flightTime / (double) flatDistance;
        final float acceleration = (float) (((arcHeightMax - heightToDistance) * heightToDistance) / (flightTime / timeToDistance) / (heightToTime * flatDistance));

        return Float.isFinite(acceleration) ? new ArcSolution(flightTime, acceleration) : null;
    }

    /**
     * @return true if every value is a finite number
     */
    public static boolean isFinite(double... values)
    {
        for (double value : values)
        {
            if (!Double.isFinite(value))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if the motion is not zero, so it has a direction to face
     */
    public static boolean hasDirection(double x, double y, double z)
    {
        return isFinite(x, y, z) && (x != 0 || y != 0 || z != 0);
    }

    /**
     * @return pitch in degrees (-90 down, 90 up) of the motion, 0 when the motion has no direction
     */
    public static double pitchDegrees(double x, double y, double z)
    {
        if (!hasDirection(x, y, z))
        {
            return 0;
        }
        return Math.atan2(y, Math.sqrt(x * x + z * z)) * 180 / Math.PI;
    }

    /**
     * @return yaw in degrees of the motion, 0 when the motion has no horizontal direction
     */
    public static double yawDegrees(double x, double z)
    {
        if (!isFinite(x, z))
        {
            return 0;
        }
        return Math.atan2(x, z) * 180 / Math.PI;
    }
}
