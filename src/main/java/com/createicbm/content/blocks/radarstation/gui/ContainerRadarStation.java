package com.createicbm.content.blocks.radarstation.gui;

import com.createicbm.content.blocks.radarstation.TileRadarStation;
import com.createicbm.content.reg.MenuReg;
import com.createicbm.prefab.gui.ContainerBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/26/2018.
 */
public class ContainerRadarStation extends ContainerBase<TileRadarStation>
{
    public ContainerRadarStation(int windowId, Player player, TileRadarStation node)
    {
        super(MenuReg.RADAR_STATION.get(), windowId, player, node);

        addPlayerInventory(player, 7, 102);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        // No machine slots, the battery slot was removed with Forge Energy
        return ItemStack.EMPTY;
    }
}
