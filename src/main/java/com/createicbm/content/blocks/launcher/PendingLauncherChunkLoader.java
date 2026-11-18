package com.createicbm.content.blocks.launcher;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
import com.createicbm.lib.world.MachineChunkLoader;
import com.createicbm.mods.sable.SableProxy;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps a launcher block entity ticking while it has a queued firing package.
 *
 * <p>RadioCruise acknowledges a received trigger when it queues the firing package. Without a chunk ticket, the
 * launcher block entity can then stop ticking outside a player's simulation distance and the missile appears to
 * launch only when a player returns. NeoForge's persistent ticking ticket lets the countdown/aiming complete with
 * no nearby (or online) players, and resumes a queued launch after a server restart.</p>
 *
 * <p>Tickets are owned by the block position and must be removed when the package finishes or the launcher is broken.
 * On chunk load, launchers call {@link #sync(BlockEntity, boolean)} so a stale ticket left by a crash is removed when
 * there is no saved firing package.</p>
 *
 * <p>A launcher on a Create Aeronautics (Sable) ship is stored in the ship's plot, far from where the ship is. Its
 * ticket goes on the world chunk under the ship instead, which keeps the ship loaded, and moves with the ship while the
 * launcher keeps calling {@link #sync(BlockEntity, boolean)}. The chunk each owner holds is remembered (and rebuilt
 * from the saved tickets on load) so the old chunk is released when the ship has moved on.</p>
 */
@EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public final class PendingLauncherChunkLoader
{
    /** Chunks each owner holds per level, filled from the saved tickets on load */
    private static final Map<ResourceKey<Level>, Map<BlockPos, LongSet>> HELD = new ConcurrentHashMap<>();
    /** Game time each owner stored on a ship was first seen unloaded, per level */
    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> SHIP_MISSING = new ConcurrentHashMap<>();
    /** Ticks between checks for launchers on ships that are gone */
    private static final int SWEEP_TICKS = 200;

    public static final TicketController TICKET_CONTROLLER = new TicketController(
        ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "pending_launcher"),
        (level, ticketHelper) -> {
            // Keep every saved ticket, the launchers load with their chunks and reconcile what they hold
            final Map<BlockPos, LongSet> held = HELD.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
            held.clear();
            ticketHelper.getBlockTickets().forEach((pos, tickets) -> {
                final LongSet chunks = new LongOpenHashSet();
                chunks.addAll(tickets.nonTicking());
                chunks.addAll(tickets.ticking());
                held.put(pos.immutable(), chunks);
            });
        });

    private PendingLauncherChunkLoader()
    {
    }

    /** Syncs the persistent ticking ticket with the launcher's saved/runtime state. Server-thread only. */
    public static void sync(BlockEntity launcher, boolean hasPendingLaunch)
    {
        final Level level = launcher.getLevel();
        if (!(level instanceof ServerLevel serverLevel) || level.isClientSide())
        {
            return;
        }

        final BlockPos pos = launcher.getBlockPos().immutable();
        final Map<BlockPos, LongSet> held = HELD.computeIfAbsent(serverLevel.dimension(), k -> new ConcurrentHashMap<>());
        final LongSet current = held.getOrDefault(pos, LongSet.of());
        final LongSet target = new LongOpenHashSet();
        if (hasPendingLaunch && !launcher.isRemoved())
        {
            // The launcher's own chunk, or the world chunk under it when it is on a ship
            target.add(MachineChunkLoader.worldChunk(launcher));
        }
        if (current.equals(target))
        {
            return;
        }

        try
        {
            final LongIterator add = target.iterator();
            while (add.hasNext())
            {
                final long chunk = add.nextLong();
                if (!current.contains(chunk))
                {
                    TICKET_CONTROLLER.forceChunk(serverLevel, pos, ChunkPos.getX(chunk), ChunkPos.getZ(chunk), true, true);
                }
            }
            final LongIterator remove = current.iterator();
            while (remove.hasNext())
            {
                final long chunk = remove.nextLong();
                if (!target.contains(chunk))
                {
                    TICKET_CONTROLLER.forceChunk(serverLevel, pos, ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false, true);
                }
            }
        }
        catch (RuntimeException exception)
        {
            // Nothing is recorded, the next sync tries again
            ICBMDiagnostics.error("PENDING_LAUNCHER_TICKET_FAILED pos=" + pos + " dim=" + level.dimension().location()
                + " pending=" + hasPendingLaunch, exception);
            return;
        }

        if (target.isEmpty())
        {
            held.remove(pos);
        }
        else
        {
            held.put(pos, target);
        }
        ICBMDiagnostics.info("PENDING_LAUNCHER_TICKET action={} pos={} dim={} chunks={} pending={}",
            hasPendingLaunch ? "ADD_OR_MOVE" : "REMOVE", pos, level.dimension().location(), target, hasPendingLaunch);
    }

    /** Called when the launcher block itself is removed; it also cleans up persistent tickets. */
    public static void removeAt(Level level, BlockPos pos)
    {
        if (!(level instanceof ServerLevel serverLevel) || level.isClientSide())
        {
            return;
        }

        final BlockPos immutablePos = pos.immutable();
        final Map<BlockPos, LongSet> held = HELD.get(serverLevel.dimension());
        final LongSet chunks = new LongOpenHashSet();
        final LongSet recorded = held == null ? null : held.remove(immutablePos);
        if (recorded == null || recorded.isEmpty())
        {
            // Saved tickets are remembered when the level loads, so the launcher holds none
            return;
        }
        chunks.addAll(recorded);
        try
        {
            final LongIterator it = chunks.iterator();
            while (it.hasNext())
            {
                final long chunk = it.nextLong();
                TICKET_CONTROLLER.forceChunk(serverLevel, immutablePos, ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false, true);
            }
            ICBMDiagnostics.info("PENDING_LAUNCHER_TICKET action=BLOCK_REMOVED pos={} dim={} chunks={}",
                immutablePos, level.dimension().location(), chunks);
        }
        catch (RuntimeException exception)
        {
            ICBMDiagnostics.error("PENDING_LAUNCHER_TICKET_REMOVE_FAILED pos=" + immutablePos
                + " dim=" + level.dimension().location(), exception);
        }
    }

    /**
     * Drops the ticket of a launcher on a Sable ship that stayed unloaded for {@link MachineChunkLoader#SHIP_GONE_TICKS}.
     * Its ticket is on the world chunk under the ship, which keeps the ship loaded, so the ship was removed or got away.
     * Launchers on land drop theirs when the block is removed or when they load without a waiting launch.
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event)
    {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % SWEEP_TICKS != 0)
        {
            return;
        }
        final Map<BlockPos, LongSet> held = HELD.get(level.dimension());
        if (held == null || held.isEmpty())
        {
            return;
        }
        final Map<BlockPos, Long> missing = SHIP_MISSING.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
        missing.keySet().retainAll(held.keySet());
        for (BlockPos pos : new ArrayList<>(held.keySet()))
        {
            if (level.isLoaded(pos) || !SableProxy.INSTANCE.isOnShip(level, pos))
            {
                missing.remove(pos);
                continue;
            }
            final long since = missing.computeIfAbsent(pos, k -> level.getGameTime());
            if (level.getGameTime() - since >= MachineChunkLoader.SHIP_GONE_TICKS)
            {
                ICBMDiagnostics.info("PENDING_LAUNCHER_TICKET_SHIP_GONE pos={} dim={}", pos, level.dimension().location());
                missing.remove(pos);
                removeAt(level, pos);
            }
        }
    }

    /**
     * Forgets what a level held, another world opened in the same game must not inherit it
     */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event)
    {
        if (event.getLevel() instanceof ServerLevel level)
        {
            HELD.remove(level.dimension());
            SHIP_MISSING.remove(level.dimension());
        }
    }
}
