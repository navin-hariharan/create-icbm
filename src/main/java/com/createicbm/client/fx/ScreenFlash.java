package com.createicbm.client.fx;

import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

/**
 * White flash over the whole screen from a flashbang missile, drawn above every other GUI layer
 */
public final class ScreenFlash
{
    /** Fade out after the fully white part */
    private static final long FADE_MS = 500;

    private static long fullUntil = 0;

    private ScreenFlash()
    {
    }

    /**
     * @param ticks the screen stays fully white for
     */
    public static void start(int ticks)
    {
        fullUntil = Math.max(fullUntil, Util.getMillis() + ticks * 50L);
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta)
    {
        draw(graphics, 0F);
    }

    /** Over open screens, moved forward so it also covers their item icons and tooltips */
    public static void render(GuiGraphics graphics)
    {
        draw(graphics, 1000F);
    }

    /**
     * @param z offset of the fill, kept at 0 for the HUD layer where a long stack of layers could push it past the far plane
     */
    private static void draw(GuiGraphics graphics, float z)
    {
        final long now = Util.getMillis();
        if (now >= fullUntil + FADE_MS)
        {
            return;
        }
        final float alpha = now <= fullUntil ? 1F : 1F - (now - fullUntil) / (float) FADE_MS;
        final int color = ((int) (alpha * 255) << 24) | 0xFFFFFF;
        graphics.pose().pushPose();
        graphics.pose().translate(0F, 0F, z);
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), color);
        graphics.pose().popPose();
    }
}
