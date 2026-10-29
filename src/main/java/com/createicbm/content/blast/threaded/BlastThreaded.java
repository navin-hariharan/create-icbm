package com.createicbm.content.blast.threaded;

import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
import com.createicbm.content.blast.Blast;
import com.createicbm.content.blast.BlastProtection;
import com.createicbm.lib.explosive.ThreadWorkBlast;
import com.createicbm.lib.thread.IThreadWork;
import com.createicbm.lib.thread.WorkerThreadManager;
import com.createicbm.lib.transform.BlockEditHandler;
import com.createicbm.lib.transform.PosDistanceSorter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robin) on 10/8/2018.
 */
public abstract class BlastThreaded extends Blast
{
    private boolean hasThreadStarted = false;
    private long diagnosticWorkerStartNanos;

    public BlastThreaded()
    {
    }

    protected IThreadWork getWorkerTask()
    {
        return new ThreadWorkBlast(this.toString(), this::doRun, this::onWorkerThreadComplete);
    }

    /**
     * @param loops - current loop count
     * @param edits - list of blocks to edit
     * @return true to run another iteration
     */
    public abstract boolean doRun(int loops, Consumer<BlockPos> edits);

    /**
     * Builds a sorter to sort all of the blocks post thread run
     *
     * @return
     */
    protected Comparator<BlockPos> buildSorter()
    {
        return new PosDistanceSorter(location, false, PosDistanceSorter.Sort.SQ);
    }

    protected void onPostThreadJoinWorld()
    {
        doExplode(-1);
        onBlastCompleted();
    }

    /**
     * Called when the thread completes, is still inside of the thread when called.
     *
     * @param edits
     */
    protected void onWorkerThreadComplete(List<BlockPos> edits)
    {
        ICBMDiagnostics.info("BLAST_WORKER_COMPUTE_COMPLETE blast={} dim={} center=({}, {}, {}) elapsedMs={} candidatePositions={} thread={} queueEdits={}",
            getClass().getSimpleName(), world == null ? "unknown" : world.dimension().location(), x(), y(), z(),
            diagnosticWorkerStartNanos == 0 ? -1 : (System.nanoTime() - diagnosticWorkerStartNanos) / 1_000_000.0,
            edits == null ? -1 : edits.size(), Thread.currentThread().getName(), !skipQueue());
        if (world instanceof ServerLevel)
        {
            //Sort distance
            edits.sort(buildSorter());

            //Schedule edits to run in the world
            scheduleOnServerThread(() -> {

                //Worker is done reading
                releaseChunkSnapshot();
                final long applyStartNanos = System.nanoTime();
                ICBMDiagnostics.info("BLAST_WORLD_APPLY_BEGIN blast={} dim={} candidatePositions={} tick={} thread={}",
                    getClass().getSimpleName(), world.dimension().location(), edits.size(), ((ServerLevel) world).getServer().getTickCount(), Thread.currentThread().getName());

                if (skipQueue())
                {
                    applyEdits(edits);
                    ICBMDiagnostics.info("BLAST_WORLD_APPLY_FINISHED blast={} dim={} candidatePositions={} elapsedMs={} mode=direct tick={} thread={}",
                        getClass().getSimpleName(), world.dimension().location(), edits.size(),
                        (System.nanoTime() - applyStartNanos) / 1_000_000.0, ((ServerLevel) world).getServer().getTickCount(), Thread.currentThread().getName());
                }
                else
                {
                    // The per-tick block edit budget can spread this across many ticks; completion is logged by the callback.
                    final int candidatePositions = edits.size();
                    BlockEditHandler.queueBatches(world, edits, this::applyEdits, () -> {
                        ICBMDiagnostics.info("BLAST_WORLD_APPLY_FINISHED blast={} dim={} candidatePositions={} elapsedMs={} mode=queued tick={} thread={}",
                            getClass().getSimpleName(), world.dimension().location(), candidatePositions,
                            (System.nanoTime() - applyStartNanos) / 1_000_000.0, ((ServerLevel) world).getServer().getTickCount(), Thread.currentThread().getName());
                        onWorldEditsCompleted(candidatePositions, (System.nanoTime() - applyStartNanos) / 1_000_000.0);
                    });
                    ICBMDiagnostics.info("BLAST_WORLD_APPLY_SUBMITTED blast={} dim={} candidatePositions={} elapsedMs={} tick={} thread={}",
                        getClass().getSimpleName(), world.dimension().location(), candidatePositions,
                        (System.nanoTime() - applyStartNanos) / 1_000_000.0, ((ServerLevel) world).getServer().getTickCount(), Thread.currentThread().getName());
                }

                // This is the blast's worker/dispatch callback, not proof that a queued block-edit workload has finished.
                onPostThreadJoinWorld();
            });
        }
    }

    /**
     * Applies a batch of edits. Claim and protection mods are asked about the blocks that will really change, as they
     * are now, so a big blast spread over many ticks still respects claims made or blocks placed meanwhile.
     */
    protected void applyEdits(List<BlockPos> batch)
    {
        // Blocks in chunks that are not loaded (any more) are left alone, reading them would load or generate the chunk
        if (!ConfigBlast.PROTECTION_EVENTS)
        {
            for (BlockPos pos : batch)
            {
                if (world.hasChunkAt(pos))
                {
                    destroyBlock(pos);
                }
            }
            return;
        }
        final List<BlockPos> changing = new ArrayList<>(batch.size());
        for (BlockPos pos : batch)
        {
            if (world.hasChunkAt(pos) && willEditBlock(pos, world.getBlockState(pos)))
            {
                changing.add(pos);
            }
        }
        final Set<BlockPos> denied = BlastProtection.denied(world, asVanillaExplosion(), changing);
        for (BlockPos pos : changing)
        {
            if (!denied.contains(pos))
            {
                destroyBlock(pos);
            }
        }
    }

    /**
     * @return true if {@link #destroyBlock(BlockPos)} would change the block, only those are checked against claims
     */
    protected boolean willEditBlock(BlockPos pos, BlockState state)
    {
        return !state.isAir();
    }

    protected boolean skipQueue()
    {
        return false;
    }

    @Override
    protected boolean doExplode(int callCount)
    {
        if (!hasThreadStarted)
        {
            hasThreadStarted = true;
            diagnosticWorkerStartNanos = System.nanoTime();
            ICBMDiagnostics.info("BLAST_WORKER_SUBMITTED blast={} dim={} center=({}, {}, {}) radius={} size={} tick={} thread={}",
                getClass().getSimpleName(), world == null ? "unknown" : world.dimension().location(), x(), y(), z(), getBlastRadius(), size,
                world instanceof ServerLevel serverLevel ? serverLevel.getServer().getTickCount() : -1, Thread.currentThread().getName());
            captureChunkSnapshot(getBlastRadius());
            WorkerThreadManager.INSTANCE.addWork(getWorkerTask());
        }
        return false;
    }

    /** Called after the queued world edits have actually been processed, rather than when the worker only submits them. */
    protected void onWorldEditsCompleted(int candidatePositions, double elapsedMs)
    {
        // Subclasses can emit domain-specific completion details (for example, the count of radioactive block swaps).
    }

    @Override
    protected void onBlastCompleted()
    {
        endBlast();
    }

    public void destroyBlock(BlockPos pos)
    {
        BlockState state = this.world().getBlockState(pos);
        if (willEditBlock(pos, state))
        {
            state.onBlockExploded(this.world(), pos, asVanillaExplosion());
        }
    }

}
