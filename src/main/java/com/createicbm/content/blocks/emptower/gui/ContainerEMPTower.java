package com.createicbm.content.blocks.emptower.gui;

import com.createicbm.content.blocks.emptower.TileEMPTower;
import com.createicbm.content.reg.MenuReg;
import com.createicbm.prefab.gui.ContainerBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/25/2018.
 */
public class ContainerEMPTower extends ContainerBase<TileEMPTower>
{
    public ContainerEMPTower(int windowId, Player player, TileEMPTower tileEMPTower)
    {
        super(MenuReg.EMP_TOWER.get(), windowId, player, tileEMPTower);
        addPlayerInventory(player, 7, 84);
    }

    /** Called to transfer a stack from one inventory to the other eg. when shift clicking. */
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        // No machine slots, the battery slot was removed with Forge Energy
        return ItemStack.EMPTY;
    }
}
