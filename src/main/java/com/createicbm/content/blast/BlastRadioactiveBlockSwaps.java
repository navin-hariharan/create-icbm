package com.createicbm.content.blast;

import com.createicbm.api.explosion.IBlastTickable;
import com.createicbm.config.util.BlockReplacementData;
import com.createicbm.content.blast.threaded.BlastThreaded;
import com.createicbm.content.radioactive.RadioactiveHandler;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

/**
 * Handles switching out blocks for dead version or with radioactive versions
 */
public class BlastRadioactiveBlockSwaps extends BlastThreaded implements IBlastTickable
{
    private long replacementApplications = 0;

    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits)
    {
        BlastHelpers.forEachPosInRadius(this.getBlastRadius(), (x, y, z) ->
        edits.accept(new BlockPos(xi() + x, yi() + y, zi() + z)));
        //TODO implement raytrace to reduce impact of effects behind protective shielding
        //TODO only contaminate top few layers of earth
        return false;
    }

    @Override
    public void destroyBlock(BlockPos targetPosition)
    {
        final BlockState blockState = world.getBlockState(targetPosition);
        final BlockReplacementData replacement = RadioactiveHandler.radioactiveBlockSwaps.getValue(blockState);
        if(replacement != null) {
            replacement.apply(world, targetPosition);
            if (ICBMDiagnostics.enabled())
            {
                replacementApplications++;
            }
        }
    }

    @Override
    protected void onBlastCompleted()
    {
        if (ICBMDiagnostics.enabled())
        {
            ICBMDiagnostics.info("RADIOACTIVE_BLOCK_SWAP_COMPUTE_COMPLETE dim={} center=({}, {}, {}) radius={} replacementsAppliedSoFar={} tick={} thread={}",
                world == null ? "unknown" : world.dimension().location(), x(), y(), z(), getBlastRadius(), replacementApplications,
                world instanceof net.minecraft.server.level.ServerLevel level ? level.getServer().getTickCount() : -1, Thread.currentThread().getName());
        }
        super.onBlastCompleted();
    }

    @Override
    protected void onWorldEditsCompleted(int candidatePositions, double elapsedMs)
    {
        if (ICBMDiagnostics.enabled())
        {
            ICBMDiagnostics.info("RADIOACTIVE_BLOCK_SWAP_FINISHED dim={} center=({}, {}, {}) radius={} candidatePositions={} replacementApplications={} elapsedMs={} tick={} thread={}",
                world == null ? "unknown" : world.dimension().location(), x(), y(), z(), getBlastRadius(), candidatePositions, replacementApplications,
                elapsedMs, world instanceof net.minecraft.server.level.ServerLevel level ? level.getServer().getTickCount() : -1, Thread.currentThread().getName());
        }
    }

    @Override
    protected boolean willEditBlock(BlockPos pos, BlockState state)
    {
        return RadioactiveHandler.radioactiveBlockSwaps.getValue(state) != null;
    }

    /** Not an explosion, so no distant boom for people who can see it */
    @Override
    protected boolean playsDistantSound()
    {
        return false;
    }
}
