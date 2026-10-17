package com.createicbm.content.blast;

import com.createicbm.api.explosion.IBlastTickable;
import com.createicbm.config.util.BlockReplacementData;
import com.createicbm.content.blast.threaded.BlastThreaded;
import com.createicbm.content.radioactive.RadioactiveHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

/**
 * Handles switching out blocks for dead version or with radioactive versions
 */
public class BlastRadioactiveBlockSwaps extends BlastThreaded implements IBlastTickable
{
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
        }
    }
}
