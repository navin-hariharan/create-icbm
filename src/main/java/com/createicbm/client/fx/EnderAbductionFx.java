package com.createicbm.client.fx;

import com.createicbm.lib.network.packet.PacketEnderAbduction;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

import java.util.Random;

/**
 * Client side of the ender missile cutscene (see EnderAbduction on the server): takes the camera and the controls,
 * stares up at the giant enderman, then turns the screen into a purple warp tunnel and lands on the other side.
 * <p>
 * Everything is driven by {@link PacketEnderAbduction}, nothing here changes the game state, the camera and
 * movement input are only overridden for the frame. A watchdog gives control back if the end packet never arrives.
 */
public final class EnderAbductionFx
{
    private enum Phase
    {
        NONE, STARE, TUNNEL, ARRIVAL
    }

    /** Control comes back by itself after this long without an end packet */
    private static final int WATCHDOG_TICKS = 20 * 45;
    private static final int LOCK_IN_TICKS = 8;
    private static final int OUTRO_TICKS = 10;
    private static final int FADE_TICKS = 24;
    /** Longest the arrival screen waits for the terrain under the player */
    private static final int MAX_TERRAIN_WAIT_TICKS = 100;

    private static final int STREAKS = 80;
    private static final int SEGMENTS = 4;
    private static final float[] ANGLE = new float[STREAKS];
    private static final float[] SPEED = new float[STREAKS];
    private static final float[] PHASE = new float[STREAKS];
    private static final float[] SIZE = new float[STREAKS];
    private static final int[] COLOR = new int[STREAKS];
    private static final int[] PALETTE = {0xC63CFF, 0xE27BFF, 0x9B30FF, 0xFF8CFF, 0x7A1FD6, 0xF2B8FF};

    static
    {
        final Random random = new Random(1337);
        for (int i = 0; i < STREAKS; i++)
        {
            ANGLE[i] = random.nextFloat() * Mth.TWO_PI;
            SPEED[i] = 0.7F + random.nextFloat() * 1.5F;
            PHASE[i] = random.nextFloat();
            SIZE[i] = 2F + random.nextFloat() * 7F;
            COLOR[i] = PALETTE[random.nextInt(PALETTE.length)];
        }
    }

    private static Phase phase = Phase.NONE;
    private static int entityId = -1;
    /** Ticks in the current phase */
    private static int ticks = 0;
    private static int totalTicks = 0;
    /** Ticks the camera has been let go of, counts down */
    private static int outro = 0;
    /** Ticks the arrival screen waited for terrain, then the fade out */
    private static int terrainWait = 0;
    private static int fadeTicks = 0;
    /** Last place the camera looked at, kept for the outro when the entity is gone */
    private static Vec3 lastTarget = null;

    private EnderAbductionFx()
    {
    }

    public static boolean isActive()
    {
        return phase != Phase.NONE;
    }

    public static void clear()
    {
        phase = Phase.NONE;
        entityId = -1;
        ticks = 0;
        totalTicks = 0;
        outro = 0;
        terrainWait = 0;
        fadeTicks = 0;
        lastTarget = null;
    }

    // ------------------------------------------------------------------
    // Packet
    // ------------------------------------------------------------------

    public static void handle(byte action, int id)
    {
        switch (action)
        {
            case PacketEnderAbduction.START ->
            {
                clear();
                phase = Phase.STARE;
                entityId = id;
            }
            case PacketEnderAbduction.WARP ->
            {
                phase = Phase.TUNNEL;
                entityId = id;
                ticks = 0;
            }
            case PacketEnderAbduction.ARRIVE ->
            {
                phase = Phase.ARRIVAL;
                entityId = id;
                // The player was moved, the old giant's position is no place to look at anymore
                lastTarget = null;
                ticks = 0;
                terrainWait = 0;
                fadeTicks = 0;
            }
            case PacketEnderAbduction.END ->
            {
                if (phase != Phase.NONE)
                {
                    phase = Phase.NONE;
                    outro = OUTRO_TICKS;
                }
            }
            default ->
            {
            }
        }
    }

    // ------------------------------------------------------------------
    // Ticks
    // ------------------------------------------------------------------

    public static void onClientTick()
    {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null)
        {
            if (phase != Phase.NONE || outro > 0)
            {
                clear();
            }
            return;
        }
        if (minecraft.isPaused())
        {
            return;
        }
        if (outro > 0)
        {
            outro--;
        }
        if (phase == Phase.NONE)
        {
            return;
        }
        ticks++;
        totalTicks++;
        if (totalTicks > WATCHDOG_TICKS)
        {
            clear();
            return;
        }
        if (phase == Phase.ARRIVAL)
        {
            if (fadeTicks == 0 && (ticks < 6 || (!terrainLoaded() && terrainWait < MAX_TERRAIN_WAIT_TICKS)))
            {
                terrainWait++;
            }
            else
            {
                fadeTicks++;
            }
        }
    }

    /**
     * The chunk under the player and the ones around it, so the screen does not fade to the void
     */
    private static boolean terrainLoaded()
    {
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        final ClientLevel level = minecraft.level;
        if (player == null || level == null)
        {
            return true;
        }
        final int cx = player.getBlockX() >> 4;
        final int cz = player.getBlockZ() >> 4;
        for (int x = -1; x <= 1; x++)
        {
            for (int z = -1; z <= 1; z++)
            {
                if (!level.getChunkSource().hasChunk(cx + x, cz + z))
                {
                    return false;
                }
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Camera and controls
    // ------------------------------------------------------------------

    /** How strongly the view is held on the giant: eases in at the start and out at the end */
    private static float lockWeight(float partial)
    {
        if (phase == Phase.NONE)
        {
            return outro > 0 ? Math.max(0F, (outro - partial) / OUTRO_TICKS) : 0F;
        }
        if (phase == Phase.STARE)
        {
            final float x = Mth.clamp((ticks + partial) / LOCK_IN_TICKS, 0F, 1F);
            return x * x * (3 - 2 * x);
        }
        return 1F;
    }

    private static float shake(float partial)
    {
        final float t = totalTicks + partial;
        switch (phase)
        {
            case STARE:
                return 0.15F + Math.min(1F, ticks / 45F) * 1.2F;
            case TUNNEL:
                return 2.2F + 0.8F * Mth.sin(t * 0.9F);
            case ARRIVAL:
                return Math.max(0F, 2.0F * (1F - (ticks + partial) / 40F));
            default:
                return 0F;
        }
    }

    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event)
    {
        final float partial = (float) event.getPartialTick();
        final float weight = lockWeight(partial);
        if (weight <= 0F)
        {
            return;
        }
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        final ClientLevel level = minecraft.level;
        if (player == null || level == null)
        {
            return;
        }

        final Entity giant = entityId >= 0 ? level.getEntity(entityId) : null;
        if (giant != null)
        {
            lastTarget = giant.getPosition(partial).add(0, giant.getEyeHeight(), 0);
        }
        if (lastTarget != null)
        {
            // Look at the glowing eyes
            final Vec3 eye = player.getEyePosition(partial);
            final double dx = lastTarget.x - eye.x;
            final double dy = lastTarget.y - eye.y;
            final double dz = lastTarget.z - eye.z;
            final double horizontal = Math.sqrt(dx * dx + dz * dz);
            final float targetYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90F;
            final float targetPitch = (float) -(Mth.atan2(dy, horizontal) * Mth.RAD_TO_DEG);
            event.setYaw(event.getYaw() + Mth.wrapDegrees(targetYaw - event.getYaw()) * weight);
            event.setPitch(event.getPitch() + (targetPitch - event.getPitch()) * weight);
        }

        // Fear and the pull of the warp: shaking and a swaying horizon
        final float strength = shake(partial) * weight;
        if (strength > 0F)
        {
            final float t = (totalTicks + partial);
            event.setYaw(event.getYaw() + strength * 0.9F * Mth.sin(t * 2.3F) * Mth.cos(t * 1.1F));
            event.setPitch(event.getPitch() + strength * 0.7F * Mth.sin(t * 3.1F + 1F));
            float roll = strength * 1.4F * Mth.sin(t * 1.7F);
            if (phase == Phase.TUNNEL)
            {
                roll += 9F * Mth.sin(t * 0.33F);
            }
            event.setRoll(event.getRoll() + roll * weight);
        }
    }

    public static void onFov(ViewportEvent.ComputeFov event)
    {
        if (!event.usedConfiguredFov())
        {
            return;
        }
        final float partial = (float) event.getPartialTick();
        final float t = ticks + partial;
        double factor;
        switch (phase)
        {
            case STARE:
                // Slow zoom in on the face
                factor = 1.0 - 0.22 * Mth.clamp(t / 45F, 0F, 1F);
                break;
            case TUNNEL:
                // Stretches out, like in the speed tunnel
                factor = 0.78 + 0.75 * Mth.clamp(t / 12F, 0F, 1F) + 0.05 * Mth.sin((totalTicks + partial) * 0.5F);
                break;
            case ARRIVAL:
                factor = Mth.lerp(Mth.clamp(t / 30F, 0F, 1F), 1.5, 1.0);
                break;
            default:
                if (outro > 0)
                {
                    factor = 1.0;
                    break;
                }
                return;
        }
        event.setFOV(event.getFOV() * factor);
    }

    /**
     * The player cannot walk, jump or sneak away, the vortex holds them
     */
    public static void onMovementInput(MovementInputUpdateEvent event)
    {
        if (phase == Phase.NONE)
        {
            return;
        }
        final Input input = event.getInput();
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
        input.forwardImpulse = 0F;
        input.leftImpulse = 0F;
    }

    // ------------------------------------------------------------------
    // Overlay
    // ------------------------------------------------------------------

    public static void render(GuiGraphics graphics, DeltaTracker delta)
    {
        if (phase == Phase.NONE)
        {
            return;
        }
        final float partial = delta.getGameTimeDeltaPartialTick(false);
        final int width = graphics.guiWidth();
        final int height = graphics.guiHeight();
        final float clock = totalTicks + partial;

        switch (phase)
        {
            case STARE ->
            {
                final float progress = Mth.clamp((ticks + partial) / 45F, 0F, 1F);
                vignette(graphics, width, height, 0.35F + 0.55F * progress, 0x0A0012);
                vignette(graphics, width, height, 0.12F + 0.3F * progress, 0x7A1FD6);
            }
            case TUNNEL ->
            {
                final float in = Mth.clamp((ticks + partial) / 10F, 0F, 1F);
                final float pulse = 0.5F + 0.5F * Mth.sin(clock * 0.45F);
                tint(graphics, width, height, in * (0.28F + 0.1F * pulse), 0x8A2BE2);
                streaks(graphics, width, height, clock, in, 1F);
                vignette(graphics, width, height, 0.5F * in, 0x2A0050);
                if (ticks < 4)
                {
                    // The moment the world tears open
                    tint(graphics, width, height, 0.8F * (1F - (ticks + partial) / 4F), 0xF2D0FF);
                }
            }
            case ARRIVAL ->
            {
                final float fade = fadeTicks == 0 ? 0F : Mth.clamp((fadeTicks + partial) / FADE_TICKS, 0F, 1F);
                final float keep = 1F - fade;
                tint(graphics, width, height, keep * 0.95F, 0xE9C6FF);
                streaks(graphics, width, height, clock, 1F, keep * 0.8F);
                vignette(graphics, width, height, 0.25F + 0.4F * keep, 0x14002A);
            }
            default ->
            {
            }
        }
    }

    private static void tint(GuiGraphics graphics, int width, int height, float alpha, int rgb)
    {
        final int a = (int) (Mth.clamp(alpha, 0F, 1F) * 255F);
        if (a > 0)
        {
            graphics.fill(0, 0, width, height, (a << 24) | rgb);
        }
    }

    /**
     * Darkens the edges of the screen in nested frames (fill only does one color, not gradients along the width)
     */
    private static void vignette(GuiGraphics graphics, int width, int height, float strength, int rgb)
    {
        final int steps = 14;
        final int band = (int) (Math.min(width, height) * 0.3F / steps) + 1;
        for (int i = 0; i < steps; i++)
        {
            final float f = 1F - i / (float) steps;
            final int a = (int) (Mth.clamp(strength * f * f, 0F, 1F) * 255F);
            if (a <= 0)
            {
                continue;
            }
            final int color = (a << 24) | rgb;
            final int o = i * band;
            graphics.fill(o, o, width - o, o + band, color);
            graphics.fill(o, height - o - band, width - o, height - o, color);
            graphics.fill(o, o + band, o + band, height - o - band, color);
            graphics.fill(width - o - band, o + band, width - o, height - o - band, color);
        }
    }

    /**
     * Square sparks flying out of the middle of the screen with trails, like the warp tunnel in the reference
     */
    private static void streaks(GuiGraphics graphics, int width, int height, float clock, float intensity, float alphaScale)
    {
        final double cx = width / 2.0;
        final double cy = height / 2.0;
        final double maxRadius = Math.sqrt(cx * cx + cy * cy);
        for (int i = 0; i < STREAKS; i++)
        {
            final float progress = (PHASE[i] + clock * 0.045F * SPEED[i]) % 1F;
            final double radius = progress * progress * maxRadius * 1.1;
            final float length = 0.05F + progress * 0.3F;
            final float cos = Mth.cos(ANGLE[i]);
            final float sin = Mth.sin(ANGLE[i]);
            final int size = Math.max(1, (int) (SIZE[i] * (0.4F + progress * 2.6F)));
            final float fadeIn = Math.min(1F, progress * 5F);
            for (int k = 0; k < SEGMENTS; k++)
            {
                final double r = radius - k * length * maxRadius / SEGMENTS;
                if (r < 0)
                {
                    break;
                }
                final int a = (int) (255F * Mth.clamp(alphaScale * intensity * fadeIn * (1F - k / (float) SEGMENTS), 0F, 1F));
                if (a <= 0)
                {
                    break;
                }
                final int px = (int) (cx + cos * r);
                final int py = (int) (cy + sin * r);
                final int half = size / 2;
                graphics.fill(px - half, py - half, px - half + size, py - half + size, (a << 24) | COLOR[i]);
            }
        }
    }
}
