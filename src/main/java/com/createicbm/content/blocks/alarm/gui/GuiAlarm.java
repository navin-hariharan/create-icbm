package com.createicbm.content.blocks.alarm.gui;

import com.createicbm.ICBMConstants;
import com.createicbm.content.blocks.alarm.BlockAlarm;
import com.createicbm.content.blocks.alarm.TileAlarm;
import com.createicbm.content.reg.ItemReg;
import com.createicbm.prefab.gui.GuiContainerBase;
import com.createicbm.prefab.gui.TextInput;
import com.createicbm.prefab.gui.tooltip.TooltipTranslations;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Screen of the alarm: a box for the radio channel of the radars it listens to, and whether it is linked and sounding.
 */
public class GuiAlarm extends GuiContainerBase<ContainerAlarm>
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_alarm.png");

    private static final Component TOOLTIP_CHANNEL = Component.translatable("gui.create_icbm:alarm.channel");
    private static final Component LINKED = Component.translatable("gui.create_icbm:alarm.linked");
    private static final Component NOT_LINKED = Component.translatable("gui.create_icbm:alarm.not_linked");
    private static final Component SOUNDING = Component.translatable("gui.create_icbm:alarm.sounding");
    private static final Component SILENT = Component.translatable("gui.create_icbm:alarm.silent");

    private static final int TEXT_COLOR = 4210752;
    private static final int SOUNDING_COLOR = 0xB02E26;

    /** Status text starts right of the picture and ends at the panel's right edge */
    private static final int STATUS_X = 58;
    private static final int STATUS_WIDTH = 111;

    private final TileAlarm alarm;
    private final ItemStack picture = new ItemStack(ItemReg.itemAlarm.get());

    public GuiAlarm(ContainerAlarm container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.alarm = container.getHost();
        this.imageWidth = 175;
        this.imageHeight = 72;
    }

    @Override
    public ResourceLocation getBackground()
    {
        return TEXTURE;
    }

    @Override
    protected void init()
    {
        super.init();

        // Set on the client right away, the server gets it when the box loses focus or Enter is pressed
        final TextInput<String> channel = TextInput.textField(0, font, 75, 26, 93, 12,
            alarm::getChannelText, alarm::setRadioChannel, (o) -> TileAlarm.PACKET_CHANNEL.sendToServer(alarm));
        channel.setMaxLength(TileAlarm.MAX_CHANNEL_LENGTH);
        addComponent(channel);

        addComponent(new TooltipTranslations(58, 25, 15, 14, TOOLTIP_CHANNEL).withDelay(1));
    }

    private boolean isSounding()
    {
        final Level level = alarm.getLevel();
        if (level == null)
        {
            return false;
        }
        final BlockState state = level.getBlockState(alarm.getBlockPos());
        return state.getBlock() instanceof BlockAlarm && state.getValue(BlockAlarm.ACTIVE);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        // Inside of the hazard frame, flashing red while the alarm sounds
        final int x = leftPos + 12;
        final int y = topPos + 26;
        final Level level = alarm.getLevel();
        if (isSounding() && level != null && level.getGameTime() / 10 % 2 == 0)
        {
            graphics.fill(x, y, x + 32, y + 32, 0x60E02020);
        }

        // The alarm itself at twice the size of an item
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(2, 2, 1);
        graphics.renderItem(picture, 0, 0);
        graphics.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        drawString(graphics, "§7" + title.getString(), 30, 6, TEXT_COLOR);

        final boolean linked = alarm.getRadioChannel() != null;
        drawFitted(graphics, (linked ? LINKED : NOT_LINKED).getString(), STATUS_X, 44, TEXT_COLOR);

        final boolean sounding = isSounding();
        drawFitted(graphics, (sounding ? SOUNDING : SILENT).getString(), STATUS_X, 55, sounding ? SOUNDING_COLOR : TEXT_COLOR);

        super.renderLabels(graphics, mouseX, mouseY);
    }

    /** Draws a status line, shrunk to fit the panel when a translation is long */
    private void drawFitted(GuiGraphics graphics, String text, int x, int y, int color)
    {
        final int width = font.width(text);
        if (width <= STATUS_WIDTH)
        {
            drawString(graphics, text, x, y, color);
            return;
        }
        final float scale = STATUS_WIDTH / (float) width;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y + (1 - scale) * font.lineHeight / 2, 0);
        graphics.pose().scale(scale, scale, 1);
        drawString(graphics, text, 0, 0, color);
        graphics.pose().popPose();
    }
}
