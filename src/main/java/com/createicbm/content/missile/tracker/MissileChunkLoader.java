package com.createicbm.content.missile.tracker;

import com.createicbm.ICBMConstants;
import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
import com.createicbm.content.missile.logic.flight.DeadFlightLogic;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Keeps the chunks just ahead of a straight flying missile loaded and ticking, the way vanilla does for thrown ender
 * pearls.
 * <p>
 * Ballistic and arc missiles hand over to the missile tracker before they reach a chunk where entities don't tick.
 * Missiles fired straight (handheld and cruise launchers, see {@link DeadFlightLogic}) have nothing to hand over to,
 * so they used to fly into the first chunk past the simulation distance and freeze there until a player came close.
 * The same goes for arc missiles the tracker refused to take, see {@link ISimulatedFlight}.
 * <p>
 * Tickets time out on their own after {@link #TICKET_TICKS}, so nothing is saved, nothing leaks when a missile is
 * removed or the server stops, and chunks load in the background instead of on the server thread.
 * <p>
 * Tickets are renewed from the level tick, not from the missile's own tick. A missile that already sits in a chunk
 * that isn't ticking yet (slow world generation) doesn't tick, and used to let its tickets run out and stay frozen.
 */
@EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class MissileChunkLoader
{
    /** Ticks a ticket lives without being renewed */
    private static final int TICKET_TICKS = 100;
    /** Ticks between path checks, tickets are renewed well before they time out */
    private static final int REFRESH_TICKS = 5;
    /** Ticks of flight looked ahead, at least {@link #MIN_LOOK_AHEAD} and at most {@link #MAX_LOOK_AHEAD} meters */
    private static final double LOOK_AHEAD_TICKS = 30;
    private static final double MIN_LOOK_AHEAD = 48;
    private static final double MAX_LOOK_AHEAD = 128;
    /** Most chunks one missile holds at once, a salvo over fresh terrain would otherwise force a lot of world generation */
    private static final int MAX_HELD_CHUNKS = 16;
    /** Always allowed, the chunk the missile is in and the next one, whatever the total budget says */
    private static final int MIN_HELD_CHUNKS = 2;

    /** Distance 2 gives ticket level 31, the level where entities tick */
    private static final int TICKET_DISTANCE = 2;

    public static final TicketType<Integer> TICKET = TicketType.create(ICBMConstants.DOMAIN + ":missile_flight", Integer::compareTo, TICKET_TICKS);

    /**
     * Missiles to keep tickets for per level, tracked while they are in the level so tickets keep renewing even when
     * they don't tick. Kept per level so a level's tick only walks its own missiles.
     */
    private static final Map<Level, Set<EntityMissile<?>>> TRACKED = new ConcurrentHashMap<>();

    /** Distinct owner from the simulated-flight ticket: either kind of missile can keep a playerless level ticking. */
    private static final BlockPos ENTITY_KEEPALIVE_OWNER = new BlockPos(0, 1, 0);
    private static final Map<Level, Boolean> ENTITY_KEEPALIVE_ACTIVE = new ConcurrentHashMap<>();

    /** Chunks on the path this missile is holding, kept while on the path because they only tick thanks to us */
    private final LongSet heldChunks = new LongOpenHashSet();
    private final LongSet wanted = new LongOpenHashSet();
    private int cooldown = 0;
    private long lastChunk = Long.MIN_VALUE;
    private int diagnosticLastServerTick = Integer.MIN_VALUE;
    private int diagnosticLastEntityTickCount = Integer.MIN_VALUE;

    /**
     * @return how many chunks this missile holds tickets for right now
     */
    public int heldCount()
    {
        return heldChunks.size();
    }

    /** One sampled status line per second; this runs from the level tick, even if the missile itself is frozen. */
    public void traceStatus(EntityMissile<?> missile, ServerLevel level, int serverTick)
    {
        if (!ICBMDiagnostics.enabled() || serverTick <= 0 || serverTick % 20 != 0 || diagnosticLastServerTick == serverTick)
        {
            return;
        }
        final int chunkX = Mth.floor(missile.getX()) >> 4;
        final int chunkZ = Mth.floor(missile.getZ()) >> 4;
        final boolean loaded = level.hasChunkAt(missile.blockPosition());
        final boolean entityTicking = MissileTrackerHandler.canEntitiesTick(level, chunkX, chunkZ);
        final int entityTickCount = missile.tickCount;
        final int tickDelta = diagnosticLastEntityTickCount == Integer.MIN_VALUE ? -1 : entityTickCount - diagnosticLastEntityTickCount;
        final String flightLogic = missile.getMissileCapability().getFlightLogic() == null ? "none" : missile.getMissileCapability().getFlightLogic().getClass().getSimpleName();
        final String status = "MISSILE_SAMPLE serverTick=" + serverTick + " id=" + missile.getId() + " uuid=" + missile.getUUID()
            + " type=" + missile.getClass().getSimpleName() + " dim=" + level.dimension().location()
            + " pos=" + missile.position() + " velocity=" + missile.getDeltaMovement()
            + " chunk=" + chunkX + "," + chunkZ + " chunkLoaded=" + loaded + " entityTicking=" + entityTicking
            + " noCulling=" + missile.noCulling + " removed=" + missile.isRemoved() + " tickCount=" + entityTickCount
            + " entityTickDelta=" + tickDelta + " flightLogic=" + flightLogic + " canRunFlightLogic=" + missile.getMissileCapability().canRunFlightLogic()
            + " directFireChunkLoading=" + ConfigMissile.DIRECT_FIRE_CHUNK_LOADING + " heldChunks=" + heldCount()
            + " shouldLoadPath=" + shouldLoad(missile);
        if (diagnosticLastEntityTickCount != Integer.MIN_VALUE && diagnosticLastServerTick < serverTick && entityTickCount == diagnosticLastEntityTickCount)
        {
            ICBMDiagnostics.warn("{}; POSSIBLE_TICK_STALL: entity tickCount did not advance over {} server ticks", status, serverTick - diagnosticLastServerTick);
        }
        else if (!entityTicking || !loaded)
        {
            ICBMDiagnostics.warn("{}; chunk is not entity-ticking/loaded", status);
        }
        else
        {
            ICBMDiagnostics.info("{}", status);
        }
        diagnosticLastServerTick = serverTick;
        diagnosticLastEntityTickCount = entityTickCount;
    }

    /**
     * Called each server tick while the missile is alive
     *
     * @param budget chunks the dimension's missiles may still hold, at least two are always kept
     */
    public void update(EntityMissile<?> missile, int budget)
    {
        if (!(missile.level() instanceof ServerLevel level))
        {
            return;
        }

        // PERF: the path is checked every few ticks, or right away when the missile enters another chunk
        final long chunk = ChunkPos.asLong(Mth.floor(missile.getX()) >> 4, Mth.floor(missile.getZ()) >> 4);
        if (--cooldown > 0 && chunk == lastChunk)
        {
            return;
        }
        cooldown = REFRESH_TICKS;
        lastChunk = chunk;

        if (!shouldLoad(missile))
        {
            heldChunks.clear();
            return;
        }

        wanted.clear();
        final Vec3 motion = missile.getDeltaMovement();
        final double speed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        final boolean moving = speed > 1.0E-4;
        final double distance = Mth.clamp(speed * LOOK_AHEAD_TICKS, MIN_LOOK_AHEAD, MAX_LOOK_AHEAD);
        MissilePathSampler.collect(
            missile.getX(), missile.getZ(),
            moving ? motion.x / speed : 0, moving ? motion.z / speed : 0,
            moving, distance, Math.min(MAX_HELD_CHUNKS, Math.max(MIN_HELD_CHUNKS, budget)), wanted,
            key -> needsTicket(level, key));

        // Renew the tickets on the path, the ones behind the missile time out
        heldChunks.clear();
        heldChunks.addAll(wanted);
        final Integer owner = missile.getId();
        final LongIterator it = wanted.iterator();
        while (it.hasNext())
        {
            // NeoForge's four-argument overload only forces the chunk to load; it does not guarantee that entities
            // tick when no player is nearby. Path chunks must use the explicit forceTicks overload or the missile can
            // still freeze at simulation-distance boundaries despite the chunk being loaded.
            level.getChunkSource().addRegionTicket(TICKET, new ChunkPos(it.nextLong()), TICKET_DISTANCE, owner, true);
        }
    }

    private boolean needsTicket(ServerLevel level, long key)
    {
        // Query actual server entity-ticking state, not just whether a chunk is present in memory. This is deliberately
        // mod-agnostic: vanilla, NeoForge, and compatible chunk loaders all feed the same ticket/ticking manager.
        // Distant Horizons LODs are render data and cannot satisfy this check, so ICBM requests a real ticking ticket
        // whenever a missile needs collision, guidance, radar, or explosion logic in that chunk.
        return heldChunks.contains(key) || !MissileTrackerHandler.canEntitiesTick(level, ChunkPos.getX(key), ChunkPos.getZ(key));
    }

    /**
     * Starts renewing tickets for a missile, called when it is added to a server level
     */
    public static void track(EntityMissile<?> missile)
    {
        if (!missile.level().isClientSide())
        {
            TRACKED.computeIfAbsent(missile.level(), level -> ConcurrentHashMap.newKeySet()).add(missile);
        }
    }

    /**
     * Stops renewing tickets for a missile, called when it is removed. Its tickets run out on their own.
     */
    public static void untrack(EntityMissile<?> missile)
    {
        final Set<EntityMissile<?>> missiles = TRACKED.get(missile.level());
        if (missiles != null)
        {
            missiles.remove(missile);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event)
    {
        final Level level = event.getLevel();
        final Set<EntityMissile<?>> missiles = TRACKED.get(level);
        if (missiles == null)
        {
            // Reconcile persisted tickets on the first tick after startup as well as after the last missile disappears.
            syncEntityKeepaliveTicket(level, false);
            return;
        }
        if (missiles.isEmpty())
        {
            TRACKED.remove(level, missiles);
            syncEntityKeepaliveTicket(level, false);
            return;
        }
        // Entity flight tickets alone are not enough to reset ServerLevel's empty timer. Keep a separate persistent
        // ticking ticket while any physical missile remains, so far launches continue when every player logs off.
        syncEntityKeepaliveTicket(level, true);

        // The chunks all missiles of the level hold together are capped, each takes from what is left
        int budget = ConfigMissile.MAX_MISSILE_CHUNK_TICKETS;
        for (EntityMissile<?> missile : missiles)
        {
            if (missile.isRemoved())
            {
                missiles.remove(missile);
                continue;
            }
            final MissileChunkLoader loader = missile.getChunkLoader();
            loader.update(missile, budget);
            if (event.getLevel() instanceof ServerLevel serverLevel)
            {
                loader.traceStatus(missile, serverLevel, serverLevel.getServer().getTickCount());
            }
            budget -= loader.heldCount();
        }
    }

    private static void syncEntityKeepaliveTicket(Level level, boolean required)
    {
        if (!(level instanceof ServerLevel serverLevel))
        {
            return;
        }
        final Boolean previous = ENTITY_KEEPALIVE_ACTIVE.get(level);
        if (previous != null && previous.booleanValue() == required)
        {
            return;
        }
        try
        {
            final boolean changed = MissileTrackerHandler.SIMULATION_KEEPALIVE_TICKETS.forceChunk(
                serverLevel, ENTITY_KEEPALIVE_OWNER, 0, 0, required, true);
            ENTITY_KEEPALIVE_ACTIVE.put(level, required);
            ICBMDiagnostics.info("MISSILE_ENTITY_KEEPALIVE action={} dim={} tracked={} changed={}",
                required ? "ADD_OR_KEEP" : "REMOVE", level.dimension().location(),
                TRACKED.getOrDefault(level, Set.of()).size(), changed);
        }
        catch (RuntimeException exception)
        {
            // Do not cache a failed reconciliation; retry on the next level tick.
            ICBMDiagnostics.error("MISSILE_ENTITY_KEEPALIVE_FAILED dim=" + level.dimension().location()
                + " required=" + required, exception);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event)
    {
        if (event.getLevel() instanceof Level level)
        {
            TRACKED.remove(level);
            // Keep the persistent ticket until the next server startup can reconcile it against loaded missile data.
            ENTITY_KEEPALIVE_ACTIVE.remove(level);
        }
    }

    /**
     * @return true for missiles flying under their own power with nothing to take them through unloaded chunks, and
     * for missiles the tracker refused to take
     */
    public static boolean shouldLoad(EntityMissile<?> missile)
    {
        if (!ConfigMissile.DIRECT_FIRE_CHUNK_LOADING || missile.isRemoved())
        {
            return false;
        }

        // Always let a newly spawned missile execute its initialization tick. The launcher may be thousands of
        // blocks from every player, and its first velocity/path is often still zero at this point.
        if (missile.tickCount == 0)
        {
            return true;
        }

        if (!missile.getMissileCapability().canRunFlightLogic())
        {
            return false;
        }

        final IMissileFlightLogic logic = missile.getMissileCapability().getFlightLogic();
        if (logic == null)
        {
            // Keep the entity's current chunk ticking long enough for the capability to initialize its next logic.
            return true;
        }
        final boolean moving = missile.getDeltaMovement().lengthSqr() >= 1.0E-4;

        // A missile may use a wrapper chain (WarmupFlightLogic -> MoveByFacingLogic -> ArcFlightLogic). Every stage
        // before the arc logic must have ticking chunks available or it never reaches the simulation hand-off.
        if (!(logic instanceof ISimulatedFlight flight))
        {
            // Direct-firing missiles and SAMs cannot use the ballistic tracker; keep renewing their path tickets until
            // impact/removal. A stopped missile that has explicitly transitioned to DeadFlightLogic has no path ahead.
            return !(logic instanceof DeadFlightLogic) || moving;
        }

        // An arc missile is offered to the tracker only after reaching SIMULATION_EXIT_HEIGHT or when its next path
        // segment would cross a non-ticking chunk. The old code only held tickets after a refused hand-off or while a
        // player watched the missile, so a far-away missile could stall before it ever reached the hand-off point.
        // Keep its trajectory ticking until the hand-off can actually happen. A successful hand-off removes/untracks
        // the entity on that tick; a refused hand-off sets isSimulationRefused() and keeps path tickets for retries.
        return MissileChunkLoaderPolicy.shouldKeepArcFlightTickets(
            flight.isSimulationRefused(),
            MissileTrackerHandler.isWatched(missile),
            MissileTrackerHandler.canSimulate(missile),
            missile.getMissileCapability().getTargetData() != null,
            missile.getY(),
            ConfigMissile.SIMULATION_EXIT_HEIGHT);
    }
}
