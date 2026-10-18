package com.createicbm.content.missile.tracker;

import com.createicbm.CreateICBM;
import com.createicbm.api.events.MissileChunkEvent;
import com.createicbm.config.ConfigDebug;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.missile.entity.explosive.EntityExplosiveMissile;
import com.createicbm.content.missile.logic.flight.DeadFlightLogic;
import com.createicbm.lib.NBTConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
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
    private static final int MAX_SPAWN_RETRIES = 6; //Extra preload waits if the target chunk is still not ticking entities

    //Missile lists
    private final LinkedList<MissileTrackerData> missileList;         //Stores missiles which are being simulated right now
    private final LinkedList<MissileTrackerData> missileSpawnList;    //Stores missiles which are awaiting to be spawned in world

    private static final UUID TICKET_OWNER = UUID.nameUUIDFromBytes("create_icbm:missile_tracker".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    //Chunk stuff
    private TicketController chunkLoadTicket;   //The chunkloading ticket used for loading chunks
    // PERF: keyed by chunk, was a LinkedList walked with get(i) for every forced chunk (quadratic with many arrivals)
    private final LinkedHashMap<ChunkPos, LoadedChunkPair> currentLoadedChunks;    //Stores the currently loaded chunks along with a timer how long they will be loaded for

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
     */
    void simulateMissile(EntityMissile<?> missile)
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
        }
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
        if (ticks++ >= 20)  //Run every 20 ticks = 1 second
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
                    // Wait for the forced target chunk to tick entities, a few retries at most so a cancelled
                    // MissileChunkEvent.Load can not hold the missile forever
                    if (mtd.spawnRetries < MAX_SPAWN_RETRIES && world instanceof ServerLevel serverLevel
                        && !MissileTrackerHandler.canEntitiesTick(serverLevel, targetChunkX(mtd), targetChunkZ(mtd)))
                    {
                        mtd.spawnRetries++;
                        forceTargetChunks(world, mtd);
                        mtd.preLoadChunkTimer = preLoadChunkTimer;
                        continue;
                    }

                    //Load missile into world
                    spawnMissileOnDestination(world, mtd);

                    //Remove
                    spawnIterator.remove();
                }
            }

            //Mark so we save
            if (hasMissiles)
            {
                this.setDirty();
            }
        }
    }

    private void spawnMissileOnDestination(final Level world, MissileTrackerData mtd)
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
        missile.world().addFreshEntity(missile);

        if(ConfigDebug.DEBUG_MISSILE_TRACKER)
            CreateICBM.logger().info("MissileTracker[" + missile.level().dimension().location() + "]: Missile spawned by missile tracker: " + missile);
    }

    private static int targetChunkX(MissileTrackerData mtd)
    {
        return Mth.floor(mtd.targetPos.x()) >> 4;
    }

    private static int targetChunkZ(MissileTrackerData mtd)
    {
        return Mth.floor(mtd.targetPos.z()) >> 4;
    }

    /** Forces the chunk the target location is in and the adjacent ones */
    private void forceTargetChunks(Level world, MissileTrackerData mtd)
    {
        if (chunkLoadTicket == null)
        {
            chunkLoadTicket = MissileTrackerHandler.CHUNK_TICKETS;
        }

        if (chunkLoadTicket != null) //If we are allowed to load chunks, lets load the chunk the target location is in and the adjacent ones
        {
            final int chunkX = targetChunkX(mtd);
            final int chunkZ = targetChunkZ(mtd);
            forceChunk(world, new ChunkPos(chunkX, chunkZ), chunkLoadTicket);
            forceChunk(world, new ChunkPos(chunkX + 1, chunkZ), chunkLoadTicket);
            forceChunk(world, new ChunkPos(chunkX - 1, chunkZ), chunkLoadTicket);
            forceChunk(world, new ChunkPos(chunkX, chunkZ + 1), chunkLoadTicket);
            forceChunk(world, new ChunkPos(chunkX, chunkZ - 1), chunkLoadTicket);
        }
        else
        {
            CreateICBM.logger().warn("Unable to receive chunkloading ticket. You could try to increase the maximum loaded chunks for ICBM.");
        }
    }

    //Helper method for forcing a chunk (chunkloading)
    private void forceChunk(Level world, ChunkPos chunkPos, TicketController ticket)
    {
        // check if the chunk that should be loaded is loaded already. If so then just reset the remaining time.
        final LoadedChunkPair existing = currentLoadedChunks.get(chunkPos);
        if (existing != null)
        {
            currentLoadedChunks.put(chunkPos, new LoadedChunkPair(existing.chunkPos, unloadChunkCooldown));
            return;
        }

        LoadedChunkPair pair = new LoadedChunkPair(chunkPos, unloadChunkCooldown);

        if(!NeoForge.EVENT_BUS.post(new MissileChunkEvent.Load(pair, ticket)).isCanceled())
        {
            currentLoadedChunks.put(chunkPos, pair);
            if (world instanceof ServerLevel serverLevel) {
                ticket.forceChunk(serverLevel, TICKET_OWNER, chunkPos.x, chunkPos.z, true, true);
            }
        }
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

    // clear buffers
    public void destroy()
    {
        this.missileList.clear();
        this.missileSpawnList.clear();

        chunkLoadTicket = null;
        currentLoadedChunks.clear();
    }
}
