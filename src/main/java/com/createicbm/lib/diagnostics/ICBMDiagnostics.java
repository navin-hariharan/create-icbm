package com.createicbm.lib.diagnostics;

import com.createicbm.CreateICBM;
import com.createicbm.config.ConfigDebug;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.config.missile.ConfigMissile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.concurrent.atomic.AtomicLong;

/** Low-volume traces for diagnosing missile ticking, blast completion, and particle delivery. */
@EventBusSubscriber(modid = "create_icbm")
public final class ICBMDiagnostics
{
    private static final AtomicLong AIR_PARTICLES_SENT = new AtomicLong();
    private static final AtomicLong AIR_PARTICLES_RECEIVED = new AtomicLong();
    private static final AtomicLong AIR_PARTICLES_CREATED = new AtomicLong();
    private static final AtomicLong BLOCK_PARTICLES_SENT = new AtomicLong();
    private static final AtomicLong BLOCK_PARTICLES_RECEIVED = new AtomicLong();
    private static final AtomicLong BLOCK_PARTICLES_CREATED = new AtomicLong();
    private static volatile long lastServerReportTick = Long.MIN_VALUE;
    private static volatile long lastClientReportTick = Long.MIN_VALUE;

    private ICBMDiagnostics()
    {
    }

    public static boolean enabled()
    {
        return ConfigDebug.DEBUG_ICBM_DIAGNOSTICS;
    }

    public static void info(String message, Object... args)
    {
        if (enabled())
        {
            CreateICBM.logger().info("[ICBM-DIAG] " + message, args);
        }
    }

    public static void warn(String message, Object... args)
    {
        if (enabled())
        {
            CreateICBM.logger().warn("[ICBM-DIAG] " + message, args);
        }
    }

    public static void error(String message, Throwable error)
    {
        if (enabled())
        {
            CreateICBM.logger().error("[ICBM-DIAG] " + message, error);
        }
    }

    public static void airParticleSent(Level level)
    {
        if (enabled() && level != null && !level.isClientSide())
        {
            AIR_PARTICLES_SENT.incrementAndGet();
        }
    }

    public static void airParticleReceived()
    {
        if (enabled())
        {
            AIR_PARTICLES_RECEIVED.incrementAndGet();
        }
    }

    public static void airParticleCreated(Level level)
    {
        if (enabled() && level != null && level.isClientSide())
        {
            AIR_PARTICLES_CREATED.incrementAndGet();
        }
    }

    public static void blockParticleSent(Level level)
    {
        if (enabled() && level != null && !level.isClientSide())
        {
            BLOCK_PARTICLES_SENT.incrementAndGet();
        }
    }

    public static void blockParticleReceived()
    {
        if (enabled())
        {
            BLOCK_PARTICLES_RECEIVED.incrementAndGet();
        }
    }

    public static void blockParticlesCreated(Level level, long count)
    {
        if (enabled() && level != null && level.isClientSide())
        {
            BLOCK_PARTICLES_CREATED.addAndGet(count);
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event)
    {
        if (!enabled())
        {
            return;
        }
        info("START java={} runtime={} mods[servercore={},doespotatotick={},entityculling={},immersive_optimization={},distanthorizons={},sable={},modernfix={},alltheleaks={}] config[directFireChunkLoading={},missileChunkTicketBudget={},blastMsPerTick={},protectionEvents={}]",
            System.getProperty("java.version"), System.getProperty("java.vm.name"),
            loaded("servercore"), loaded("doespotatotick"), loaded("entityculling"), loaded("immersive_optimization"), loaded("distanthorizons"),
            loaded("sable"), loaded("modernfix"), loaded("alltheleaks"),
            ConfigMissile.DIRECT_FIRE_CHUNK_LOADING, ConfigMissile.MAX_MISSILE_CHUNK_TICKETS,
            ConfigBlast.BLAST_MS_PER_TICK, ConfigBlast.PROTECTION_EVENTS);
    }

    private static boolean loaded(String modId)
    {
        return ModList.get().isLoaded(modId);
    }

    @SubscribeEvent
    public static void onLevelTickPost(LevelTickEvent.Post event)
    {
        if (!enabled())
        {
            return;
        }
        final Level level = event.getLevel();
        if (level instanceof ServerLevel serverLevel)
        {
            final long tick = serverLevel.getServer().getTickCount();
            if (tick <= 0 || tick % 20 != 0 || lastServerReportTick == tick)
            {
                return;
            }
            synchronized (ICBMDiagnostics.class)
            {
                if (lastServerReportTick == tick)
                {
                    return;
                }
                lastServerReportTick = tick;
            }
            final long airSent = AIR_PARTICLES_SENT.getAndSet(0);
            final long blockSent = BLOCK_PARTICLES_SENT.getAndSet(0);
            if (airSent > 0 || blockSent > 0)
            {
                info("PARTICLE_NETWORK side=server tick={} airParticlePacketsSent={} blockExplosionParticlePacketsSent={}", tick, airSent, blockSent);
            }
        }
        else if (level.isClientSide())
        {
            final long tick = level.getGameTime();
            if (tick <= 0 || tick % 20 != 0 || lastClientReportTick == tick)
            {
                return;
            }
            synchronized (ICBMDiagnostics.class)
            {
                if (lastClientReportTick == tick)
                {
                    return;
                }
                lastClientReportTick = tick;
            }
            final long airReceived = AIR_PARTICLES_RECEIVED.getAndSet(0);
            final long airCreated = AIR_PARTICLES_CREATED.getAndSet(0);
            final long blockReceived = BLOCK_PARTICLES_RECEIVED.getAndSet(0);
            final long blockCreated = BLOCK_PARTICLES_CREATED.getAndSet(0);
            if (airReceived > 0 || airCreated > 0 || blockReceived > 0 || blockCreated > 0)
            {
                info("PARTICLE_NETWORK side=client tick={} airParticlePacketsReceived={} airParticlesCreated={} blockExplosionParticlePacketsReceived={} blockExplosionParticlesCreated={}",
                    tick, airReceived, airCreated, blockReceived, blockCreated);
            }
        }
    }
}
