package com.createicbm.prefab.tile;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.createicbm.lib.InventoryUtility;
import com.createicbm.lib.kinetic.KineticBlockHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * ICBM machine block that runs on Create rotation: a shaft or cogwheel connects to its bottom face and the block
 * entity ({@link TileKineticMachine}) joins that kinetic network.
 */
public abstract class BlockKineticICBM extends BlockICBM implements IRotate
{
    public BlockKineticICBM(String name, Properties properties)
    {
        super(name, properties);
    }

    public BlockKineticICBM(String name)
    {
        super(name);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face)
    {
        return face == Direction.DOWN;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state)
    {
        return Direction.Axis.Y;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston)
    {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        KineticBlockHooks.onPlace(state, level, pos, oldState);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (dropInventory && !state.is(newState.getBlock()))
        {
            InventoryUtility.dropInventory(world, pos);
        }
        KineticBlockHooks.onRemove(state, world, pos, newState);
    }

    @Override
    public void updateIndirectNeighbourShapes(BlockState state, LevelAccessor level, BlockPos pos, int flags, int recursionLeft)
    {
        KineticBlockHooks.updateIndirectNeighbourShapes(level, pos);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        KineticBlockHooks.setPlacedBy(level, pos);
    }
}
