package com.createicbm.content.missile.entity;

/**
 * Client side smoothing of a missile's flight.
 * <p>
 * The server tells the client where a missile is every tick or two and the client used to jump there, so a missile
 * flying several blocks per tick moved in 20 steps per second however high the frame rate. Instead the client missile
 * keeps flying on its own with the velocity it was last given (dead reckoning) and every update from the server only
 * pulls it gently towards where the server says it should be. The server position is a little old by the time it
 * arrives, so it is moved forward by the velocity for every tick it has been waiting.
 * <p>
 * Plain Java (no Minecraft classes) so it can be unit tested. It holds the result in fields to not allocate anything
 * per missile per tick.
 */
public final class MissileInterpolator
{
    /** Share of the distance to the server's position that is made up each tick */
    public static final double POSITION_BLEND = 0.3;
    /** Share of the angle to the server's rotation that is made up each tick */
    public static final float ROTATION_BLEND = 0.35F;
    /** Further than this from where the server says the missile is (a teleport, a lagging client) it just jumps */
    public static final double SNAP_DISTANCE = 8.0;
    /** A server update older than this many ticks is not followed anymore, the connection stalled */
    public static final int MAX_EXTRAPOLATION_TICKS = 40;

    private boolean hasTarget;
    private double targetX, targetY, targetZ;
    private float targetYaw, targetPitch;
    private int ticksSinceUpdate;

    private double x, y, z;
    private boolean snapped;

    public boolean hasTarget()
    {
        return hasTarget;
    }

    /**
     * Called with the position the server sent
     */
    public void setTarget(double x, double y, double z, float yaw, float pitch)
    {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.targetYaw = yaw;
        this.targetPitch = pitch;
        this.ticksSinceUpdate = 0;
        this.hasTarget = true;
    }

    /**
     * Moves the missile one tick.
     *
     * @param px current position
     * @param vx velocity (blocks per tick)
     * @return the new position through {@link #x()}, {@link #y()}, {@link #z()}
     */
    public void step(double px, double py, double pz, double vx, double vy, double vz)
    {
        // Keeps flying with the velocity
        x = px + vx;
        y = py + vy;
        z = pz + vz;
        snapped = false;
        if (!hasTarget)
        {
            return;
        }

        // Too old to trust (the server stopped sending), it just keeps flying
        if (ticksSinceUpdate >= MAX_EXTRAPOLATION_TICKS)
        {
            return;
        }

        // Where the server's copy is by now, it was sent ticksSinceUpdate ticks ago (this tick counts)
        final int age = ticksSinceUpdate + 1;
        ticksSinceUpdate++;
        final double expectedX = targetX + vx * age;
        final double expectedY = targetY + vy * age;
        final double expectedZ = targetZ + vz * age;

        final double errorX = expectedX - x;
        final double errorY = expectedY - y;
        final double errorZ = expectedZ - z;
        final double errorSq = errorX * errorX + errorY * errorY + errorZ * errorZ;
        if (!(errorSq <= SNAP_DISTANCE * SNAP_DISTANCE)) // also catches NaN
        {
            x = expectedX;
            y = expectedY;
            z = expectedZ;
            snapped = true;
            return;
        }
        x += errorX * POSITION_BLEND;
        y += errorY * POSITION_BLEND;
        z += errorZ * POSITION_BLEND;
    }

    public double x()
    {
        return x;
    }

    public double y()
    {
        return y;
    }

    public double z()
    {
        return z;
    }

    /**
     * @return true when the last step jumped instead of blending, the renderer should not smooth over that
     */
    public boolean snapped()
    {
        return snapped;
    }

    /**
     * @return the yaw after turning a part of the way to the server's yaw, the short way round
     */
    public float yaw(float current)
    {
        return hasTarget ? turn(current, targetYaw) : current;
    }

    public float pitch(float current)
    {
        return hasTarget ? current + (targetPitch - current) * ROTATION_BLEND : current;
    }

    private static float turn(float current, float target)
    {
        float delta = (target - current) % 360F;
        if (delta >= 180F)
        {
            delta -= 360F;
        }
        else if (delta < -180F)
        {
            delta += 360F;
        }
        return current + delta * ROTATION_BLEND;
    }
}
