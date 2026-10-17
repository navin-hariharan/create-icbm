package com.createicbm.lib;

import com.createicbm.jlib.data.vector.IPos3D;
import com.createicbm.lib.capability.holder.ICapabilityHolderTile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Series of helper classes for dealing with any kind of inventory
 *
 * @author Calclavia, DarkCow(aka Darkguardsman, Robin)
 */
public class InventoryUtility
{
    public static ItemEntity dropItemStack(Level world, IPos3D position, ItemStack itemStack, int delay)
    {
        return dropItemStack(world, position, itemStack, delay, 0f);
    }

    public static ItemEntity dropItemStack(Level world, IPos3D position, ItemStack itemStack, int delay, float randomAmount)
    {
        return dropItemStack(world, position.x(), position.y(), position.z(), itemStack, delay, randomAmount);
    }

    public static void dropInventory(Level world, BlockPos pos) {
        dropInventory(world, pos, null);
    }

    /**
     * Drops the contents of the block entity's item handler at the position.
     *
     * @param world    containing the block
     * @param pos      of the block
     * @param oldState state of the block being removed, null to use the block entity's cached state
     */
    public static void dropInventory(Level world, BlockPos pos, @Nullable BlockState oldState) {
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile != null)
        {
            final IItemHandler handler;
            if (tile instanceof ICapabilityHolderTile holder)
            {
                handler = holder.getCapability(Capabilities.ItemHandler.BLOCK, null);
            }
            else
            {
                final BlockState state = oldState != null ? oldState : tile.getBlockState();
                handler = world.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, tile, null);
            }
            if(handler != null) {

                final double x = pos.getX() + 0.5;
                final double y = pos.getY() + 0.5;
                final double z = pos.getZ() + 0.5;

                for(int slot = 0; slot < handler.getSlots(); slot++) {
                    final ItemStack stack = handler.getStackInSlot(slot);
                    if(handler instanceof IItemHandlerModifiable) {
                        ((IItemHandlerModifiable) handler).setStackInSlot(slot, ItemStack.EMPTY);
                    }
                    dropItemStack(world, x, y, z, stack, 0, 0);
                }
            }
        }
    }

    public static ItemEntity dropItemStack(Level world, double x, double y, double z, ItemStack itemStack, int delay, float randomAmount)
    {
        //TODO fire drop events if not already done by forge
        //TODO add banned item filtering, prevent creative mode only items from being dropped
        if (world != null && !world.isClientSide() && !itemStack.isEmpty())
        {
            double randomX = 0;
            double randomY = 0;
            double randomZ = 0;

            if (randomAmount > 0)
            {
                randomX = world.random.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
                randomY = world.random.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
                randomZ = world.random.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
            }

            ItemEntity entityitem = new ItemEntity(world, x + randomX, y + randomY, z + randomZ, itemStack);

            if (randomAmount <= 0)
            {
                entityitem.setDeltaMovement(Vec3.ZERO);
            }

            entityitem.setPickUpDelay(delay);
            world.addFreshEntity(entityitem);
            return entityitem;
        }
        return null;
    }

    /**
     * Checks if the two item stacks match each other exactly. Item, meta, stacksize, nbt
     *
     * @param stackA - item stack a
     * @param stackB - item stack a
     * @return true if they match
     */
    public static boolean stacksMatchExact(ItemStack stackA, ItemStack stackB)
    {
        if (!stackA.isEmpty() && !stackB.isEmpty())
        {
            return ItemStack.isSameItem(stackA, stackB) && doesStackNBTMatch(stackA, stackB) && stackA.getCount() == stackB.getCount();
        }
        return stackA.isEmpty() && stackB.isEmpty();
    }

    /**
     * Checks if two itemStack's nbt matches exactly. Does not check item, stacksize, or damage value.
     *
     * @param stackA - item stack a, can't be null
     * @param stackB - item stack a, can't be null
     * @return true if the stack's nbt matches
     */
    public static boolean doesStackNBTMatch(ItemStack stackA, ItemStack stackB)
    {
        return Objects.equals(stackA.getComponentsPatch(), stackB.getComponentsPatch());
    }
}
