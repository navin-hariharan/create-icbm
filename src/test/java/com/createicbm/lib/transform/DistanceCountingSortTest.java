package com.createicbm.lib.transform;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistanceCountingSortTest
{
    @Test
    void ordersByKeyAndKeepsTheOrderOfEqualKeys()
    {
        final int[] keys = {5, 1, 5, 0, 1, 3};
        assertArrayEquals(new int[]{3, 1, 4, 5, 0, 2}, DistanceCountingSort.order(keys));
    }

    @Test
    void emptyAndSingle()
    {
        assertEquals(0, DistanceCountingSort.order(new int[0]).length);
        assertArrayEquals(new int[]{0}, DistanceCountingSort.order(new int[]{42}));
    }

    @Test
    void refusesKeysItCannotBucket()
    {
        assertNull(DistanceCountingSort.order(new int[]{1, -1}));
        assertNull(DistanceCountingSort.order(new int[]{1, DistanceCountingSort.MAX_KEY + 1}));
    }

    @Test
    void matchesAComparisonSortOnABlastSizedInput()
    {
        final Random random = new Random(5);
        final int[] keys = new int[300_000];
        for (int i = 0; i < keys.length; i++)
        {
            keys[i] = random.nextInt(3026);
        }
        final int[] order = DistanceCountingSort.order(keys);
        for (int i = 1; i < order.length; i++)
        {
            assertTrue(keys[order[i - 1]] <= keys[order[i]]);
            if (keys[order[i - 1]] == keys[order[i]])
            {
                assertTrue(order[i - 1] < order[i]);
            }
        }
    }
}
