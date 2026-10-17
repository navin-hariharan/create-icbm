package com.createicbm.content.blast.threaded;

import com.createicbm.content.blast.Blast;
import com.createicbm.lib.explosive.ThreadWorkBlast;
import com.createicbm.lib.thread.IThreadWork;
import com.createicbm.lib.thread.WorkerThreadManager;
import com.createicbm.lib.transform.BlockEditHandler;
import com.createicbm.lib.transform.PosDistanceSorter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robin) on 10/8/2018.
 */
public abstract class BlastThreaded extends Blast
{
    private boolean hasThreadStarted = false;

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
        if (world instanceof ServerLevel)
        {
            //Sort distance
            edits.sort(buildSorter());

            //Schedule edits to run in the world
            scheduleOnServerThread(() -> {

                //Worker is done reading
                releaseChunkSnapshot();

                if (skipQueue())
                {
                    edits.forEach(blockPos -> destroyBlock(blockPos));
                }
                else
                {
                    //Queue edits
                    BlockEditHandler.queue(world, edits, blockPos -> destroyBlock(blockPos));
                }

                //Notify blast we have entered world again
                onPostThreadJoinWorld();
            });
        }
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

            captureChunkSnapshot(getBlastRadius());
            WorkerThreadManager.INSTANCE.addWork(getWorkerTask());
        }
        return false;
    }

    @Override
    protected void onBlastCompleted()
    {
        endBlast();
    }

    public void destroyBlock(BlockPos pos)
    {
        BlockState state = this.world().getBlockState(pos);
        if (!state.isAir())
        {
            state.onBlockExploded(this.world(), pos, asVanillaExplosion());
        }
    }

}
