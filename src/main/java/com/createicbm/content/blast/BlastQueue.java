package com.createicbm.content.blast;

import com.createicbm.CreateICBM;
import com.createicbm.ICBMConstants;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.lib.transform.BlockEditHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Spreads explosions over several ticks when many go off at once, for example a salvo of missiles landing together.
 * <p>
 * An explosion runs right away while the current server tick still has budget left ({@link ConfigBlast#BLAST_MS_PER_TICK})
 * and nothing is waiting, which covers every explosion in normal play. Past the budget the rest wait in a queue per
 * level and run at the end of the following level ticks, oldest first and at least one per tick.
 */
@EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public final class BlastQueue
{
    /** Explosions waiting to run, per level */
    private static final Map<Level, ArrayDeque<Runnable>> QUEUES = new HashMap<>();

    /** Server tick the used budget below belongs to */
    private static int budgetTick = Integer.MIN_VALUE;
    /** Nanoseconds of explosion work done in {@link #budgetTick} */
    private static long budgetUsedNanos = 0;
    /** True while a timed explosion runs, explosions it sets off (chain reactions) count as part of it */
    private static boolean running = false;

    private BlastQueue()
    {
    }

    /**
     * @param level - level the explosion happens in
     * @return true if an explosion starting now should wait in the queue
     */
    public static boolean shouldQueue(Level level)
    {
        if (!(level instanceof ServerLevel) || ConfigBlast.BLAST_MS_PER_TICK <= 0)
        {
            return false;
        }
        final ArrayDeque<Runnable> queue = QUEUES.get(level);
        if (queue != null && !queue.isEmpty())
        {
            return true; // keep the order, nothing skips ahead of explosions already waiting
        }
        refreshBudget(level);
        return budgetUsedNanos >= BlockEditHandler.budgetNanos(ConfigBlast.BLAST_MS_PER_TICK);
    }

    /**
     * Queues an explosion to run on a later tick of the level, see {@link #shouldQueue(Level)}
     *
     * @param level     - level the explosion happens in
     * @param explosion - creates and runs the blast
     */
    public static void queue(Level level, Runnable explosion)
    {
        QUEUES.computeIfAbsent(level, k -> new ArrayDeque<>()).add(explosion);
    }

    /** Number of pending blast actions for diagnostics. */
    public static int queuedCount(Level level)
    {
        final ArrayDeque<Runnable> queue = QUEUES.get(level);
        return queue == null ? 0 : queue.size();
    }

    /**
     * Runs an explosion now and counts the time it takes against the budget of the current tick
     *
     * @param level     - level the explosion happens in
     * @param explosion - creates and runs the blast
     * @return result of the explosion
     */
    public static <T> T runTimed(Level level, Supplier<T> explosion)
    {
        if (running || !(level instanceof ServerLevel))
        {
            return explosion.get();
        }
        refreshBudget(level);
        running = true;
        final long start = System.nanoTime();
        try
        {
            return explosion.get();
        }
        finally
        {
            running = false;
            budgetUsedNanos += System.nanoTime() - start;
        }
    }

    private static void refreshBudget(Level level)
    {
        final int tick = ((ServerLevel) level).getServer().getTickCount();
        if (tick != budgetTick)
        {
            budgetTick = tick;
            budgetUsedNanos = 0;
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event)
    {
        final Level level = event.getLevel();
        if (level.isClientSide())
        {
            return;
        }
        final ArrayDeque<Runnable> queue = QUEUES.get(level);
        if (queue == null || queue.isEmpty())
        {
            return;
        }

        refreshBudget(level);
        boolean first = true;
        while (!queue.isEmpty() && (first || budgetUsedNanos < BlockEditHandler.budgetNanos(ConfigBlast.BLAST_MS_PER_TICK)))
        {
            first = false;
            final Runnable explosion = queue.poll();
            try
            {
                runTimed(level, () -> {
                    explosion.run();
                    return null;
                });
            }
            catch (Exception e)
            {
                CreateICBM.logger().error("BlastQueue: Unexpected error running queued explosion in " + level.dimension().location(), e);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event)
    {
        if (event.getLevel() instanceof Level level && !level.isClientSide())
        {
            final ArrayDeque<Runnable> queue = QUEUES.remove(level);
            if (queue != null && !queue.isEmpty())
            {
                CreateICBM.logger().warn("BlastQueue: Dropped {} queued explosions as {} unloaded", queue.size(), level.dimension().location());
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event)
    {
        QUEUES.clear();
        budgetTick = Integer.MIN_VALUE;
        budgetUsedNanos = 0;
        running = false;
    }
}
