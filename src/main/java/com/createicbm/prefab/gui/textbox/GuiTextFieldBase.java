package com.createicbm.prefab.gui.textbox;

import com.createicbm.prefab.gui.GuiContainerBase;
import com.createicbm.prefab.gui.IGuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Text field component. Client only.
 * The 1.21 {@link EditBox} is a widget with absolute coordinates, so the relative position is kept and converted when
 * added to the container. Typed characters reach the box through the screen's focus handling.
 */
public class GuiTextFieldBase extends EditBox implements IGuiComponent {

    protected GuiContainerBase<?> container;

    /** Position relative to the top-left of the container */
    protected final int relativeX;
    protected final int relativeY;

    /** Mirrors EditBox's border flag, whose getter is not public in 1.21.1 */
    private boolean bordered = true;

    public GuiTextFieldBase(int componentId, Font font, int x, int y, int width, int height) {
        super(font, x, y, width, height, Component.empty());
        this.relativeX = x;
        this.relativeY = y;
    }

    @Override
    public void onAddedToHost(GuiContainerBase<?> container) {
        this.container = container;
        this.setX(container.getGuiLeft() + relativeX);
        this.setY(container.getGuiTop() + relativeY);
    }

    @Override
    public boolean onKeyTyped(int keyCode, int scanCode, int modifiers) {
        // Enter sets the typed value, the same as clicking away from the box
        if(isFocused() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            setFocused(false);
            if(container != null && container.getFocused() == this) {
                container.setFocused(null);
            }
            return true;
        }
        // Consume keys while typing so the inventory key doesn't close the screen
        if(isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return false;
    }

    @Override
    public void setBordered(boolean bordered) {
        this.bordered = bordered;
        super.setBordered(bordered);
    }

    public boolean hasBorder() {
        return bordered;
    }

}
