package com.createicbm.content.missile.tracker;

import com.createicbm.ICBMConstants;
import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.missile.logic.flight.DeadFlightLogic;
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

    /** Chunks on the path this missile is holding, kept while on the path because they only tick thanks to us */
    private final LongSet heldChunks = new LongOpenHashSet();
    private final LongSet wanted = new LongOpenHashSet();
    private int cooldown = 0;
    private long lastChunk = Long.MIN_VALUE;

    /**
     * @return how many chunks this missile holds tickets for right now
     */
    public int heldCount()
    {
        return heldChunks.size();
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
            level.getChunkSource().addRegionTicket(TICKET, new ChunkPos(it.nextLong()), TICKET_DISTANCE, owner);
        }
    }

    private boolean needsTicket(ServerLevel level, long key)
    {
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
        final Set<EntityMissile<?>> missiles = TRACKED.get(event.getLevel());
        if (missiles == null)
        {
            return;
        }
        if (missiles.isEmpty())
        {
            TRACKED.remove(event.getLevel(), missiles);
            return;
        }
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
            budget -= loader.heldCount();
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event)
    {
        if (event.getLevel() instanceof Level level)
        {
            TRACKED.remove(level);
        }
    }

    /**
     * @return true for missiles flying under their own power with nothing to take them through unloaded chunks, and
     * for missiles the tracker refused to take
     */
    public static boolean shouldLoad(EntityMissile<?> missile)
    {
        if (!ConfigMissile.DIRECT_FIRE_CHUNK_LOADING || missile.isRemoved() || !missile.getMissileCapability().canRunFlightLogic())
        {
            return false;
        }
        // A missile that came to rest has no path to load
        if (missile.getDeltaMovement().lengthSqr() < 1.0E-4)
        {
            return false;
        }
        final IMissileFlightLogic logic = missile.getMissileCapability().getFlightLogic();
        return logic == null || logic instanceof DeadFlightLogic
            || logic instanceof ISimulatedFlight flight && (flight.isSimulationRefused() || MissileTrackerHandler.isWatched(missile));
    }
}
