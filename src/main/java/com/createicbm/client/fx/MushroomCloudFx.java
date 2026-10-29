package com.createicbm.client.fx;

import com.createicbm.config.ConfigClient;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Draws mushroom clouds, see {@link MushroomCloudShape} for the shape. Every client makes its own particles from the
 * one packet it got, with a particle budget per tick that shrinks with the particle setting and the distance, so a
 * player a few hundred blocks away pays only for what they can see.
 */
public final class MushroomCloudFx
{
    /** More at once only stacks particles on top of each other */
    private static final int MAX_CLOUDS = 3;
    /** Ticks the cap keeps leaking smoke after the cloud stopped rising */
    private static final int TAIL_TICKS = 140;
    /** Beyond this the budget starts to shrink, down to a quarter at the far end */
    private static final double FULL_BUDGET_DISTANCE = 128;
    private static final double LOW_BUDGET_DISTANCE = 600;

    private static final List<Cloud> CLOUDS = new ArrayList<>();

    private MushroomCloudFx()
    {
    }

    private static final class Cloud
    {
        final ClientLevel level;
        final double x, y, z;
        final float radius;
        final int duration;
        /** Particle size multiplier, a bigger blast gets bigger puffs */
        final float scale;
        int age;
        /** Carries the fraction of a particle between ticks so low budgets still produce a steady stream */
        float carry;

        Cloud(ClientLevel level, double x, double y, double z, float radius, int duration)
        {
            this.level = level;
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
            this.duration = Math.max(20, duration);
            this.scale = Mth.clamp(radius / 55F, 0.5F, 2.5F);
        }
    }

    public static void clear()
    {
        CLOUDS.clear();
    }

    public static void start(double x, double y, double z, float radius, int ticks)
    {
        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null || ConfigClient.MUSHROOM_CLOUD_PARTICLES <= 0)
        {
            ICBMDiagnostics.warn("MUSHROOM_CLOUD_FX_SKIPPED clientLevelPresent={} particleBudget={} pos=({}, {}, {}) radius={} ticks={}",
                level != null, ConfigClient.MUSHROOM_CLOUD_PARTICLES, x, y, z, radius, ticks);
            return;
        }
        if (CLOUDS.size() >= MAX_CLOUDS)
        {
            CLOUDS.remove(0);
        }
        CLOUDS.add(new Cloud(level, x, y, z, radius, ticks));
        ICBMDiagnostics.info("MUSHROOM_CLOUD_FX_STARTED dimension={} pos=({}, {}, {}) radius={} ticks={} activeClouds={} particleBudget={}",
            level.dimension().location(), x, y, z, radius, ticks, CLOUDS.size(), ConfigClient.MUSHROOM_CLOUD_PARTICLES);
    }

    public static void onClientTick()
    {
        if (CLOUDS.isEmpty())
        {
            return;
        }
        final Minecraft minecraft = Minecraft.getInstance();
        final ClientLevel level = minecraft.level;
        final LocalPlayer player = minecraft.player;
        if (level == null || player == null)
        {
            CLOUDS.clear();
            return;
        }
        if (minecraft.isPaused())
        {
            return;
        }

        final Iterator<Cloud> it = CLOUDS.iterator();
        while (it.hasNext())
        {
            final Cloud cloud = it.next();
            if (cloud.level != level || ++cloud.age > cloud.duration + TAIL_TICKS)
            {
                it.remove();
                continue;
            }
            emit(cloud, minecraft, player.getX(), player.getZ());
        }
    }

    private static float budgetFactor(ParticleStatus status, double distance)
    {
        float factor = status == ParticleStatus.MINIMAL ? 0.2F : status == ParticleStatus.DECREASED ? 0.5F : 1F;
        if (distance > FULL_BUDGET_DISTANCE)
        {
            final double far = Mth.clamp((distance - FULL_BUDGET_DISTANCE) / (LOW_BUDGET_DISTANCE - FULL_BUDGET_DISTANCE), 0, 1);
            factor *= (float) (1.0 - 0.75 * far);
        }
        return factor;
    }

    private static void emit(Cloud c, Minecraft minecraft, double px, double pz)
    {
        final double dx = px - c.x;
        final double dz = pz - c.z;
        final float factor = budgetFactor(minecraft.options.particles().get(), Math.sqrt(dx * dx + dz * dz));

        final double p = Math.min(1.0, c.age / (double) c.duration);
        final boolean rising = c.age <= c.duration;
        // After the cloud stopped rising only a thin trickle from the cap is left
        final double tail = rising ? 1.0 : Math.max(0.0, 1.0 - (c.age - c.duration) / (double) TAIL_TICKS) * 0.35;

        c.carry += ConfigClient.MUSHROOM_CLOUD_PARTICLES * factor * (float) tail;
        final int budget = (int) c.carry;
        c.carry -= budget;
        if (budget <= 0)
        {
            return;
        }

        final RandomSource random = c.level.random;
        final double radius = c.radius;
        final double headY = MushroomCloudShape.capHeight(p, radius);
        final double heat = MushroomCloudShape.heat(p);
        final double rise = rising ? MushroomCloudShape.riseSpeed(p, radius) / c.duration : 0.0;

        final boolean fireball = p < MushroomCloudShape.FIREBALL_END && rising;
        final boolean hasStem = rising && headY > 4;
        // Share of the budget: the fireball takes over at the start, later the stem and the cap
        final double fireShare = fireball ? 0.5 * (1.0 - p / MushroomCloudShape.FIREBALL_END) + 0.1 : 0.0;
        final double skirtShare = rising && p < 0.5 ? 0.1 : 0.0;
        final double stemShare = hasStem ? 0.25 : 0.0;

        for (int i = 0; i < budget; i++)
        {
            final double roll = random.nextDouble();
            if (roll < fireShare)
            {
                fireball(c, random, p);
            }
            else if (roll < fireShare + skirtShare)
            {
                skirt(c, random, p);
            }
            else if (roll < fireShare + skirtShare + stemShare)
            {
                stem(c, random, p, headY, heat, rise);
            }
            else if (headY > 6)
            {
                cap(c, random, p, headY, heat, rise);
            }
        }
    }

    private static void fireball(Cloud c, RandomSource random, double p)
    {
        final double r = MushroomCloudShape.fireballRadius(p, c.radius);
        // Random point in a sphere, cube roots keep the spread even
        final double theta = random.nextDouble() * Mth.TWO_PI;
        final double cosPhi = random.nextDouble() * 2 - 1;
        final double sinPhi = Math.sqrt(1 - cosPhi * cosPhi);
        final double dist = r * Math.cbrt(random.nextDouble());
        final double ox = Math.cos(theta) * sinPhi * dist;
        final double oy = cosPhi * dist;
        final double oz = Math.sin(theta) * sinPhi * dist;
        final double heat = 1.0 - p / MushroomCloudShape.FIREBALL_END * 0.5;
        // White hot in the middle, orange and red at the edge
        final double edge = r <= 0 ? 0 : dist / r;
        final float red = 1F;
        final float green = (float) Mth.clamp(0.95 - 0.65 * edge * (1.5 - heat), 0.15, 1);
        final float blue = (float) Mth.clamp(0.8 - 1.0 * edge, 0.02, 0.9) * (float) heat;
        spawn(c, c.x + ox, c.y + r * 0.6 + oy, c.z + oz, ox * 0.012, oy * 0.012 + 0.03, oz * 0.012,
            red, green, blue, 16F * c.scale, 26 + random.nextInt(24), true, true);
    }

    private static void skirt(Cloud c, RandomSource random, double p)
    {
        final double r = MushroomCloudShape.skirtRadius(p, c.radius) * (0.85 + 0.3 * random.nextDouble());
        final double angle = random.nextDouble() * Mth.TWO_PI;
        final double cos = Math.cos(angle);
        final double sin = Math.sin(angle);
        spawn(c, c.x + cos * r, c.y + 1.5, c.z + sin * r, cos * 0.35, 0.03, sin * 0.35,
            0.5F, 0.44F, 0.36F, 13F * c.scale, 50 + random.nextInt(40), false, true);
    }

    private static void stem(Cloud c, RandomSource random, double p, double headY, double heat, double rise)
    {
        final double height = Math.max(1.0, headY);
        final double h = random.nextDouble() * height;
        final double r = MushroomCloudShape.stemRadiusAt(h, height, p, c.radius) * (0.4 + 0.7 * random.nextDouble());
        final double angle = random.nextDouble() * Mth.TWO_PI;
        final double cos = Math.cos(angle);
        final double sin = Math.sin(angle);
        // The foot of the stem still glows, higher up it is dark smoke being sucked upwards
        final double low = 1.0 - Math.min(1.0, h / height);
        final double hot = heat * low * low;
        final float red = (float) Mth.lerp(hot, 0.34, 1.0);
        final float green = (float) Mth.lerp(hot, 0.3, 0.45);
        final float blue = (float) Mth.lerp(hot, 0.28, 0.12);
        spawn(c, c.x + cos * r, c.y + h, c.z + sin * r,
            -cos * 0.02, rise * 0.6 + 0.05, -sin * 0.02,
            red, green, blue, 13F * c.scale, 55 + random.nextInt(40), hot > 0.35, true);
    }

    private static void cap(Cloud c, RandomSource random, double p, double headY, double heat, double rise)
    {
        final double major = MushroomCloudShape.capRadius(p, c.radius) * 0.72;
        final double minor = MushroomCloudShape.capRadius(p, c.radius) * 0.38;
        final double theta = random.nextDouble() * Mth.TWO_PI;
        final double phi = random.nextDouble() * Mth.TWO_PI;
        final double ring = major + minor * Math.cos(phi);
        final double cosT = Math.cos(theta);
        final double sinT = Math.sin(theta);
        // The cap rolls: outwards on top, down on the outside, inwards below
        final double roll = 0.16;
        final double vr = Math.sin(phi) * roll;
        final double vy = -Math.cos(phi) * roll * 0.7;
        // Underside lit by the fire, top already cooled to ash
        final double lit = heat * (0.5 - 0.5 * Math.sin(phi));
        final float red = (float) Mth.lerp(lit, 0.3, 1.0);
        final float green = (float) Mth.lerp(lit, 0.27, 0.42);
        final float blue = (float) Mth.lerp(lit, 0.26, 0.1);
        spawn(c, c.x + ring * cosT, c.y + headY + minor * 0.75 * Math.sin(phi), c.z + ring * sinT,
            vr * cosT, vy + rise * 0.85, vr * sinT,
            red, green, blue, 24F * c.scale, 60 + random.nextInt(50), lit > 0.4, true);
    }

    private static void spawn(Cloud c, double x, double y, double z, double vx, double vy, double vz,
                              float red, float green, float blue, float size, int life, boolean glow, boolean fade)
    {
        final ParticleAirICBM particle = new ParticleAirICBM(c.level, x, y, z, vx, vy, vz, size);
        particle.setColor(red, green, blue, false);
        particle.setAge(life);
        particle.setGlow(glow);
        particle.setFade(fade);
        Minecraft.getInstance().particleEngine.add(particle);
    }
}
