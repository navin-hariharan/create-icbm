package com.createicbm.content.blocks.emptower;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.createicbm.lib.kinetic.KineticBlockHooks;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;

import com.mojang.serialization.MapCodec;
import com.createicbm.ICBMConstants;
import com.createicbm.content.reg.BlockReg;
import com.createicbm.content.reg.ItemReg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/23/2018.
 * COIL and ELECTRIC are render only states used by {@link TESREmpTower}; the ELECTRIC model should declare
 * "render_type": "translucent".
 */
public class BlockEmpTower extends BaseEntityBlock implements IRotate
{
    public static final MapCodec<BlockEmpTower> CODEC = simpleCodec(p -> new BlockEmpTower());

    public static final PropertyTowerStates TOWER_MODELS = new PropertyTowerStates();
    public static BlockState COIL;
    public static BlockState ELECTRIC;

    public BlockEmpTower()
    {
        super(Properties.of().mapColor(MapColor.METAL).strength(10f, 2f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(TOWER_MODELS, PropertyTowerStates.EnumTowerTypes.BASE));

        COIL = defaultBlockState().setValue(TOWER_MODELS, PropertyTowerStates.EnumTowerTypes.COIL);
        ELECTRIC = defaultBlockState().setValue(TOWER_MODELS, PropertyTowerStates.EnumTowerTypes.ELECTRIC);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "emptower.0";
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(TOWER_MODELS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        // Extender state is applied by ItemBlockEmpTower
        return defaultBlockState();
    }

    public static boolean isExtender(BlockState state)
    {
        return state.getValue(TOWER_MODELS) == PropertyTowerStates.EnumTowerTypes.SPIN;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state)
    {
        return new ItemStack(isExtender(state) ? ItemReg.itemEmpTowerExtender.get() : ItemReg.itemEmpTower.get());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit)
    {
        if (!worldIn.isClientSide())
        {
            final BlockEntity tile = worldIn.getBlockEntity(pos);
            if (tile instanceof TileEMPTower)
            {
                ((TileEMPTower) tile).openGui(playerIn);
            }
            else if (tile instanceof TileEmpTowerFake && ((TileEmpTowerFake) tile).getHost() != null)
            {
                ((TileEmpTowerFake) tile).getHost().openGui(playerIn);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state)
    {
        return false;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos)
    {
        return 0; //TODO output charge amount
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
        if (isExtender(state))
        {
            return new TileEmpTowerFake(pos, state);
        }
        return new TileEMPTower(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return createTickerHelper(type, BlockReg.TILE_EMP_TOWER.get(), (l, p, s, tile) -> tile.tick());
    }

    // ---- Create kinetics: the base tower takes a shaft from below, extenders only carry the coil ----

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face)
    {
        return face == Direction.DOWN && !isExtender(state);
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
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        KineticBlockHooks.onRemove(state, level, pos, newState);
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
