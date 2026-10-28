package com.createicbm.lib.transform;

import com.createicbm.ICBMConstants;
import com.createicbm.config.blast.ConfigBlast;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robin) on 10/8/2018.
 */
@EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class BlockEditHandler
{
    public static final HashMap<ResourceKey<Level>, Queue<EditQueue>> worldToRemoveQueue = new HashMap<>();
    // PERF: one time budget per server tick (ConfigBlast#BLOCK_EDIT_MS_PER_TICK) shared by every level and both tick
    // phases. It was 70% of a tick per level and per phase, so a big blast could take 70ms+ of every tick.
    /** Server tick the used budget below belongs to */
    private static int budgetTick = Integer.MIN_VALUE;
    /** Nanoseconds spent on edits in {@link #budgetTick} */
    private static long budgetUsedNanos = 0;

    /** Shrinks the budgets while the server is lagging, see {@link ConfigBlast#ADAPTIVE_BUDGET} */
    private static final AdaptiveBudget ADAPTIVE = new AdaptiveBudget();
    private static long tickStartNanos = 0;

    /** Blocks edited between two checks of the clock */
    private static final int BATCH = 64;

    /**
     * @param ms a budget in milliseconds from the config
     * @return nanoseconds to spend, lowered while the server tick is running long
     */
    public static long budgetNanos(int ms)
    {
        final long nanos = ms * 1_000_000L;
        return ConfigBlast.ADAPTIVE_BUDGET ? ADAPTIVE.scale(nanos) : nanos;
    }

    @SubscribeEvent
    public static void onServerTickPre(ServerTickEvent.Pre event)
    {
        tickStartNanos = System.nanoTime();
    }

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event)
    {
        if (tickStartNanos != 0)
        {
            ADAPTIVE.recordTick((System.nanoTime() - tickStartNanos) / 1_000_000.0);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event)
    {
        worldToRemoveQueue.clear();
        ADAPTIVE.reset();
        tickStartNanos = 0;
        budgetTick = Integer.MIN_VALUE;
        budgetUsedNanos = 0;
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event)
    {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide())
        {
            return;
        }
        worldToRemoveQueue.remove(level.dimension());
    }

    // Limited to the server as the queue is only filled by server side logic.
    @SubscribeEvent
    public static void onWorldTickPre(LevelTickEvent.Pre event)
    {
        onWorldTick(event.getLevel());
    }

    @SubscribeEvent
    public static void onWorldTickPost(LevelTickEvent.Post event)
    {
        onWorldTick(event.getLevel());
    }

    private static void onWorldTick(Level world)
    {
        if (world.isClientSide())
        {
            return;
        }
        final Queue<EditQueue> list = worldToRemoveQueue.get(world.dimension());
        if (list == null || list.isEmpty())
        {
            return;
        }

        final int tick = world instanceof ServerLevel serverLevel ? serverLevel.getServer().getTickCount() : 0;
        if (tick != budgetTick)
        {
            budgetTick = tick;
            budgetUsedNanos = 0;
        }
        final long startTime = System.nanoTime();
        // calculate time when to stop
        final long endTime = startTime + Math.max(0, budgetNanos(ConfigBlast.BLOCK_EDIT_MS_PER_TICK) - budgetUsedNanos);

        // A snapshot, finishing a queue can start a new one (the callbacks) and that must not disturb this loop
        final EditQueue[] snapshot = list.toArray(new EditQueue[0]);
        for (EditQueue queue : snapshot)
        {
            if (System.nanoTime() >= endTime)
            {
                break;
            }

            final boolean done;
            if (queue.paceTicks > 0)
            {
                // Paced queues take their share of this tick and let the ones behind them have the rest
                done = queue.workPaced(tick, endTime);
            }
            else
            {
                done = queue.workUntil(endTime);
            }

            if (done)
            {
                list.remove(queue);
            }
            else if (queue.paceTicks <= 0)
            {
                // Out of time. Unpaced queues finish in order, the ones behind wait for it
                break;
            }
        }
        budgetUsedNanos += System.nanoTime() - startTime;
    }

    public static void queue(Level world, Collection<BlockPos> edits, Consumer<BlockPos> onEditBlock)
    {
        queue(world, edits, onEditBlock, null);
    }

    public static void queue(Level world, Collection<BlockPos> edits, Consumer<BlockPos> onEditBlock, Runnable onCompleteCallback)
    {
        queueBatches(world, edits, batch -> batch.forEach(onEditBlock), onCompleteCallback);
    }

    /**
     * Like {@link #queue(Level, Collection, Consumer, Runnable)} but hands over each batch of positions at once, so
     * work such as claim checks can be done once per batch, right before the blocks are edited
     */
    public static void queueBatches(Level world, Collection<BlockPos> edits, Consumer<List<BlockPos>> onEditBatch, Runnable onCompleteCallback)
    {
        queueBatches(world, edits, onEditBatch, onCompleteCallback, 0);
    }

    /**
     * Like {@link #queueBatches(Level, Collection, Consumer, Runnable)} but spreads the edits evenly over about the
     * given number of ticks, so a big crater opens up smoothly instead of in a few lumps. The time budget still
     * applies, a paced queue that falls behind catches up on what is left.
     *
     * @param paceTicks ticks to spread the work over, 0 or less for as fast as the budget allows
     */
    public static void queueBatches(Level world, Collection<BlockPos> edits, Consumer<List<BlockPos>> onEditBatch, Runnable onCompleteCallback, int paceTicks)
    {
        worldToRemoveQueue.computeIfAbsent(world.dimension(), key -> new LinkedList<>())
            .add(new EditQueue(edits, onEditBatch, onCompleteCallback, paceTicks));
    }

    public static class EditQueue
    {
        // PERF: an array and a cursor, a linked list of a few hundred thousand positions was a node per block
        private final BlockPos[] positions;
        private int cursor = 0;
        final Consumer<List<BlockPos>> onEditBatch;
        final Runnable onCompleteCallback;
        final int paceTicks;
        private int pacedTicksUsed = 0;
        private int lastTick = Integer.MIN_VALUE;
        private boolean completed = false;

        public EditQueue(Collection<BlockPos> edits, Consumer<List<BlockPos>> onEditBatch, Runnable onCompleteCallback)
        {
            this(edits, onEditBatch, onCompleteCallback, 0);
        }

        public EditQueue(Collection<BlockPos> edits, Consumer<List<BlockPos>> onEditBatch, Runnable onCompleteCallback, int paceTicks)
        {
            this.positions = edits.toArray(new BlockPos[0]);
            this.onEditBatch = onEditBatch;
            this.onCompleteCallback = onCompleteCallback;
            this.paceTicks = paceTicks;
        }

        int remaining()
        {
            return positions.length - cursor;
        }

        /**
         * Edits as much as fits the time. Also called on a queue with nothing in it, to run its callback.
         *
         * @return true when the queue is finished
         */
        boolean workUntil(long endNanos)
        {
            do
            {
                doWork(BATCH);
            }
            while (remaining() > 0 && System.nanoTime() < endNanos);
            return remaining() == 0;
        }

        /**
         * Edits this tick's share. The share is what is left divided by the ticks that are left, so being held back
         * by the time budget is made up for later.
         *
         * @return true when the queue is finished
         */
        boolean workPaced(int tick, long endNanos)
        {
            if (tick == lastTick && remaining() > 0)
            {
                // Called for both tick phases, the share is for the whole tick
                return false;
            }
            lastTick = tick;
            final int ticksLeft = Math.max(1, paceTicks - pacedTicksUsed);
            pacedTicksUsed++;
            final long share = (remaining() + ticksLeft - 1L) / ticksLeft;
            final int quota = (int) Math.min(Math.max(1L, share), Math.max(1, ConfigBlast.MAX_PACED_BLOCKS_PER_TICK));

            int done = 0;
            do
            {
                done += doWork(Math.min(BATCH, quota - done));
            }
            while (done < quota && remaining() > 0 && System.nanoTime() < endNanos);
            return remaining() == 0;
        }

        /**
         * @return how many positions were edited
         */
        int doWork(int limit)
        {
            final int count = Math.max(0, Math.min(limit, remaining()));
            if (count > 0)
            {
                final List<BlockPos> batch = new ArrayList<>(count);
                for (int i = 0; i < count; i++)
                {
                    batch.add(positions[cursor]);
                    positions[cursor++] = null; // lets the position go as soon as it is edited
                }
                onEditBatch.accept(batch);
            }

            if (remaining() == 0 && !completed)
            {
                completed = true;
                if (onCompleteCallback != null)
                {
                    onCompleteCallback.run();
                }
            }
            return count;
        }
    }
}
