package com.createicbm.content.blocks.launcher.base.gui;

import com.createicbm.content.blocks.launcher.base.TileLauncherBase;
import com.createicbm.content.reg.MenuReg;
import com.createicbm.prefab.gui.ContainerBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/27/2018.
 */
public class ContainerLaunchBase extends ContainerBase<TileLauncherBase>
{
    public ContainerLaunchBase(int windowId, Player player, TileLauncherBase tileEntity)
    {
        super(MenuReg.LAUNCHER_BASE.get(), windowId, player, tileEntity);

        // Missile Slot
        this.addSlot(new SlotItemHandler(tileEntity.inventory, 0, 88, 47));

        addPlayerInventory(player, 7, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        return transferStackInSlot(player, slotIndex, 1, 1);
    }
}
