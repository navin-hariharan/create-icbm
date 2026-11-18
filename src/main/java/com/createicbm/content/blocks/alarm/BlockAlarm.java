package com.createicbm.content.blocks.alarm;

import com.createicbm.ICBMConstants;
import com.createicbm.content.reg.BlockReg;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Siren with a warning light. Fixed to floors, walls and ceilings like a lever, it wails while it gets a redstone
 * signal or while a radar on its radio channel reports a missile coming in. It needs no rotational force and gives off
 * no redstone signal.
 */
public class BlockAlarm extends FaceAttachedHorizontalDirectionalBlock implements EntityBlock
{
    public static final MapCodec<BlockAlarm> CODEC = simpleCodec(p -> new BlockAlarm());

    /** True while the siren sounds */
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    /** Parts of the model standing on a floor: across from, across to, height from, height to */
    private static final double[][] PARTS = {{3, 13, 0, 2}, {4, 12, 2, 6}, {5, 11, 6, 11}};

    /** Shape for each direction the alarm points away from the block it is fixed to */
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static
    {
        for (Direction out : Direction.values())
        {
            VoxelShape shape = Shapes.empty();
            for (double[] part : PARTS)
            {
                shape = Shapes.or(shape, part(part[0], part[1], part[2], part[3], out));
            }
            SHAPES.put(out, shape.optimize());
        }
    }

    public BlockAlarm()
    {
        super(Properties.of()
            .mapColor(MapColor.GOLD)
            .strength(1.5F, 6.0F)
            .sound(SoundType.COPPER)
            .noOcclusion()
            .pushReaction(PushReaction.DESTROY)
            .lightLevel(state -> state.getValue(ACTIVE) ? 7 : 0));
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(FACE, AttachFace.WALL)
            .setValue(ACTIVE, false));
    }

    /** One part of the model, standing out of the face it is fixed to towards {@code out} */
    private static VoxelShape part(double lo, double hi, double from, double to, Direction out)
    {
        return switch (out)
        {
            case UP -> Block.box(lo, from, lo, hi, to, hi);
            case DOWN -> Block.box(lo, 16 - to, lo, hi, 16 - from, hi);
            case NORTH -> Block.box(lo, lo, 16 - to, hi, hi, 16 - from);
            case SOUTH -> Block.box(lo, lo, from, hi, hi, to);
            case WEST -> Block.box(16 - to, lo, lo, 16 - from, hi, hi);
            case EAST -> Block.box(from, lo, lo, to, hi, hi);
        };
    }

    @Override
    public MapCodec<BlockAlarm> codec()
    {
        return CODEC;
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "alarm";
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACE, FACING, ACTIVE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPES.get(getConnectedDirection(state));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston)
    {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !oldState.is(this))
        {
            // Next tick: an alarm moved by a contraption or a ship gets its saved radio data after it is placed
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston)
    {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide())
        {
            refresh(level, pos);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        refresh(level, pos);
        if (level.getBlockEntity(pos) instanceof TileAlarm alarm)
        {
            // Radars keep reporting a missile while they see it, the alarm stops a while after the last report
            final long left = alarm.alertTicksLeft();
            if (left > 0)
            {
                level.scheduleTick(pos, this, (int) Math.min(left + 1, 20 * 60));
            }
        }
    }

    /**
     * Sounds the alarm while it has a redstone signal or a radar alert, silences it otherwise. Server only.
     */
    public static void refresh(Level level, BlockPos pos)
    {
        final BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockAlarm))
        {
            return;
        }
        final boolean active = level.hasNeighborSignal(pos)
            || level.getBlockEntity(pos) instanceof TileAlarm alarm && alarm.alertTicksLeft() > 0;
        if (state.getValue(ACTIVE) != active)
        {
            level.setBlock(pos, state.setValue(ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }

    /** Opens the alarm's screen to set the radio channel of the radars it listens to */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileAlarm alarm)
        {
            alarm.openGui(player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TileAlarm(pos, state);
    }

    /** Only a sounding alarm ticks, and only on the client, to play its siren */
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return level.isClientSide() && state.getValue(ACTIVE) && type == BlockReg.TILE_ALARM.get()
            ? (BlockEntityTicker<T>) (BlockEntityTicker<TileAlarm>) TileAlarm::clientTick
            : null;
    }
}
