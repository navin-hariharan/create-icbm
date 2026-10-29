package com.createicbm.content.missile.tracker;

import com.createicbm.CreateICBM;
import com.createicbm.api.events.MissileChunkEvent;
import com.createicbm.config.ConfigDebug;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.missile.entity.explosive.EntityExplosiveMissile;
import com.createicbm.content.missile.logic.flight.DeadFlightLogic;
import com.createicbm.lib.NBTConstants;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.TicketController;

import java.util.UUID;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Per world handler for tracking and simulating missiles
 *
 * Created by GHXX on 7/31/2018.
 */
public class MissileTrackerWorld extends SavedData
{
    //Constants
    private static final int unloadChunkCooldown = 60; //1 minute
    private static final int preLoadChunkTimer = 5; //5 update ticks (5 seconds) / the time that we wait before spawning the missile in a force-loaded chunk
    private static final int SPAWN_RETRIES_BEFORE_WARNING = 6; //Warn after several seconds waiting for target chunks
    private static final int SPAWN_RETRY_WARNING_INTERVAL = 6; //With the existing five-second retry delay, warn about every 30 seconds
    private static final int MAX_SPAWN_REFUSALS = 6; //A world that repeatedly refuses an entity spawn can lose the malformed missile
    /** Temporary target tickets are refreshed every simulated second during the configured preload window. */
    private static final int TARGET_PRELOAD_TICKET_TICKS = 400; //20 seconds; expires naturally if no missile refreshes it
    private static final int TARGET_TICKET_DISTANCE = 2; //Entity-ticking ticket level on Minecraft 1.21.1
    private static final TicketType<Long> TARGET_PRELOAD_TICKET = TicketType.create(
        "create_icbm:missile_target_preload", Long::compareTo, TARGET_PRELOAD_TICKET_TICKS);

    //Missile lists
    private final LinkedList<MissileTrackerData> missileList;         //Stores missiles which are being simulated right now
    private final LinkedList<MissileTrackerData> missileSpawnList;    //Stores missiles which are awaiting to be spawned in world

    private static final UUID TICKET_OWNER = UUID.nameUUIDFromBytes("create_icbm:missile_tracker".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    //Chunk stuff
    private TicketController chunkLoadTicket;   //The chunkloading ticket used for loading chunks
    // PERF: keyed by chunk, was a LinkedList walked with get(i) for every forced chunk (quadratic with many arrivals)
    private final LinkedHashMap<ChunkPos, LoadedChunkPair> currentLoadedChunks;    //Stores the currently loaded chunks along with a timer how long they will be loaded for

    /** Stable owner/location for the one ticking ticket that keeps an otherwise empty dimension advancing. */
    private static final BlockPos SIMULATION_KEEPALIVE_OWNER = BlockPos.ZERO;
    private boolean simulationKeepaliveStateKnown = false;
    private boolean simulationKeepaliveActive = false;

    //Tick counter for reducing the simulation speed
    private int ticks = 0;

    public MissileTrackerWorld()
    {
        missileList = new LinkedList<>();
        currentLoadedChunks = new LinkedHashMap<>();
        missileSpawnList = new LinkedList<>();
    }

    public static MissileTrackerWorld load(CompoundTag nbt, HolderLookup.Provider provider)
    {
        final MissileTrackerWorld tracker = new MissileTrackerWorld();
        tracker.readFromNBT(nbt);
        return tracker;
    }

    /**
     * Called to simulate the missile
     * @param missile
     * @return true if the missile left the world for the simulation, false if it has no target to fly to
     */
    boolean simulateMissile(EntityMissile<?> missile)
    {
        if(ConfigDebug.DEBUG_MISSILE_TRACKER) {
            final String formatted = String.format("MissileTracker[%s]: Simulating missile: %s",
                missile.level().dimension().location(),
                missile
            );
            CreateICBM.logger().info(formatted);
        }

        //Only run on server
        if (!missile.level().isClientSide() && missile.getMissileCapability().getTargetData()  != null)
        {
            //Clear flight logic, once we are out of simulation the computer is dead
            missile.getMissileCapability().setFlightLogic(null); //TODO create custom sim-logic flight logic to better handle re-entry

            final MissileTrackerData mtd = new MissileTrackerData(missile);

            //Calculate distance
            double dx = missile.getMissileCapability().getTargetData().getX() - missile.getX();
            double dz = missile.getMissileCapability().getTargetData().getZ() - missile.getZ();
            double dst = Math.sqrt(dx * dx + dz * dz);

            //Calculate duration and queue up
            mtd.ticksLeftToTarget = (int) Math.round(dst / ConfigMissile.SIMULATION_FLIGHT_SPEED);
            mtd.totalTicks = mtd.ticksLeftToTarget;
            missileList.add(mtd);

            //Destroys the entity and marks it for removal from world, the missile flies on in the simulation
            missile.leaveForSimulation();
            missile.discard();

            //Mark that we need to save
            this.setDirty();
            return true;
        }
        return false;
    }

    /**
     * Visits every missile being simulated or waiting to spawn at its target, read only
     */
    public void forEachSimulated(Consumer<MissileTrackerData> visitor)
    {
        missileList.forEach(visitor);
        missileSpawnList.forEach(visitor);
    }

    /**
     * Called each time the world ticks
     */
    public void onWorldTick(final Level world)
    {
        // A forced ticking ticket prevents the world's empty-time optimization from stopping a long flight when
        // everyone logs off. Do this every level tick, including ticks where the one-second simulation loop skips.
        syncSimulationKeepaliveTicket(world);

        if (++ticks >= 20)  //Run every 20 ticks = 1 second
        {
            ticks = 0;
            // Only the missile lists are saved, nothing to mark dirty when both are empty
            final boolean hasMissiles = !missileList.isEmpty() || !missileSpawnList.isEmpty();
            int mIndex = 0;
            //Loop through all simulated missiles
            final Iterator<MissileTrackerData> missileIterator = missileList.iterator();
            while(missileIterator.hasNext())
            {
                //Get current missile
                MissileTrackerData missile = missileIterator.next();
                if (missile.ticksLeftToTarget <= 0) //If missile is at the target location
                {
                    forceTargetChunks(world, missile);

                    missile.preLoadChunkTimer = preLoadChunkTimer;
                    missileSpawnList.add(missile);
                    missileIterator.remove();
                }
                else //If we aren't at the target location then simulate it for another tick
                {
                    missile.ticksLeftToTarget--;

                    // Start loading the destination well before arrival. Busy servers may need several seconds to
                    // generate a remote target area; requesting tickets only at arrival can make the spawned missile
                    // wait in a non-ticking chunk and lose collision, radar, guidance, or explosion behaviour.
                    if (ConfigMissile.TARGET_CHUNK_PRELOAD_SECONDS > 0
                        && missile.ticksLeftToTarget <= ConfigMissile.TARGET_CHUNK_PRELOAD_SECONDS)
                    {
                        // Renew expiring region tickets once per simulated second. addRegionTicket queues the chunks
                        // through Minecraft's normal ticket manager, so chunk generation can start before arrival
                        // instead of synchronously forcing five fresh chunks on the impact tick.
                        preloadTargetChunks(world, missile);
                    }

                    if(ConfigDebug.DEBUG_MISSILE_TRACKER)
                        CreateICBM.logger().info("MissileTracker Missile ["+mIndex+"]: Simulation ticks left: "+missile.ticksLeftToTarget);
                }
                mIndex++;
            }

            //Check forced chunks, decrease lifetime and maybe unforce
            final Iterator<Map.Entry<ChunkPos, LoadedChunkPair>> chunkIterator = currentLoadedChunks.entrySet().iterator();
            while (chunkIterator.hasNext())
            {
                final Map.Entry<ChunkPos, LoadedChunkPair> entry = chunkIterator.next();
                ChunkPos chunkPos = entry.getValue().chunkPos;
                int waitTime = entry.getValue().timeLeft - 1;
                if (waitTime <= 0)
                {
                    if(!NeoForge.EVENT_BUS.post(new MissileChunkEvent.Unload(new LoadedChunkPair(chunkPos, waitTime), chunkLoadTicket)).isCanceled())
                    {
                        unforceChunk(world, chunkPos, chunkLoadTicket);
                        chunkIterator.remove();
                    }
                }
                else
                {
                    entry.setValue(new LoadedChunkPair(chunkPos, waitTime));
                }
            }

            //Check missile spawn-queue
            final Iterator<MissileTrackerData> spawnIterator = missileSpawnList.iterator();
            while(spawnIterator.hasNext()) // TODO wait for callback maybe instead of waiting a set amount of time
            {
                final MissileTrackerData mtd = spawnIterator.next();

                //Decrease timer
                mtd.preLoadChunkTimer--;

                //Check if ready to launch
                if (mtd.preLoadChunkTimer <= 0)
                {
                    // Never respawn an entity in a chunk where entities are not ticking: it can immediately freeze
                    // and lose collision, SAM, radar, or explosion behaviour. Keep waiting if a chunk-loading mod
                    // rejects the ticket or world generation is slow. The retry timer keeps this asynchronous and
                    // non-blocking; the warning is rate-limited instead of silently spawning a frozen missile.
                    if (world instanceof ServerLevel serverLevel
                        && !MissileTrackerHandler.canEntitiesTick(serverLevel, targetChunkX(mtd), targetChunkZ(mtd)))
                    {
                        mtd.spawnRetries++;
                        forceTargetChunks(world, mtd);
                        if (mtd.spawnRetries == SPAWN_RETRIES_BEFORE_WARNING
                            || (mtd.spawnRetries > SPAWN_RETRIES_BEFORE_WARNING
                                && mtd.spawnRetries % SPAWN_RETRY_WARNING_INTERVAL == 0))
                        {
                            CreateICBM.logger().warn(
                                "MissileTracker is still waiting for target chunk [{}, {}] in dimension {} to tick entities after {} retries. Check chunk-ticket limits and third-party chunk-loading mods; the missile will remain queued instead of being spawned into a frozen chunk.",
                                targetChunkX(mtd), targetChunkZ(mtd), world.dimension().location(), mtd.spawnRetries);
                        }
                        mtd.preLoadChunkTimer = preLoadChunkTimer;
                        continue;
                    }
                    mtd.spawnRetries = 0;

                    //Load missile into world, one that can't be built (bad save data, a mod removed) must not crash the
                    //world tick every second and stay in the save forever
                    boolean spawned;
                    try
                    {
                        spawned = spawnMissileOnDestination(world, mtd);
                    }
                    catch (RuntimeException e)
                    {
                        CreateICBM.logger().error("MissileTracker[{}]: failed to spawn the missile arriving at {}, it is lost",
                            world.dimension().location(), mtd.targetPos, e);
                        spawnIterator.remove();
                        continue;
                    }
                    if (!spawned)
                    {
                        // The world refused the missile, try again a few times before giving up on it
                        if (mtd.spawnRefusals < MAX_SPAWN_REFUSALS)
                        {
                            mtd.spawnRefusals++;
                            mtd.preLoadChunkTimer = preLoadChunkTimer;
                            continue;
                        }
                        CreateICBM.logger().warn("MissileTracker[{}]: the world refused the missile arriving at {}, it is lost",
                            world.dimension().location(), mtd.targetPos);
                    }

                    //Remove
                    spawnIterator.remove();
                }
            }

            //Mark so we save
            if (hasMissiles)
            {
                this.setDirty();
            }
            // Lists may have become empty after their last missile arrived, or gained a destination spawn above.
            syncSimulationKeepaliveTicket(world);
        }
    }

    /**
     * Reconciles the persistent ticking ticket with simulated-flight and destination-spawn state. Called during level
     * load as well as normal ticks, so a stale ticket is removed on startup and a saved long flight resumes offline.
     */
    public void syncSimulationKeepaliveTicket(Level world)
    {
        if (!(world instanceof ServerLevel serverLevel))
        {
            return;
        }

        final boolean required = !missileList.isEmpty() || !missileSpawnList.isEmpty() || !currentLoadedChunks.isEmpty();
        if (simulationKeepaliveStateKnown && required == simulationKeepaliveActive)
        {
            return;
        }

        try
        {
            final boolean changed = MissileTrackerHandler.SIMULATION_KEEPALIVE_TICKETS.forceChunk(
                serverLevel, SIMULATION_KEEPALIVE_OWNER, 0, 0, required, true);
            simulationKeepaliveStateKnown = true;
            simulationKeepaliveActive = required;
            ICBMDiagnostics.info("MISSILE_TRACKER_KEEPALIVE action={} dim={} simulated={} awaitingSpawn={} targetChunks={} changed={}",
                required ? "ADD_OR_KEEP" : "REMOVE", serverLevel.dimension().location(), missileList.size(),
                missileSpawnList.size(), currentLoadedChunks.size(), changed);
        }
        catch (RuntimeException exception)
        {
            // Do not mark state as known on failure; retry on the next level tick.
            ICBMDiagnostics.error("MISSILE_TRACKER_KEEPALIVE_FAILED dim=" + serverLevel.dimension().location()
                + " required=" + required, exception);
        }
    }

    /**
     * @return true if the world accepted the missile
     */
    private boolean spawnMissileOnDestination(final Level world, MissileTrackerData mtd)
    {
        //Create entity of the kind that left, a delivery missile used to come back as an explosive missile
        final EntityMissile<?> missile;
        if (EntityType.create(mtd.missileData, world).orElse(null) instanceof EntityMissile<?> saved)
        {
            missile = saved;
        }
        else
        {
            final EntityExplosiveMissile explosive = new EntityExplosiveMissile(world);
            explosive.load(mtd.missileData);
            missile = explosive;
        }
        //TODO calculate arc position so we don't come in on top of the target
        missile.setPos(mtd.targetPos.x(), ConfigMissile.SIMULATION_ENTER_HEIGHT, mtd.targetPos.z());
        missile.setMotionVector(0, -ConfigMissile.SIMULATION_ENTER_SPEED, 0); //TODO get speed it would have been at the given time
        missile.rotateTowardsMotion(1);

        // Change over to dead aim if we have no custom flight system
        if(missile.getMissileCapability().getFlightLogic() == null) {
            missile.getMissileCapability().setFlightLogic(new DeadFlightLogic(100));
        }

        //Trigger launch event
        missile.getMissileCapability().launch();

        //Spawn entity
        if (!missile.world().addFreshEntity(missile))
        {
            // Nothing flew, take the radar contact and launch sound back
            missile.getMissileCapability().cancelLaunch();
            return false;
        }

        if(ConfigDebug.DEBUG_MISSILE_TRACKER)
            CreateICBM.logger().info("MissileTracker[" + missile.level().dimension().location() + "]: Missile spawned by missile tracker: " + missile);
        return true;
    }

    private static int targetChunkX(MissileTrackerData mtd)
    {
        return Mth.floor(mtd.targetPos.x()) >> 4;
    }

    private static int targetChunkZ(MissileTrackerData mtd)
    {
        return Mth.floor(mtd.targetPos.z()) >> 4;
    }

    /**
     * Asynchronously warms the target chunk and its four cardinal neighbours using ordinary server region tickets.
     * The ticket owner is the target center chunk, deduplicating repeated requests from missiles aimed at the same area.
     * Tickets expire automatically after 20 seconds without a refresh, so aborted flights cannot leak tickets.
     */
    private void preloadTargetChunks(Level world, MissileTrackerData mtd)
    {
        if (!(world instanceof ServerLevel serverLevel))
        {
            return;
        }

        final int chunkX = targetChunkX(mtd);
        final int chunkZ = targetChunkZ(mtd);
        final ChunkPos center = new ChunkPos(chunkX, chunkZ);
        final long owner = center.toLong();
        addPreloadTicket(serverLevel, center, owner);
        addPreloadTicket(serverLevel, new ChunkPos(chunkX + 1, chunkZ), owner);
        addPreloadTicket(serverLevel, new ChunkPos(chunkX - 1, chunkZ), owner);
        addPreloadTicket(serverLevel, new ChunkPos(chunkX, chunkZ + 1), owner);
        addPreloadTicket(serverLevel, new ChunkPos(chunkX, chunkZ - 1), owner);
    }

    private static void addPreloadTicket(ServerLevel level, ChunkPos pos, long owner)
    {
        level.getChunkSource().addRegionTicket(TARGET_PRELOAD_TICKET, pos, TARGET_TICKET_DISTANCE, owner);
    }

    /** Forces the target chunk and its four cardinal neighbours; this keeps the target preload bounded on busy servers. */
    private boolean forceTargetChunks(Level world, MissileTrackerData mtd)
    {
        if (chunkLoadTicket == null)
        {
            chunkLoadTicket = MissileTrackerHandler.CHUNK_TICKETS;
        }

        if (chunkLoadTicket == null || !(world instanceof ServerLevel))
        {
            CreateICBM.logger().warn("Unable to obtain a server chunk-loading ticket for a missile target in dimension {}.", world.dimension().location());
            return false;
        }

        final int chunkX = targetChunkX(mtd);
        final int chunkZ = targetChunkZ(mtd);
        boolean accepted = true;
        accepted &= forceChunk(world, new ChunkPos(chunkX, chunkZ), chunkLoadTicket);
        accepted &= forceChunk(world, new ChunkPos(chunkX + 1, chunkZ), chunkLoadTicket);
        accepted &= forceChunk(world, new ChunkPos(chunkX - 1, chunkZ), chunkLoadTicket);
        accepted &= forceChunk(world, new ChunkPos(chunkX, chunkZ + 1), chunkLoadTicket);
        accepted &= forceChunk(world, new ChunkPos(chunkX, chunkZ - 1), chunkLoadTicket);
        return accepted;
    }

    // Use NeoForge's shared server chunk-ticket manager. This composes with vanilla and other compatible chunk
    // loaders. Distant Horizons LOD data is rendering-only; the missile requires a real server ticking ticket for
    // collision, target acquisition, and impact effects.
    private boolean forceChunk(Level world, ChunkPos chunkPos, TicketController ticket)
    {
        final LoadedChunkPair existing = currentLoadedChunks.get(chunkPos);
        if (existing != null)
        {
            currentLoadedChunks.put(chunkPos, new LoadedChunkPair(existing.chunkPos, unloadChunkCooldown));
            return true;
        }

        LoadedChunkPair pair = new LoadedChunkPair(chunkPos, unloadChunkCooldown);
        if (NeoForge.EVENT_BUS.post(new MissileChunkEvent.Load(pair, ticket)).isCanceled())
        {
            return false;
        }

        if (world instanceof ServerLevel serverLevel
            && ticket.forceChunk(serverLevel, TICKET_OWNER, chunkPos.x, chunkPos.z, true, true))
        {
            currentLoadedChunks.put(chunkPos, pair);
            return true;
        }
        return false;
    }

    private void unforceChunk(Level world, ChunkPos chunkPos, TicketController ticket)
    {
        if (ticket != null && world instanceof ServerLevel serverLevel) {
            ticket.forceChunk(serverLevel, TICKET_OWNER, chunkPos.x, chunkPos.z, false, true);
        }
    }

    public void readFromNBT(CompoundTag nbt)
    {
        //Load missiles
        ListTag list = nbt.getList(NBTConstants.MISSILES, 10);
        for (int i = 0; i < list.size(); i++)
        {
            CompoundTag missileSave = list.getCompound(i);
            MissileTrackerData mtd = new MissileTrackerData(missileSave);
            missileList.add(mtd);
        }

        list = nbt.getList(NBTConstants.SPAWNS, 10);
        for (int i = 0; i < list.size(); i++)
        {
            CompoundTag missileSave = list.getCompound(i);
            MissileTrackerData mtd = new MissileTrackerData(missileSave);
            mtd.ticksLeftToTarget = 0;
            missileList.add(mtd);
        }
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider)
    {
        //Save missiles
        ListTag list = new ListTag();
        for (MissileTrackerData mtd : missileList)
        {
            CompoundTag compound = new CompoundTag();
            mtd.writeToNBT(compound);
            list.add(compound);
        }
        nbt.put(NBTConstants.MISSILES, list);

        //Save missiles that will spawn
        list = new ListTag();
        for (MissileTrackerData mtd : missileSpawnList)
        {
            CompoundTag compound = new CompoundTag();
            mtd.writeToNBT(compound);
            list.add(compound);
        }
        nbt.put(NBTConstants.SPAWNS, list);

        return nbt;
    }

    // Release local bookkeeping only. The simulated-flight lists are SavedData and must survive a world/server unload;
    // clearing them here silently discarded missiles that were still in flight when the server stopped.
    public void destroy()
    {
        chunkLoadTicket = null;
        currentLoadedChunks.clear();
        simulationKeepaliveStateKnown = false;
        simulationKeepaliveActive = false;
        if (!missileList.isEmpty() || !missileSpawnList.isEmpty())
        {
            this.setDirty();
        }
    }
}
