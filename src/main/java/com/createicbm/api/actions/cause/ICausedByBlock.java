package com.createicbm.api.actions.cause;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Cause containing block information
 */
public interface ICausedByBlock extends IActionCause {
    Level getWorld();

    BlockPos getBlockPos();

    BlockState getBlockState();
}
