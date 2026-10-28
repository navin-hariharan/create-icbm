package com.createicbm.lib.transform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdaptiveBudgetTest
{
    @Test
    void idleServerKeepsTheFullBudget()
    {
        final AdaptiveBudget budget = new AdaptiveBudget();
        for (int i = 0; i < 500; i++)
        {
            budget.recordTick(12);
        }
        assertEquals(1.0, budget.factor(), 1e-9);
        assertEquals(20_000_000L, budget.scale(20_000_000L));
    }

    @Test
    void overloadedServerShrinksItButNeverToNothing()
    {
        final AdaptiveBudget budget = new AdaptiveBudget();
        for (int i = 0; i < 2000; i++)
        {
            budget.recordTick(80);
        }
        assertEquals(0.1, budget.factor(), 1e-9);
        assertTrue(budget.scale(20_000_000L) >= 1_000_000L);
        assertEquals(500_000L, budget.scale(500_000L)); // asking for less than a millisecond is left alone
    }

    @Test
    void recoversWhenTheServerCalmsDown()
    {
        final AdaptiveBudget budget = new AdaptiveBudget();
        for (int i = 0; i < 400; i++)
        {
            budget.recordTick(80);
        }
        final double low = budget.factor();
        assertTrue(low < 0.5);
        for (int i = 0; i < 2000; i++)
        {
            budget.recordTick(10);
        }
        assertEquals(1.0, budget.factor(), 1e-9);
    }

    @Test
    void deadBandDoesNotMoveTheFactor()
    {
        final AdaptiveBudget budget = new AdaptiveBudget();
        for (int i = 0; i < 400; i++)
        {
            budget.recordTick(80);
        }
        final double before = budget.factor();
        // First settle the smoothed time inside the band
        for (int i = 0; i < 400; i++)
        {
            budget.recordTick(40);
        }
        final double settled = budget.factor();
        for (int i = 0; i < 200; i++)
        {
            budget.recordTick(40);
        }
        assertEquals(settled, budget.factor(), 1e-9);
        assertTrue(settled >= before);
    }

    @Test
    void ignoresBrokenNumbers()
    {
        final AdaptiveBudget budget = new AdaptiveBudget();
        budget.recordTick(Double.NaN);
        budget.recordTick(-5);
        budget.recordTick(Double.POSITIVE_INFINITY);
        assertEquals(1.0, budget.factor(), 1e-9);
    }
}
