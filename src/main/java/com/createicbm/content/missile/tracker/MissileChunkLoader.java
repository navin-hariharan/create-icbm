package com.createicbm.content.missile.tracker;

import com.createicbm.ICBMConstants;
import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.missile.logic.flight.DeadFlightLogic;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

/**
 * Keeps the chunks just ahead of a straight flying missile loaded and ticking, the way vanilla does for thrown ender
 * pearls.
 * <p>
 * Ballistic and arc missiles hand over to the missile tracker before they reach a chunk where entities don't tick.
 * Missiles fired straight (handheld and cruise launchers, see {@link DeadFlightLogic}) have nothing to hand over to,
 * so they used to fly into the first chunk past the simulation distance and freeze there until a player came close.
 * <p>
 * Tickets time out on their own after {@link #TICKET_TICKS}, so nothing is saved, nothing leaks when a missile is
 * removed or the server stops, and chunks load in the background instead of on the server thread.
 */
public class MissileChunkLoader
{
    /** Ticks a ticket lives without being renewed */
    private static final int TICKET_TICKS = 100;
    /** Ticks between path checks, tickets are renewed well before they time out */
    private static final int REFRESH_TICKS = 5;
    /** Ticks of flight looked ahead, at least {@link #MIN_LOOK_AHEAD} and at most {@link #MAX_LOOK_AHEAD} meters */
    private static final double LOOK_AHEAD_TICKS = 30;
    private static final double MIN_LOOK_AHEAD = 48;
    private static final double MAX_LOOK_AHEAD = 160;
    /** Distance (meters) between path samples, less than a chunk so none is skipped */
    private static final double SAMPLE_STEP = 8;

    /** Distance 2 gives ticket level 31, the level where entities tick */
    private static final int TICKET_DISTANCE = 2;

    public static final TicketType<Integer> TICKET = TicketType.create(ICBMConstants.DOMAIN + ":missile_flight", Integer::compareTo, TICKET_TICKS);

    /** Chunks on the path this missile is holding, kept while on the path because they only tick thanks to us */
    private final LongSet heldChunks = new LongOpenHashSet();
    private final LongSet wanted = new LongOpenHashSet();
    private int cooldown = 0;
    private long lastChunk = Long.MIN_VALUE;

    /**
     * Called each server tick while the missile is alive
     */
    public void update(EntityMissile<?> missile)
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
        addIfNeeded(level, missile.getX(), missile.getZ());
        if (speed > 1.0E-4)
        {
            final double distance = Mth.clamp(speed * LOOK_AHEAD_TICKS, MIN_LOOK_AHEAD, MAX_LOOK_AHEAD);
            final double dirX = motion.x / speed;
            final double dirZ = motion.z / speed;
            for (double d = SAMPLE_STEP; d < distance + SAMPLE_STEP; d += SAMPLE_STEP)
            {
                final double step = Math.min(d, distance);
                addIfNeeded(level, missile.getX() + dirX * step, missile.getZ() + dirZ * step);
            }
        }

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

    private void addIfNeeded(ServerLevel level, double x, double z)
    {
        final int chunkX = Mth.floor(x) >> 4;
        final int chunkZ = Mth.floor(z) >> 4;
        final long key = ChunkPos.asLong(chunkX, chunkZ);
        if (heldChunks.contains(key) || !MissileTrackerHandler.canEntitiesTick(level, chunkX, chunkZ))
        {
            wanted.add(key);
        }
    }

    /**
     * @return true for missiles flying under their own power with nothing to take them through unloaded chunks
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
        return logic == null || logic instanceof DeadFlightLogic;
    }
}
