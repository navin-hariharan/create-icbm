package com.createicbm.content.blocks.mail;

import com.createicbm.ICBMConstants;
import com.createicbm.content.reg.BlockReg;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Parcel a delivery missile leaves where it lands. Slab sized, holds one stack and carries a label with the
 * sender's name. Opening it hands the items to the player and the parcel is gone. It can't be broken, crafted or
 * picked, and has no item, so it only ever comes from a missile.
 */
public class BlockMail extends BaseEntityBlock
{
    public static final MapCodec<BlockMail> CODEC = simpleCodec(p -> new BlockMail());

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    public BlockMail()
    {
        super(Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(-1.0F, 3600000.0F)
            .sound(SoundType.WOOL)
            .noLootTable()
            .pushReaction(PushReaction.BLOCK));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "mail";
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation)
    {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TileMail(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return level.isClientSide() ? null : createTickerHelper(type, BlockReg.TILE_MAIL.get(), TileMail::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (level.isClientSide())
        {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof TileMail mail)
        {
            final ItemStack before = mail.getContents().copy();
            final boolean empty = mail.collect(player);
            final int taken = before.getCount() - mail.getContents().getCount();

            if (taken > 0)
            {
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 1.0F + level.random.nextFloat() * 0.4F);
                // One message, a second one would replace this on the action bar
                final String key = empty ? "info.create_icbm:mail.collected" : "info.create_icbm:mail.collected_partial";
                player.displayClientMessage(Component.translatable(key, taken, before.getHoverName(), MailDelivery.senderName(mail)), true);
            }

            if (empty)
            {
                // Break effect (sound and cardboard bits) without dropping anything
                level.levelEvent(2001, pos, Block.getId(state));
                if (level instanceof ServerLevel server)
                {
                    server.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, 6, 0.25, 0.1, 0.25, 0.01);
                }
                level.removeBlock(pos, false);
            }
            else if (taken == 0)
            {
                player.displayClientMessage(Component.translatable("info.create_icbm:mail.inventory_full"), true);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston)
    {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !oldState.is(this))
        {
            // Lets the missile fill it first, see tick
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        // Mail no missile delivered (placed with commands) is removed again
        if (!(level.getBlockEntity(pos) instanceof TileMail mail) || !mail.isDelivered())
        {
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (!state.is(newState.getBlock()))
        {
            UnopenedMail.remove(level, pos);
        }
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TileMail mail)
        {
            // Only reached if something other than opening it removes the mail (creative mode, commands), keep the
            // items rather than deleting them
            final ItemStack left = mail.takeContents();
            if (!left.isEmpty())
            {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, left);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state)
    {
        return ItemStack.EMPTY;
    }
}
