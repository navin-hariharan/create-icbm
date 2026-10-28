package com.createicbm.prefab.gui;

import com.createicbm.CreateICBM;
import com.createicbm.ICBMConstants;
import com.createicbm.lib.LanguageUtility;
import com.createicbm.prefab.gui.tooltip.IToolTip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base screen for ICBM machines. Client only.
 * Subclasses override {@link #init()} instead of initGui, {@link #renderLabels(GuiGraphics, int, int)} (foreground,
 * translated to the gui corner) instead of drawGuiContainerForegroundLayer and call super last so tooltips render
 * above.
 */
public abstract class GuiContainerBase<T extends AbstractContainerMenu> extends AbstractContainerScreen<T>
{
    public static final ResourceLocation COMPONENTS_TEXTURE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_components.png");

    public String currentTooltipText = "";

    protected ArrayList<IGuiComponent> components = new ArrayList<>();

    /** Lines to render as tooltip after everything else has been drawn this frame */
    private List<String> pendingTooltip = null;

    /** Debug toogle to render text for the ID and inventory ID for a slot */
    public boolean renderSlotDebugIDs = false;

    public GuiContainerBase(T container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
    }

    public abstract ResourceLocation getBackground();

    @Override
    protected void init()
    {
        super.init();
        this.components.clear();
    }

    protected void drawString(GuiGraphics graphics, String str, int x, int y, int color)
    {
        graphics.drawString(Minecraft.getInstance().font, str, x, y, color, false);
    }

    protected <C extends IGuiComponent> C addComponent(C field)
    {
        if (field instanceof AbstractWidget)
        {
            addRenderableWidget((AbstractWidget) field);
        }
        components.add(field);
        field.onAddedToHost(this);
        return field;
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();
        components.forEach(IGuiComponent::onUpdate);
    }

    /**
     * Foreground layer, pose is translated to (leftPos, topPos). Subclasses draw their text then call super.
     */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        components.forEach(field -> {

            // Draw text
            field.drawForegroundLayer(graphics, mouseX, mouseY);

            // Detect if we need to display error feedback for the box
            if (field instanceof IToolTip && ((IToolTip) field).isWithin(mouseX, mouseY))
            {
                final Component tooltip = ((IToolTip) field).getToolTip();
                if (tooltip != null)
                {
                    try
                    {
                        this.currentTooltipText = LanguageUtility.buildToolTipString(tooltip);
                    }
                    catch (Exception e)
                    {
                        if (CreateICBM.runningAsDev)
                        {
                            CreateICBM.logger().error("Failed to format text for display", e);
                        }
                    }
                }
            }
        });

        // Queue current tooltip if not empty, rendered last in render() so it is above widgets
        if (!StringUtils.isEmpty(this.currentTooltipText))
        {
            pendingTooltip = LanguageUtility.splitByLine(currentTooltipText);
        }

        // Reset tooltip for next render tick
        this.currentTooltipText = "";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY); //TODO consider render tooltips in this step
        components.forEach(component -> component.draw(graphics, mouseX, mouseY, partialTicks));

        if (pendingTooltip != null)
        {
            drawTooltip(graphics, mouseX, mouseY, pendingTooltip);
            pendingTooltip = null;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        //Key for debug render
        if (keyCode == GLFW.GLFW_KEY_INSERT)
        {
            renderSlotDebugIDs = !renderSlotDebugIDs;
            return true;
        }
        if (components.stream().anyMatch(component -> component.onKeyTyped(keyCode, scanCode, modifiers)))
        {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {
        graphics.blit(this.getBackground(), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        components.forEach(component -> component.drawBackgroundLayer(graphics, partialTicks, mouseX, mouseY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        for (IGuiComponent component : components)
        {
            if (component instanceof EditBox && ((EditBox) component).isFocused() && !((EditBox) component).isMouseOver(mouseX, mouseY))
            {
                ((EditBox) component).setFocused(false);
                if (getFocused() == component)
                {
                    setFocused(null);
                }
            }
        }

        final boolean result = super.mouseClicked(mouseX, mouseY, mouseButton);
        components.forEach(component -> {
            if (!(component instanceof AbstractWidget))
            {
                component.onMouseClick((int) mouseX, (int) mouseY, mouseButton);
            }
        });
        return result;
    }

    /**
     * Draws a tooltip at the screen position
     */
    public void drawTooltip(GuiGraphics graphics, int x, int y, Collection<String> toolTips)
    {
        if (toolTips != null && !toolTips.isEmpty())
        {
            final List<Component> lines = toolTips.stream().map(Component::literal).collect(Collectors.toList());
            graphics.renderComponentTooltip(Minecraft.getInstance().font, lines, x, y);
        }
    }
}
