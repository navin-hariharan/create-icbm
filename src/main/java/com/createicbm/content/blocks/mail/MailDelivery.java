package com.createicbm.content.blocks.mail;

import com.createicbm.content.reg.BlockReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Places {@link BlockMail} for delivery missiles
 */
public final class MailDelivery
{
    /** Blocks the mail may climb to get out of whatever the missile hit */
    private static final int MAX_CLIMB = 16;

    private MailDelivery()
    {
    }

    /**
     * Packs the stack into a parcel near the position, on the ground below it
     *
     * @param start  where the missile came down
     * @param facing side the label reads from
     * @param stack  items to deliver
     * @param sender player who sent them, null if unknown
     * @return true if the mail was placed, false if there was no room (the caller should drop the items instead)
     */
    public static boolean deliver(Level level, BlockPos start, Direction facing, ItemStack stack, @Nullable LauncherOwner sender)
    {
        if (stack.isEmpty())
        {
            return true;
        }
        final BlockPos pos = findSpot(level, start);
        if (pos == null)
        {
            return false;
        }
        final BlockState state = BlockReg.blockMail.get().defaultBlockState().setValue(BlockMail.FACING, facing);
        if (!level.setBlock(pos, state, Block.UPDATE_ALL))
        {
            return false;
        }
        if (level.getBlockEntity(pos) instanceof TileMail mail)
        {
            mail.fill(stack, sender);
            return true;
        }
        level.removeBlock(pos, false);
        return false;
    }

    /**
     * Climbs out of solid blocks and fluids, then drops down until there is ground below
     */
    @Nullable
    private static BlockPos findSpot(Level level, BlockPos start)
    {
        // Missiles are often shot down above the build limit
        final int bottom = level.getMinBuildHeight();
        final int top = level.getMaxBuildHeight() - 1;
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(start.getX(), Mth.clamp(start.getY(), bottom, top), start.getZ());

        // Fluids don't count towards the climb, a lava sea can be deeper than that
        int climbed = 0;
        while (!canPlaceAt(level, pos) && pos.getY() < top && (climbed < MAX_CLIMB || isFluid(level, pos)))
        {
            if (!isFluid(level, pos))
            {
                climbed++;
            }
            pos.move(Direction.UP);
        }
        if (!canPlaceAt(level, pos))
        {
            return null;
        }

        final int dropFrom = pos.getY();
        while (pos.getY() > bottom && canPlaceAt(level, pos.below()))
        {
            pos.move(Direction.DOWN);
        }
        // Nothing to land on (the void in the End), stay where the missile came down rather than at the world floor
        if (pos.getY() == bottom && canPlaceAt(level, pos) && !level.getBlockState(pos.below()).isSolid())
        {
            pos.setY(dropFrom);
        }
        return pos.immutable();
    }

    private static boolean isFluid(Level level, BlockPos pos)
    {
        return level.isLoaded(pos) && !level.getFluidState(pos).isEmpty();
    }

    private static boolean canPlaceAt(Level level, BlockPos pos)
    {
        if (!level.isInWorldBounds(pos) || !level.isLoaded(pos))
        {
            return false;
        }
        final BlockState state = level.getBlockState(pos);
        return state.canBeReplaced() && !state.getFluidState().is(FluidTags.LAVA);
    }

    /** Sender for messages and the label, "Unknown sender" if there is none */
    public static Component senderName(TileMail mail)
    {
        final String name = mail.getSenderName();
        return name.isEmpty() ? Component.translatable("info.create_icbm:mail.unknown_sender") : Component.literal(name);
    }
}
