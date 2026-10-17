package com.createicbm.content.missile.entity.delivery.gui;

import com.createicbm.ICBMConstants;
import com.createicbm.prefab.gui.GuiContainerBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for {@link ContainerDeliveryMissile}: the cargo slot between the brass clamps, and the player inventory.
 */
public class GuiDeliveryMissile extends GuiContainerBase<ContainerDeliveryMissile>
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_delivery_missile.png");

    public GuiDeliveryMissile(ContainerDeliveryMissile container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        imageHeight = 133;
    }

    @Override
    public ResourceLocation getBackground()
    {
        return TEXTURE;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        graphics.drawString(font, title, 30, 6, 0xAAAAAA, false);
        super.renderLabels(graphics, mouseX, mouseY);
    }
}
