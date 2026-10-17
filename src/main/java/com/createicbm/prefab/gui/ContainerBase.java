package com.createicbm.prefab.gui;

import com.createicbm.content.reg.MenuReg;
import lombok.Getter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

/**
 * Base menu for ICBM machines.
 */
public class ContainerBase<H extends Object> extends AbstractContainerMenu
{
    protected int slotCount = 0;

    protected Player player;

    @Getter
    protected H host;

    public ContainerBase(@Nullable MenuType<?> type, int windowId, Player player, H node)
    {
        super(type, windowId);
        this.player = player;
        this.host = node;
        if (node instanceof IPlayerUsing)
        {
            ((IPlayerUsing) node).addPlayerToUseList(player);
        }
    }

    @Override
    public void removed(Player entityplayer)
    {
        if (host instanceof IPlayerUsing)
        {
            ((IPlayerUsing) host).removePlayerToUseList(entityplayer);
        }
        super.removed(entityplayer);
    }

    public void addPlayerInventory(Player player)
    {
        addPlayerInventory(player, 8, 84);
    }

    public void addPlayerInventory(Player player, int x, int y)
    {
        //Inventory
        for (int row = 0; row < 3; ++row)
        {
            for (int slot = 0; slot < 9; ++slot)
            {
                this.addSlot(new Slot(player.getInventory(), slot + row * 9 + 9, slot * 18 + x, row * 18 + y));
            }
        }

        //Hot bar
        for (int slot = 0; slot < 9; ++slot)
        {
            this.addSlot(new Slot(player.getInventory(), slot, slot * 18 + x, 58 + y));
        }
    }

    @Override
    public boolean stillValid(Player entityplayer)
    {
        if (this.host instanceof BlockEntity)
        {
            return MenuReg.stillValid(this, (BlockEntity) this.host, entityplayer);
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        return ItemStack.EMPTY;
    }

    /**
     * @param player            moving the stack
     * @param slotIndex         clicked
     * @param machineSlots      number of machine slots, player inventory follows them
     * @return copy of the moved stack or empty
     */
    protected ItemStack transferStackInSlot(Player player, int slotIndex, int machineSlots, int firstPlayerSlotCheck)
    {
        ItemStack targetItemStackCopy = null;
        final Slot targetSlot = this.slots.get(slotIndex);

        if (targetSlot != null && targetSlot.hasItem())
        {
            final ItemStack targetItemStack = targetSlot.getItem();
            targetItemStackCopy = targetItemStack.copy();

            if (slotIndex >= firstPlayerSlotCheck)
            {
                for (int i = 0; i < machineSlots; i++)
                {
                    if (this.getSlot(i).mayPlace(targetItemStack))
                    {
                        if (!this.moveItemStackTo(targetItemStack, i, i + 1, false))
                        {
                            return ItemStack.EMPTY;
                        }
                        break;
                    }
                }
            }
            else if (!this.moveItemStackTo(targetItemStack, machineSlots, 36 + machineSlots, false))
            {
                return ItemStack.EMPTY;
            }

            if (targetItemStack.getCount() == 0)
            {
                targetSlot.set(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            }
            else
            {
                targetSlot.setChanged();
            }

            if (targetItemStack.getCount() == targetItemStackCopy.getCount())
            {
                return ItemStack.EMPTY;
            }

            targetSlot.onTake(player, targetItemStack);
        }

        if (targetItemStackCopy == null)
        {
            return ItemStack.EMPTY;
        }
        return targetItemStackCopy;
    }
}
