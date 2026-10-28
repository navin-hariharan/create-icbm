package com.createicbm.content.missile.tracker;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The chunks a straight flying missile holds ahead of itself. The walk must visit every chunk the path crosses, a
 * chunk skipped there is a chunk the missile can freeze in.
 */
class MissilePathSamplerTest
{
    private static long key(int chunkX, int chunkZ)
    {
        return MissilePathSampler.key(chunkX, chunkZ);
    }

    private static Set<Long> collect(double x, double z, double dirX, double dirZ, double distance, int max)
    {
        final Set<Long> wanted = new LinkedHashSet<>();
        MissilePathSampler.collect(x, z, dirX, dirZ, true, distance, max, wanted, k -> true);
        return wanted;
    }

    @Test
    void restingMissileHoldsOnlyItsOwnChunk()
    {
        final Set<Long> wanted = new LinkedHashSet<>();
        MissilePathSampler.collect(8, 8, 0, 0, false, 100, 16, wanted, k -> true);
        assertEquals(1, wanted.size());
        assertTrue(wanted.contains(key(0, 0)));
    }

    @Test
    void flyingEastCoversEveryChunkOnTheWay()
    {
        final Set<Long> wanted = collect(8, 8, 1, 0, 64, 16);
        for (int chunkX = 0; chunkX <= 4; chunkX++)
        {
            assertTrue(wanted.contains(key(chunkX, 0)), "chunk " + chunkX);
        }
        assertFalse(wanted.contains(key(6, 0)));
        assertEquals(5, wanted.size());
    }

    @Test
    void flyingWestAcrossZeroUsesFloorNotTruncation()
    {
        final Set<Long> wanted = collect(8, 8, -1, 0, 39, 16);
        assertTrue(wanted.contains(key(0, 0)));
        assertTrue(wanted.contains(key(-1, 0)));
        assertTrue(wanted.contains(key(-2, 0)));
        assertFalse(wanted.contains(key(-3, 0)));
    }

    @Test
    void negativeCoordinatesAreFloored()
    {
        final Set<Long> wanted = new LinkedHashSet<>();
        MissilePathSampler.collect(-1, -1, 0, 0, false, 0, 16, wanted, k -> true);
        assertTrue(wanted.contains(key(-1, -1)));
        assertFalse(wanted.contains(key(0, 0)));
    }

    @Test
    void diagonalPathClippingAChunkCornerStillIncludesThatChunk()
    {
        // Sampling this path every 8 blocks saw (0,0) then (1,1) and never the chunk (0,1) it cuts across for 0.6 blocks
        final Set<Long> wanted = collect(10.425, 12.620, 0.8311, 0.5561, 64, 16);
        assertTrue(wanted.contains(key(0, 0)));
        assertTrue(wanted.contains(key(0, 1)));
        assertTrue(wanted.contains(key(1, 1)));
    }

    @Test
    void pathThroughAnExactCornerIncludesBothNeighbours()
    {
        // From (8, 8) heading exactly diagonally the path goes through the corner at (16, 16)
        final Set<Long> wanted = collect(8, 8, Math.sqrt(0.5), Math.sqrt(0.5), 30, 16);
        assertTrue(wanted.contains(key(0, 0)));
        assertTrue(wanted.contains(key(1, 0)));
        assertTrue(wanted.contains(key(0, 1)));
        assertTrue(wanted.contains(key(1, 1)));
    }

    @Test
    void startingOnAChunkBorderHeadingBackIncludesTheChunkBehind()
    {
        final Set<Long> wanted = collect(16, 8, -1, 0, 20, 16);
        assertTrue(wanted.contains(key(1, 0)));
        assertTrue(wanted.contains(key(0, 0)));
    }

    @Test
    void nearAxisPathDoesNotSkipOrRunAway()
    {
        final Set<Long> wanted = collect(8, 8, 1, 1.0E-15, 160, 64);
        for (int chunkX = 0; chunkX <= 10; chunkX++)
        {
            assertTrue(wanted.contains(key(chunkX, 0)), "chunk " + chunkX);
        }
    }

    @Test
    void limitKeepsTheChunksClosestToTheMissile()
    {
        final Set<Long> wanted = collect(8, 8, 1, 0, 160, 3);
        assertEquals(3, wanted.size());
        assertTrue(wanted.contains(key(0, 0)));
        assertTrue(wanted.contains(key(1, 0)));
        assertTrue(wanted.contains(key(2, 0)));
    }

    @Test
    void chunksThatAlreadyTickAreSkippedButTheWalkContinuesThroughThem()
    {
        final Set<Long> wanted = new LinkedHashSet<>();
        MissilePathSampler.collect(8, 8, 1, 0, true, 64, 16, wanted, k -> k != key(1, 0));
        assertFalse(wanted.contains(key(1, 0)));
        assertTrue(wanted.contains(key(2, 0)));
        assertTrue(wanted.contains(key(4, 0)));
    }

    @Test
    void invalidInputCollectsOnlyTheOwnChunk()
    {
        final Set<Long> wanted = collect(8, 8, Double.NaN, 0, 64, 16);
        assertEquals(1, wanted.size());
        assertEquals(1, collect(8, 8, 1, 0, 0, 16).size());
        assertEquals(1, collect(8, 8, 1, 0, Double.POSITIVE_INFINITY, 16).size());
    }

    /**
     * Brute force: walk the segment in tiny steps and note every chunk it is ever in. The walk must contain them all.
     */
    @Test
    void everyChunkCrossedByARandomPathIsIncluded()
    {
        final Random random = new Random(20261028L);
        for (int run = 0; run < 2000; run++)
        {
            final double x = (random.nextDouble() - 0.5) * 2000;
            final double z = (random.nextDouble() - 0.5) * 2000;
            final double angle = random.nextDouble() * Math.PI * 2;
            final double dirX = Math.cos(angle);
            final double dirZ = Math.sin(angle);
            final double distance = 1 + random.nextDouble() * 160;

            final Set<Long> expected = new HashSet<>();
            for (double d = 0; d <= distance; d += 0.01)
            {
                expected.add(key(MissilePathSampler.chunkOf(x + dirX * d), MissilePathSampler.chunkOf(z + dirZ * d)));
            }
            expected.add(key(MissilePathSampler.chunkOf(x + dirX * distance), MissilePathSampler.chunkOf(z + dirZ * distance)));

            final Set<Long> wanted = collect(x, z, dirX, dirZ, distance, 10_000);
            assertTrue(wanted.containsAll(expected), "run " + run + " from " + x + ", " + z + " heading " + angle + " for " + distance);
            // Only corners add chunks the line doesn't strictly cross, so it must not stray far from the exact set
            assertTrue(wanted.size() <= expected.size() + 2 * (int) (distance / 16 + 2), "run " + run);
        }
    }
}
