package com.createicbm.content.blocks.launcher.base;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.createicbm.lib.kinetic.KineticBlockHooks;
import com.createicbm.content.blocks.launcher.PendingLauncherChunkLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import com.createicbm.ICBMConstants;
import com.createicbm.content.blocks.launcher.network.ILauncherComponent;
import com.createicbm.content.blocks.launcher.network.LauncherNetwork;
import com.createicbm.content.reg.BlockReg;
import com.createicbm.lib.InventoryUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/8/2018.
 */
public class BlockLauncherBase extends BaseEntityBlock implements IRotate
{
    public static final MapCodec<BlockLauncherBase> CODEC = simpleCodec(p -> new BlockLauncherBase());

    public static final DirectionProperty ROTATION_PROP = DirectionProperty.create("facing");

    public BlockLauncherBase()
    {
        super(Properties.of().mapColor(MapColor.METAL).strength(10f, 2f).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any().setValue(ROTATION_PROP, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "launcherbase";
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit)
    {
        final BlockEntity tile = worldIn.getBlockEntity(pos);
        if (tile instanceof TileLauncherBase && !worldIn.isClientSide())
        {
            if (heldItem.getItem() == Items.STONE_AXE)
            {
                final LauncherNetwork network = ((TileLauncherBase) tile).getNetworkNode().getNetwork();
                playerIn.sendSystemMessage(Component.literal("Network: " + network));
                playerIn.sendSystemMessage(Component.literal("L: " + (network != null ? network.getLaunchers().size() : 0)));
                return ItemInteractionResult.SUCCESS;
            }
            if (!((TileLauncherBase) tile).tryInsertMissile(playerIn, hand, heldItem))
            {
                ((TileLauncherBase) tile).openGui(playerIn);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (!state.is(newState.getBlock()))
        {
            PendingLauncherChunkLoader.removeAt(world, pos);
            final BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof ILauncherComponent)
            {
                ((ILauncherComponent) tile).getNetworkNode().onTileRemoved();
            }
            InventoryUtility.dropInventory(world, pos);
        }
        KineticBlockHooks.onRemove(state, world, pos, newState);
    }

    // ---- Create kinetics: the shaft enters on the side opposite the launch direction ----

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face)
    {
        return face == state.getValue(ROTATION_PROP).getOpposite();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state)
    {
        return state.getValue(ROTATION_PROP).getAxis();
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace)
    {
        // The wrench would turn the pad without re-linking its shaft, the pad is aimed by placing it instead
        return originalState;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston)
    {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        KineticBlockHooks.onPlace(state, level, pos, oldState);
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

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TileLauncherBase(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return createTickerHelper(type, BlockReg.TILE_LAUNCHER_BASE.get(), (l, p, s, tile) -> tile.tick());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(ROTATION_PROP);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return defaultBlockState().setValue(ROTATION_PROP, context.getClickedFace());
    }
}
