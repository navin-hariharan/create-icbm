package com.createicbm.content.blocks.launcher.screen.gui;

import com.createicbm.content.blocks.launcher.screen.TileLauncherScreen;
import com.createicbm.content.reg.MenuReg;
import com.createicbm.prefab.gui.ContainerBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/27/2018.
 */
public class ContainerLaunchScreen extends ContainerBase<TileLauncherScreen>
{
    public ContainerLaunchScreen(int windowId, Player player, TileLauncherScreen node)
    {
        super(MenuReg.LAUNCHER_SCREEN.get(), windowId, player, node);

        addPlayerInventory(player, 7, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        // No machine slots, the battery slot was removed with Forge Energy
        return ItemStack.EMPTY;
    }
}
