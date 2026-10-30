package com.createicbm.content.blocks.radarstation.gui;

import com.createicbm.content.blocks.radarstation.TileRadarStation;
import com.createicbm.content.blocks.radarstation.data.RadarDotType;
import com.createicbm.content.blocks.radarstation.data.RadarRenderData;
import com.createicbm.content.blocks.radarstation.data.RadarRenderDot;
import com.createicbm.prefab.gui.GuiContainerBase;
import com.createicbm.prefab.gui.IGuiComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/** Compact Create-style radar scope with a hard circular clip, calm grid colours and clearly separated contacts. */
public class RadarComponent implements IGuiComponent
{
    private static final int OUTER_RING = 0xFF4E9D8C;
    private static final int GRID = 0xFF234D45;
    private static final int CROSSHAIR = 0xFF376B60;
    private static final int TRIGGER_RING = 0xFFE2A64A;
    private static final int MARKER = 0xFFB5D9CC;
    private static final int HOSTILE = 0xFFEEB64B;
    private static final int INCOMING = 0xFFFF554F;
    private static final int SCOPE_BACKGROUND = 0xFF07130F;
    private static final int FRAME = 0xFF344E44;

    // Dedicated console sits beneath the radio controls on the right-hand side of the GUI.
    // Keep these in GUI-local coordinates so both the standard and advanced radar share the layout.
    private static final int CONSOLE_X = 89;
    // Align the lower bezel with the bottom edge of the range input controls (y=77..89).
    private static final int CONSOLE_Y = 32;
    private static final int CONSOLE_WIDTH = 78;
    private static final int CONSOLE_HEIGHT = 56;
    private static final int CONSOLE_OUTER = 0xFF29231C;
    private static final int CONSOLE_BRASS = 0xFF8A6635;
    private static final int CONSOLE_BEZEL = 0xFF403324;
    private static final int CONSOLE_BLACK = 0xFF020705;
    private static final int CONSOLE_RULE = 0xFF19342D;
    private static final int CONSOLE_HEADER = 0xFF5B9687;

    private final TileRadarStation tile;
    private final int x;
    private final int y;
    private GuiContainerBase<?> container;

    public RadarComponent(TileRadarStation tile, int x, int y)
    {
        this.tile = tile;
        this.x = x;
        this.y = y;
    }

    @Override
    public void onAddedToHost(GuiContainerBase<?> container)
    {
        this.container = container;
    }

    @Override
    public void onUpdate()
    {
        // Kept intentionally free of computed spacing: the display now scales rings proportionally to range.
    }

    @Override
    public void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY)
    {
        final List<RadarRenderDot> dots = tile.getRadarRenderData().getDots();
        final int hostileCount = (int) dots.stream().filter(dot -> dot.getType() == RadarDotType.HOSTILE).count();
        final int incomingCount = (int) dots.stream().filter(dot -> dot.getType() == RadarDotType.INCOMING).count();
        final var font = Minecraft.getInstance().font;

        // The readout belongs inside the dedicated black console on the right, not
        // over the light paper panel or the scope. Use a compact header and evenly
        // spaced rows that fit the console at the normal Minecraft GUI scale.
        final int labelX = CONSOLE_X + 4;
        final int maxLabelWidth = CONSOLE_WIDTH - 8;

        graphics.drawString(font, "RADAR FEED", labelX, CONSOLE_Y + 3, CONSOLE_HEADER, false);
        drawFittedText(graphics, font, "HOSTILE " + twoDigits(hostileCount), labelX, CONSOLE_Y + 14, HOSTILE, maxLabelWidth);
        drawFittedText(graphics, font, "INBOUND " + twoDigits(incomingCount), labelX, CONSOLE_Y + 24, INCOMING, maxLabelWidth);
        drawFittedText(graphics, font, "RANGE " + tile.getDetectionRange() + "m", labelX, CONSOLE_Y + 34, OUTER_RING, maxLabelWidth);
        drawFittedText(graphics, font, "TRIG " + tile.getTriggerRange() + "m", labelX, CONSOLE_Y + 44, TRIGGER_RING, maxLabelWidth);
    }

    /** Draw a status line without allowing unusually large values to extend outside the GUI. */
    private static void drawFittedText(GuiGraphics graphics, net.minecraft.client.gui.Font font,
                                       String text, int x, int y, int color, int maxWidth)
    {
        final int textWidth = font.width(text);
        if (textWidth <= maxWidth || textWidth == 0)
        {
            graphics.drawString(font, text, x, y, color, false);
            return;
        }

        final float scale = maxWidth / (float) textWidth;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        final int size = RadarRenderData.UV_SIZE;
        final int radius = size / 2 - 2;
        final int left = container.getGuiLeft() + x;
        final int top = container.getGuiTop() + y;
        final int cx = left + size / 2;
        final int cy = top + size / 2;

        drawConsoleFrame(graphics, container.getGuiLeft() + CONSOLE_X, container.getGuiTop() + CONSOLE_Y);

        graphics.fill(left - 1, top - 1, left + size + 1, top + size + 1, FRAME);
        graphics.fill(left, top, left + size, top + size, 0xFF020806);
        fillCircle(graphics, cx, cy, radius, SCOPE_BACKGROUND);

        // Range circles and their very short cardinal crosshair ticks remain inside the circular scope.
        drawCircleOutline(graphics, cx, cy, radius, OUTER_RING);
        drawCircleOutline(graphics, cx, cy, Math.max(2, radius * 2 / 3), GRID);
        drawCircleOutline(graphics, cx, cy, Math.max(1, radius / 3), GRID);
        drawHorizontalClippedLine(graphics, cx, cy, radius, CROSSHAIR);
        drawVerticalClippedLine(graphics, cx, cy, radius, CROSSHAIR);

        final double range = Math.max(1, tile.getDetectionRange());
        final int triggerRadius = Math.max(1, Math.min(radius, (int) Math.round(tile.getTriggerRange() / range * radius)));
        drawCircleOutline(graphics, cx, cy, triggerRadius, TRIGGER_RING);

        // Contact coordinates are clipped before rendering, so no bright lines or markers can leak past the scope.
        for (RadarRenderDot dot : tile.getRadarRenderData().getDots())
        {
            final int px = Math.max(-radius, Math.min(radius, dot.getX()));
            final int py = Math.max(-radius, Math.min(radius, dot.getY()));
            final int dx = px;
            final int dy = py;
            if (dx * dx + dy * dy > radius * radius)
            {
                continue;
            }
            final int dotX = cx + dx;
            final int dotY = cy + dy;
            if (dot.getType() == RadarDotType.MARKER)
            {
                graphics.fill(dotX, dotY, dotX + 1, dotY + 1, MARKER);
            }
            else if (dot.getType() == RadarDotType.HOSTILE)
            {
                drawClippedContact(graphics, cx, cy, dx, dy, radius, HOSTILE);
            }
            else if (dot.getType() == RadarDotType.INCOMING)
            {
                drawClippedContact(graphics, cx, cy, dx, dy, radius, INCOMING);
            }
        }
    }

    /** Draw a restrained brass-and-steel bezel around the black status console. */
    private static void drawConsoleFrame(GuiGraphics graphics, int left, int top)
    {
        graphics.fill(left - 1, top - 1, left + CONSOLE_WIDTH + 1, top + CONSOLE_HEIGHT + 1, CONSOLE_OUTER);
        graphics.fill(left, top, left + CONSOLE_WIDTH, top + CONSOLE_HEIGHT, CONSOLE_BRASS);
        graphics.fill(left + 1, top + 1, left + CONSOLE_WIDTH - 1, top + CONSOLE_HEIGHT - 1, CONSOLE_BEZEL);
        graphics.fill(left + 2, top + 2, left + CONSOLE_WIDTH - 2, top + CONSOLE_HEIGHT - 2, CONSOLE_BLACK);
        // Header underline and tiny power indicator give it a compact machine-console look.
        graphics.fill(left + 4, top + 12, left + CONSOLE_WIDTH - 4, top + 13, CONSOLE_RULE);
        graphics.fill(left + CONSOLE_WIDTH - 7, top + 5, left + CONSOLE_WIDTH - 5, top + 7, CONSOLE_HEADER);
    }

    private static String twoDigits(int number)
    {
        return number < 10 ? "0" + number : Integer.toString(number);
    }

    private static void drawClippedContact(GuiGraphics graphics, int cx, int cy, int dx, int dy, int radius, int color)
    {
        for (int ox = -1; ox <= 1; ox++)
        {
            for (int oy = -1; oy <= 1; oy++)
            {
                final int px = dx + ox;
                final int py = dy + oy;
                if (px * px + py * py <= radius * radius)
                {
                    graphics.fill(cx + px, cy + py, cx + px + 1, cy + py + 1, color);
                }
            }
        }
    }

    private static void fillCircle(GuiGraphics graphics, int cx, int cy, int radius, int color)
    {
        for (int dy = -radius; dy <= radius; dy++)
        {
            final int halfWidth = (int) Math.floor(Math.sqrt(radius * radius - dy * dy));
            graphics.fill(cx - halfWidth, cy + dy, cx + halfWidth + 1, cy + dy + 1, color);
        }
    }

    private static void drawCircleOutline(GuiGraphics graphics, int cx, int cy, int radius, int color)
    {
        if (radius < 1)
        {
            graphics.fill(cx, cy, cx + 1, cy + 1, color);
            return;
        }
        for (int dy = -radius; dy <= radius; dy++)
        {
            final int dx = (int) Math.round(Math.sqrt(Math.max(0, radius * radius - dy * dy)));
            graphics.fill(cx - dx, cy + dy, cx - dx + 1, cy + dy + 1, color);
            graphics.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private static void drawHorizontalClippedLine(GuiGraphics graphics, int cx, int cy, int radius, int color)
    {
        graphics.fill(cx - radius + 2, cy, cx - 4, cy + 1, color);
        graphics.fill(cx + 4, cy, cx + radius - 1, cy + 1, color);
    }

    private static void drawVerticalClippedLine(GuiGraphics graphics, int cx, int cy, int radius, int color)
    {
        graphics.fill(cx, cy - radius + 2, cx + 1, cy - 4, color);
        graphics.fill(cx, cy + 4, cx + 1, cy + radius - 1, color);
    }
}
