package com.createicbm.prefab.tile;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.InventoryUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;

/**
 * Prefab for ICBM machine blocks with a block entity and a 6 direction rotation.
 */
public abstract class BlockICBM extends BaseEntityBlock
{
    public static final DirectionProperty ROTATION_PROP = DirectionProperty.create("rotation");

    protected boolean dropInventory = false;

    private final String descriptionId;

    public BlockICBM(String name, Properties properties)
    {
        super(properties);
        this.descriptionId = "tile." + ICBMConstants.PREFIX + name.toLowerCase();
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION_PROP, Direction.NORTH));
    }

    public BlockICBM(String name)
    {
        this(name, machineProperties());
    }

    public static Properties machineProperties()
    {
        return Properties.of().mapColor(MapColor.METAL).strength(10f, 2f).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    @Override
    public String getDescriptionId()
    {
        return descriptionId;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(ROTATION_PROP);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return defaultBlockState().setValue(ROTATION_PROP, context.getHorizontalDirection());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (dropInventory && !state.is(newState.getBlock()))
        {
            InventoryUtility.dropInventory(world, pos);
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }
}
