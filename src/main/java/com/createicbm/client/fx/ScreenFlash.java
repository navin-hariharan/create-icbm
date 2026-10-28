package com.createicbm.client.fx;

import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

/**
 * White flash over the whole screen from a flashbang missile, drawn above every other GUI layer
 */
public final class ScreenFlash
{
    /** Fade out after the fully white part when nothing else is asked for */
    private static final long DEFAULT_FADE_MS = 500;

    private static long fullUntil = 0;
    private static long fadeMs = DEFAULT_FADE_MS;

    private ScreenFlash()
    {
    }

    /** Ends a running flash, called when leaving a world so it doesn't carry over to the next one */
    public static void clear()
    {
        fullUntil = 0;
        fadeMs = DEFAULT_FADE_MS;
    }

    /**
     * @param ticks the screen stays fully white for
     */
    public static void start(int ticks)
    {
        start(ticks, 10);
    }

    /**
     * @param ticks     the screen stays fully white for
     * @param fadeTicks ticks it takes to fade back to the game afterwards
     */
    public static void start(int ticks, int fadeTicks)
    {
        final long now = Util.getMillis();
        final long end = now + ticks * 50L + fadeTicks * 50L;
        // A weaker flash must not cut a stronger one that is still running short
        if (end > fullUntil + fadeMs)
        {
            fullUntil = now + ticks * 50L;
            fadeMs = Math.max(1L, fadeTicks * 50L);
        }
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
        if (now >= fullUntil + fadeMs)
        {
            return;
        }
        final float alpha = now <= fullUntil ? 1F : 1F - (now - fullUntil) / (float) fadeMs;
        final int color = ((int) (alpha * 255) << 24) | 0xFFFFFF;
        graphics.pose().pushPose();
        graphics.pose().translate(0F, 0F, z);
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), color);
        graphics.pose().popPose();
    }
}
