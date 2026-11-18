package com.createicbm.content.blocks.alarm.gui;

import com.createicbm.content.blocks.alarm.TileAlarm;
import com.createicbm.content.reg.MenuReg;
import com.createicbm.prefab.gui.ContainerBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Menu of the alarm's screen. It has no slots, it only lets the player set the radio channel.
 */
public class ContainerAlarm extends ContainerBase<TileAlarm>
{
    public ContainerAlarm(int windowId, Player player, TileAlarm alarm)
    {
        super(MenuReg.ALARM.get(), windowId, player, alarm);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        return ItemStack.EMPTY;
    }
}
