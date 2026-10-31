package com.createicbm.lib.world;

import com.createicbm.ICBMConstants;
import com.createicbm.config.ConfigMain;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
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
 * Keeps the chunks of radio machines (Launch Controllers and their pads, Cruise Turrets, radars) loaded and ticking,
 * the way a chunk loader does.
 * <p>
 * A radio message only reaches machines in loaded chunks. A launch site far from every player used to ignore the
 * Laser Designator, Remote Detonator and radars, and a far radar saw no missiles at all. The tickets are NeoForge
 * forced ticking tickets, owned by the machine's position: they are saved, so the site is loaded again after a
 * restart and its machines register their radios without a player going there. Each machine reconciles its tickets
 * when it loads and every few seconds after, and drops them when it is broken or loses its channel.
 */
@EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public final class MachineChunkLoader
{
    /** Ticks between checks for owners whose machine is gone, broken by something that skipped the block removal */
    private static final int SWEEP_TICKS = 200;
    /** Ticks between machines reconciling the chunks they hold */
    public static final int SYNC_TICKS = 100;

    /** Chunks each owner holds per level, filled from the saved tickets on load so stale ones can be dropped */
    private static final Map<ResourceKey<Level>, Map<BlockPos, LongSet>> HELD = new ConcurrentHashMap<>();

    public static final TicketController TICKETS = new TicketController(
        ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "radio_machine"),
        (level, ticketHelper) -> {
            final Map<BlockPos, LongSet> held = HELD.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
            held.clear();
            if (!ConfigMain.KEEP_RADIO_MACHINES_LOADED)
            {
                new ArrayList<>(ticketHelper.getBlockTickets().keySet()).forEach(ticketHelper::removeAllTickets);
                return;
            }
            // Keep them, the machines load with their chunks and reconcile what they hold
            ticketHelper.getBlockTickets().forEach((pos, tickets) -> {
                final LongSet chunks = new LongOpenHashSet();
                chunks.addAll(tickets.nonTicking());
                chunks.addAll(tickets.ticking());
                held.put(pos.immutable(), chunks);
            });
        });

    private MachineChunkLoader()
    {
    }

    /**
     * Wanted chunks for a machine at a position: its own chunk and the radius around it
     */
    public static void addArea(LongSet into, BlockPos pos)
    {
        final int radius = ConfigMain.RADIO_MACHINE_LOAD_RADIUS;
        final int cx = pos.getX() >> 4;
        final int cz = pos.getZ() >> 4;
        for (int x = -radius; x <= radius; x++)
        {
            for (int z = -radius; z <= radius; z++)
            {
                into.add(ChunkPos.asLong(cx + x, cz + z));
            }
        }
    }

    /**
     * Holds exactly the given chunks for the machine, server thread only
     *
     * @param wanted chunks to keep ticking, empty to release all
     */
    public static void sync(BlockEntity machine, LongSet wanted)
    {
        if (!(machine.getLevel() instanceof ServerLevel level) || machine.isRemoved())
        {
            return;
        }
        final BlockPos owner = machine.getBlockPos().immutable();
        final Map<BlockPos, LongSet> held = HELD.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
        final LongSet current = held.getOrDefault(owner, LongSet.of());
        final LongSet target = ConfigMain.KEEP_RADIO_MACHINES_LOADED ? wanted : LongSet.of();
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
                    TICKETS.forceChunk(level, owner, ChunkPos.getX(chunk), ChunkPos.getZ(chunk), true, true);
                }
            }
            final LongIterator remove = current.iterator();
            while (remove.hasNext())
            {
                final long chunk = remove.nextLong();
                if (!target.contains(chunk))
                {
                    TICKETS.forceChunk(level, owner, ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false, true);
                }
            }
        }
        catch (RuntimeException exception)
        {
            ICBMDiagnostics.error("MACHINE_TICKET_FAILED pos=" + owner + " dim=" + level.dimension().location(), exception);
            return;
        }
        if (target.isEmpty())
        {
            held.remove(owner);
        }
        else
        {
            held.put(owner, new LongOpenHashSet(target));
        }
        ICBMDiagnostics.info("MACHINE_TICKET pos={} dim={} chunks={}", owner, level.dimension().location(), target.size());
    }

    /**
     * Drops every ticket of the machine at this position, called when its block is removed
     */
    public static void release(Level world, BlockPos pos)
    {
        if (!(world instanceof ServerLevel level))
        {
            return;
        }
        final Map<BlockPos, LongSet> held = HELD.get(level.dimension());
        final LongSet chunks = held == null ? null : held.remove(pos);
        if (chunks == null)
        {
            return;
        }
        try
        {
            final LongIterator it = chunks.iterator();
            while (it.hasNext())
            {
                final long chunk = it.nextLong();
                TICKETS.forceChunk(level, pos.immutable(), ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false, true);
            }
        }
        catch (RuntimeException exception)
        {
            ICBMDiagnostics.error("MACHINE_TICKET_RELEASE_FAILED pos=" + pos + " dim=" + level.dimension().location(), exception);
        }
    }

    /**
     * Drops the tickets of owners whose machine is no longer there, a block replaced by a world editor or a lost
     * block entity would otherwise keep its chunks loaded forever
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
        for (BlockPos pos : new ArrayList<>(held.keySet()))
        {
            // Only judge loaded chunks, a held chunk is loaded unless the server is still starting up
            if (level.isLoaded(pos) && !(level.getBlockEntity(pos) instanceof IRadioMachine))
            {
                ICBMDiagnostics.info("MACHINE_TICKET_STALE pos={} dim={}", pos, level.dimension().location());
                release(level, pos);
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
        }
    }

    /**
     * Marker for block entities that hold machine tickets, so stale owners can be told apart
     */
    public interface IRadioMachine
    {
    }
}
