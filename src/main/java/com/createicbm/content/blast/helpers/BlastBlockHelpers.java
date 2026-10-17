package com.createicbm.content.blast.helpers;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created by Dark(DarkGuardsman, Robin) on 5/23/2020.
 */
public final class BlastBlockHelpers
{
    private BlastBlockHelpers()
    {
        //Private and empty to prevent creation
    }

    /**
     * Checks if a block state is a fluid
     *
     * @param blockState to check
     * @return true if the block state is a fluid
     */
    public static boolean isFluid(BlockState blockState)
    {
        return blockState.getBlock() instanceof LiquidBlock;
    }

    /**
     * Checks if the blocks state is a flowing fluid
     *
     * @param blockState to check
     * @return true if the block state is a flowing fluid
     */
    public static boolean isFlowingWater(BlockState blockState)
    {
        return blockState.getBlock() instanceof LiquidBlock && blockState.getValue(LiquidBlock.LEVEL) < 7;
    }

    public static boolean isWaterMaterial(BlockState state)
    {
        return state.getBlock() instanceof LiquidBlock && state.getFluidState().is(FluidTags.WATER);
    }

    public static boolean isPlantMaterial(BlockState state)
    {
        return state.is(BlockTags.LEAVES) || state.getBlock() instanceof BushBlock || state.getBlock() instanceof VineBlock;
    }

    public static boolean isRockMaterial(BlockState state)
    {
        return state.instrument() == NoteBlockInstrument.BASEDRUM && state.requiresCorrectToolForDrops();
    }

    public static boolean isSandMaterial(BlockState state)
    {
        return state.is(BlockTags.SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.SOUL_SAND);
    }

    public static boolean isGroundMaterial(BlockState state)
    {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND) || state.is(Blocks.DIRT_PATH);
    }
}
