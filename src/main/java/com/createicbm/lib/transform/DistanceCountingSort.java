package com.createicbm.lib.transform;

/**
 * Orders items by a small non negative integer key (squared distance from the middle of a blast) in linear time.
 * <p>
 * A blast of radius 55 has hundreds of thousands of positions but only about 3000 different squared distances, a
 * comparison sort spent most of its time re-computing the same distances. Plain Java so it can be unit tested.
 */
public final class DistanceCountingSort
{
    /** Largest key (so number of buckets) it will do, beyond that a comparison sort is the better choice */
    public static final int MAX_KEY = 4_000_000;

    private DistanceCountingSort()
    {
    }

    /**
     * @param keys the key of each item
     * @return the item indices from the smallest key to the largest, items with the same key stay in their order,
     * or null when a key is negative or above {@link #MAX_KEY}
     */
    public static int[] order(int[] keys)
    {
        int max = 0;
        for (int key : keys)
        {
            if (key < 0 || key > MAX_KEY)
            {
                return null;
            }
            if (key > max)
            {
                max = key;
            }
        }

        final int[] start = new int[max + 2];
        for (int key : keys)
        {
            start[key + 1]++;
        }
        for (int i = 1; i < start.length; i++)
        {
            start[i] += start[i - 1];
        }

        final int[] order = new int[keys.length];
        for (int i = 0; i < keys.length; i++)
        {
            order[start[keys[i]]++] = i;
        }
        return order;
    }
}
