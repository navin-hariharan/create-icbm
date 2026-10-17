package com.createicbm.lib.kinetic;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.createicbm.prefab.tile.TileKineticMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The block side of Create's kinetic blocks, for ICBM machine blocks that keep their own block class instead of
 * extending Create's KineticBlock. Each method mirrors the same named KineticBlock override, call it from the block.
 */
public final class KineticBlockHooks
{
    private KineticBlockHooks()
    {
    }

    /** KineticBlock#onPlace: skip a full network rebuild when only a non kinetic property changed */
    public static void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState)
    {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof KineticBlockEntity kinetic)
        {
            kinetic.preventSpeedUpdate = 0;

            if (oldState.getBlock() != state.getBlock())
                return;
            if (state.hasBlockEntity() != oldState.hasBlockEntity())
                return;
            if (!sameShaft(oldState, state))
                return;

            kinetic.preventSpeedUpdate = 2;
        }
    }

    /**
     * KineticBlock#onRemove: lets Create detach the block entity from its network. Call after dropping inventories,
     * in place of the super call.
     */
    public static void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState)
    {
        IBE.onRemove(state, level, pos, newState);
    }

    /** KineticBlock#updateIndirectNeighbourShapes: a neighbour changed, so recheck the connections */
    public static void updateIndirectNeighbourShapes(LevelAccessor level, BlockPos pos)
    {
        if (level.isClientSide())
            return;

        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof KineticBlockEntity kinetic))
            return;
        if (kinetic.preventSpeedUpdate > 0)
            return;

        // Remove previous information when block is added
        kinetic.warnOfMovement();
        kinetic.clearKineticInformation();
        kinetic.updateSpeed = true;
    }

    /** KineticBlock#setPlacedBy: show Create's rotation indicator particles */
    public static void setPlacedBy(Level level, BlockPos pos)
    {
        if (level.isClientSide)
            return;

        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileKineticMachine machine)
        {
            machine.queueRotationIndicators();
        }
    }

    private static boolean sameShaft(BlockState oldState, BlockState newState)
    {
        if (newState.getBlock() instanceof com.simibubi.create.content.kinetics.base.IRotate rotate)
        {
            return rotate.getRotationAxis(oldState) == rotate.getRotationAxis(newState);
        }
        return true;
    }
}
