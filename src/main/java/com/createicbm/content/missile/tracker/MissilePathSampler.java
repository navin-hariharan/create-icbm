package com.createicbm.content.missile.tracker;

import java.util.Set;
import java.util.function.LongPredicate;

/**
 * Works out which chunks a straight path crosses, closest first.
 * <p>
 * The path is walked cell by cell through the chunk grid (Amanatides and Woo's voxel traversal), so every chunk the
 * line touches is visited. Sampling the line every few blocks cannot promise that: on a diagonal the line can clip the
 * corner of a chunk for less than the sample step and never be seen in it. Where the line passes exactly through a
 * corner both chunks beside the corner are included as well (a "supercover"), because the missile has a hitbox and
 * either one could be the one it ends up in.
 * <p>
 * Plain Java on purpose (no Minecraft or fastutil classes, the unit test classpath has neither) so it can be tested
 * without the game, see {@link MissileChunkLoader}.
 */
public final class MissilePathSampler
{
    /** Parameter distance (fraction of the path) under which two chunk borders count as crossed together, a corner */
    private static final double CORNER_EPSILON = 1.0E-9;

    private MissilePathSampler()
    {
    }

    /**
     * @return the chunk key, the same bit layout as {@code ChunkPos.asLong} so keys can go straight to Minecraft
     */
    public static long key(int chunkX, int chunkZ)
    {
        return (chunkX & 0xFFFFFFFFL) | ((chunkZ & 0xFFFFFFFFL) << 32);
    }

    /**
     * @return the chunk coordinate a block coordinate is in, floor based so negative coordinates are right
     */
    public static int chunkOf(double coordinate)
    {
        return (int) Math.floor(coordinate) >> 4;
    }

    /**
     * Collects the chunk the missile is in, then every chunk its horizontal path crosses, nearest first.
     *
     * @param x         missile x
     * @param z         missile z
     * @param dirX      normalized heading x
     * @param dirZ      normalized heading z
     * @param moving    false when the missile has no horizontal heading, only its own chunk is collected
     * @param distance  meters to look ahead
     * @param maxChunks most chunks to collect, the ones closest to the missile win
     * @param wanted    receives the chunk keys, see {@link #key(int, int)}
     * @param accept    asked for each distinct chunk key, return false to skip it (for example chunks that already tick).
     *                  Skipped chunks don't count towards {@code maxChunks} but the walk still goes through them.
     */
    public static void collect(double x, double z, double dirX, double dirZ, boolean moving, double distance,
                               int maxChunks, Set<Long> wanted, LongPredicate accept)
    {
        int chunkX = chunkOf(x);
        int chunkZ = chunkOf(z);
        add(chunkX, chunkZ, maxChunks, wanted, accept);
        if (!moving || !(distance > 0) || !Double.isFinite(distance + dirX + dirZ + x + z))
        {
            return;
        }

        final double deltaX = dirX * distance;
        final double deltaZ = dirZ * distance;
        final int stepX = deltaX > 0 ? 1 : -1;
        final int stepZ = deltaZ > 0 ? 1 : -1;

        // Fraction of the path (0 to 1) at which the next chunk border is crossed, and the fraction a whole chunk takes
        final double absX = Math.abs(deltaX);
        final double absZ = Math.abs(deltaZ);
        final double tDeltaX = absX < 1.0E-12 ? Double.POSITIVE_INFINITY : 16.0 / absX;
        final double tDeltaZ = absZ < 1.0E-12 ? Double.POSITIVE_INFINITY : 16.0 / absZ;
        double tMaxX = absX < 1.0E-12 ? Double.POSITIVE_INFINITY : ((stepX > 0 ? (chunkX + 1) * 16.0 - x : x - chunkX * 16.0) / absX);
        double tMaxZ = absZ < 1.0E-12 ? Double.POSITIVE_INFINITY : ((stepZ > 0 ? (chunkZ + 1) * 16.0 - z : z - chunkZ * 16.0) / absZ);

        // A path of N meters crosses at most about N / 16 borders on each axis, the guard only stops runaway loops
        final int guard = (int) Math.min(100_000, Math.ceil(distance / 16.0) * 2 + 8);
        for (int i = 0; i < guard && wanted.size() < maxChunks; i++)
        {
            if (tMaxX > 1 && tMaxZ > 1)
            {
                return;
            }
            if (Math.abs(tMaxX - tMaxZ) <= CORNER_EPSILON)
            {
                // Straight through a corner: both chunks beside it, then the one across
                add(chunkX + stepX, chunkZ, maxChunks, wanted, accept);
                add(chunkX, chunkZ + stepZ, maxChunks, wanted, accept);
                chunkX += stepX;
                chunkZ += stepZ;
                tMaxX += tDeltaX;
                tMaxZ += tDeltaZ;
            }
            else if (tMaxX < tMaxZ)
            {
                chunkX += stepX;
                tMaxX += tDeltaX;
            }
            else
            {
                chunkZ += stepZ;
                tMaxZ += tDeltaZ;
            }
            add(chunkX, chunkZ, maxChunks, wanted, accept);
        }
    }

    private static void add(int chunkX, int chunkZ, int maxChunks, Set<Long> wanted, LongPredicate accept)
    {
        if (wanted.size() >= maxChunks)
        {
            return;
        }
        final long key = key(chunkX, chunkZ);
        if (!wanted.contains(key) && accept.test(key))
        {
            wanted.add(key);
        }
    }
}
