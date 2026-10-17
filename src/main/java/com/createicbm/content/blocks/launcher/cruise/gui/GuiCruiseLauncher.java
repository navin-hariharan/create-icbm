package com.createicbm.content.blocks.launcher.cruise.gui;

import com.createicbm.ICBMConstants;
import com.createicbm.content.blocks.launcher.LauncherLangs;
import com.createicbm.content.blocks.launcher.cruise.TileCruiseLauncher;
import com.createicbm.content.missile.logic.targeting.BasicTargetData;
import com.createicbm.lib.LanguageUtility;
import com.createicbm.prefab.gui.GuiContainerBase;
import com.createicbm.prefab.gui.TextInput;
import com.createicbm.prefab.gui.button.DisableButton;
import com.createicbm.prefab.gui.components.SlotEnergyBar;
import com.createicbm.prefab.gui.tooltip.TooltipTranslations;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiCruiseLauncher extends GuiContainerBase<ContainerCruiseLauncher>
{
    // Localizations
    private static final String LANG_KEY = "gui.launcher.cruise";
    private static final String GUI_NAME = LANG_KEY + ".name";

    // Texture
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_cruise_launcher.png");

    // Launcher
    private final TileCruiseLauncher tileEntity;

    public GuiCruiseLauncher(ContainerCruiseLauncher container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.tileEntity = container.getHost();
    }

    @Override
    public ResourceLocation getBackground() {
        return TEXTURE;
    }

    @Override
    protected void init()
    {
        super.init();

        int componentID = 0;

        // Target field
        addComponent(TextInput.vec3dField(componentID++, font, 18, 17, 100, 12,
            tileEntity::getTarget, tileEntity::setTarget, (o) -> TileCruiseLauncher.PACKET_TARGET.sendToServer(tileEntity)));
        addComponent(TextInput.textField(componentID++, font, 135, 17, 34, 12,
            tileEntity.radio::getChannel, tileEntity.radio::setChannel, (o) -> TileCruiseLauncher.PACKET_RADIO_HZ.sendToServer(tileEntity)));

        // Launch button
        addComponent(new LaunchButton(0, leftPos + 24, topPos + 38)
            .doDrawDisabledGlass()
            .setTooltip(() -> this.tileEntity.getLauncher().preCheckLaunch(new BasicTargetData(tileEntity.getTarget()), null).message())
            .setAction(() -> TileCruiseLauncher.PACKET_LAUNCH.sendToServer(tileEntity))
            .setEnabledCheck(() -> !tileEntity.getLauncher().preCheckLaunch(new BasicTargetData(tileEntity.getTarget()), null).isBlocking()));

        addComponent(new SlotEnergyBar(141, 66,
            tileEntity.energyStorage::getEnergyStored,
            tileEntity.energyStorage::getMaxEnergyStored)
            .withActionCost(tileEntity::getFiringCost)
        );

        // Radio tooltip
        addComponent(new TooltipTranslations(119, 16, 14, 14, LauncherLangs.TRANSLATION_TOOLTIP_RADIO).withDelay(1));
        addComponent(new DisableButton(1, leftPos + 119, topPos + 16, tileEntity.radio::isDisabled)
            .setAction(() -> TileCruiseLauncher.PACKET_RADIO_DISABLE.sendToServer(tileEntity))
        );

        // Target tooltip
        addComponent(new TooltipTranslations(2, 16, 14, 14, LauncherLangs.TRANSLATION_TOOLTIP_TARGET).withDelay(1));
    }

    /** Draw the foreground layer (everything in front of the items) */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        // Draw text
        drawString(graphics, "§7" + LanguageUtility.getLocal(GUI_NAME), 52, 6, 4210752);
        drawString(graphics, LanguageUtility.getLocal("container.inventory"), 8, this.imageHeight - 96 + 4, 4210752);

        // Goes last so tooltips render above our UI elements
        super.renderLabels(graphics, mouseX, mouseY);
    }
}
