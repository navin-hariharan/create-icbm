package com.createicbm.lib.radar;

import com.createicbm.CreateICBM;
import com.createicbm.lib.transform.region.Cube;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * System designed to track moving or stationary targets on a 2D map. Can be used to detect objects or visualize objects in an area. Mainly
 * used to track flying objects that are outside of the map bounds(Missile in ICBM).
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 3/5/2016.
 */
public class RadarMap
{
    public static final int UPDATE_DELAY = 20;

    /** Dimension key, never change */
    public final ResourceKey<Level> dimID;

    /** Map of chunk coords( converted to long) to radar contacts in that chunk */
    private final HashMap<ChunkPos, List<RadarEntity>> chunk_to_entities = new HashMap();
    // PERF: set instead of list, contains/remove were linear and ran for every contact on add and every chunk move
    private final Set<RadarEntity> allEntities = new HashSet<>(); //TODO turn into wrapper array around chunk map

    private boolean accessingData = false;

    public int ticks = 0;

    /**
     * Dimension ID
     *
     * @param dimID - unique dimension that is not already tracked
     */
    public RadarMap(ResourceKey<Level> dimID)
    {
        this.dimID = dimID;
    }

    /**
     * Called at the end of every world tick to do checks on
     * data stored.
     */
    public void update()
    {
        if (ticks++ >= UPDATE_DELAY && !chunk_to_entities.isEmpty())
        {
            ticks = 0;
            //TODO consider multi-threading if number of entries is too high (need to ensure runs in less than 10ms~)

            this.accessingData = true;

            HashMap<RadarEntity, ChunkPos> removeList = new HashMap();
            List<RadarEntity> addList = new ArrayList();

            // Handle entities moving between chunks
            for (Map.Entry<ChunkPos, List<RadarEntity>> entry : chunk_to_entities.entrySet())
            {
                if (entry.getValue() != null)
                {
                    for (RadarEntity object : entry.getValue())
                    {
                        if (!object.isValid() || !entry.getKey().equals(object.getChunkPos()))
                        {
                            removeList.put(object, entry.getKey());
                            if (object.isValid())
                            {
                                addList.add(object);
                            }
                        }
                    }
                }
            }

            for (Map.Entry<RadarEntity, ChunkPos> entry : removeList.entrySet())
            {
                allEntities.remove(entry.getKey());
                List<RadarEntity> list = chunk_to_entities.get(entry.getValue());
                if (list != null)
                {
                    list.remove(entry.getKey());
                    if (!list.isEmpty())
                    {
                        chunk_to_entities.put(entry.getValue(), list);
                    }
                    else
                    {
                        chunk_to_entities.remove(entry.getValue());
                    }
                }
                else
                {
                    chunk_to_entities.remove(entry.getValue());
                }
            }

            this.accessingData = false;

            addList.forEach(this::add);
            allEntities.removeIf(object -> object == null || !object.isValid());
        }
    }

    public boolean add(Entity entity)
    {
        // Some reason nulls are finding their way into this call
        if(entity == null) {
            CreateICBM.logger().warn(this + ": Attempted to add null  entity", new RuntimeException());
            return false;
        }

        return add(new RadarEntity(entity));
    }

    public boolean add(@Nonnull RadarEntity object)
    {
        // Some reason nulls are finding their way into this call
        if(object == null) {
            CreateICBM.logger().warn(this + ": Attempted to add null radar entity", new RuntimeException());
            return false;
        }
        
        if(accessingData) {
            CreateICBM.logger().warn("Attempted to add radar entity mid access of entities", new RuntimeException());
            return false;
        }
        if (!allEntities.contains(object) && object.isValid())
        {
            allEntities.add(object);
            ChunkPos pair = object.getChunkPos();
            List<RadarEntity> list;

            //Get list or make new
            if (chunk_to_entities.containsKey(pair))
            {
                list = chunk_to_entities.get(pair);
            }
            else
            {
                list = new ArrayList();
            }

            //Check if object is not already added
            if (!list.contains(object))
            {
                list.add(object);
                //TODO fire map update event
                //TODO fire map add event
                //Update map
                chunk_to_entities.put(pair, list);
                return true;
            }
        }
        return false;
    }

    public boolean remove(Entity entity)
    {
        return remove(new RadarEntity(entity));
    }

    public boolean remove(RadarEntity object)
    {
        if(accessingData) {
            CreateICBM.logger().warn("Attempted to remove radar entity mid access of entities", new RuntimeException());
            return false;
        }

        ChunkPos pair = object.getChunkPos();
        allEntities.remove(object);
        if (chunk_to_entities.containsKey(pair))
        {
            List<RadarEntity> list = chunk_to_entities.get(pair);
            boolean b = list.remove(object);
            //TODO fire radar remove event
            //TODO fire map update event
            if (list.isEmpty())
            {
                chunk_to_entities.remove(pair);
            }
            return b;
        }
        return false;
    }

    /**
     * Removes all entries connected with the provided chunk location data
     *
     * @param chunk - should never be null
     */
    public void remove(ChunkAccess chunk)
    {
        if(accessingData) {
            CreateICBM.logger().warn("Attempted to add radar chunk mid access of entities", new RuntimeException());
            return;
        }

        ChunkPos pair = chunk.getPos();
        if (chunk_to_entities.containsKey(pair))
        {
            for (RadarEntity object : chunk_to_entities.get(pair))
            {
                //TODO fire remove event
                allEntities.remove(object);
            }
            chunk_to_entities.remove(pair);
        }
    }

    protected final ChunkPos getChunkValue(int x, int z)
    {
        return new ChunkPos(x >> 4, z >> 4);
    }

    public void unloadAll()
    {
        if(accessingData) {
            CreateICBM.logger().warn("Attempted to clear all mid access of entities", new RuntimeException());
            return;
        }
        chunk_to_entities.clear();
    }

    /**
     * Finds all contacts within chunk distances
     *
     * @param x        - world location x
     * @param z        - world location x
     * @param distance - distance m
     * @return list of entries
     */
    public List<RadarEntity> getRadarObjects(double x, double z, double distance)
    {
        return getRadarObjects(new Cube(x - distance, 0, z - distance, x + distance, CreateICBM.MAP_HEIGHT, z + distance).cropToWorld(), true);
    }

    protected List<RadarEntity> getEntitiesInChunk(int chunkX, int chunkZ)
    {
        ChunkPos p = new ChunkPos(chunkX, chunkZ);
        return chunk_to_entities.get(p);
    }

    public void collectEntitiesInChunk(int chunkX, int chunkZ, Consumer<RadarEntity> collector)
    {
        this.accessingData = true;
        final List<RadarEntity> objects = getEntitiesInChunk(chunkX, chunkZ);
        if (objects != null)
        {
            for (RadarEntity entity : objects)
            {
                if (entity != null && entity.isValid())
                {
                    collector.accept(entity);
                }
            }
        }
        this.accessingData = false;
    }

    /**
     * Finds all contacts within chunk distances
     *
     * @param cube  - area to search inside, approximated to chunk bounds
     * @param exact - match exact cube size, overrides approximation
     * @return list of entries
     */
    public List<RadarEntity> getRadarObjects(Cube cube, boolean exact)
    {
        final List<RadarEntity> list = new ArrayList();
        final int minChunkX = (cube.min().xi() >> 4) - 1;
        final int maxChunkX = (cube.max().xi() >> 4) + 1;
        final int minChunkZ = (cube.min().zi() >> 4) - 1;
        final int maxChunkZ = (cube.max().zi() >> 4) + 1;

        // PERF: a 500m radar covers ~4000 chunks, walk the chunks that have contacts instead when there are fewer of them
        final long areaChunks = (long) (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
        if (chunk_to_entities.size() < areaChunks)
        {
            if (exact)
            {
                this.accessingData = true;
                for (Map.Entry<ChunkPos, List<RadarEntity>> entry : chunk_to_entities.entrySet())
                {
                    final ChunkPos pos = entry.getKey();
                    if (entry.getValue() != null && pos.x >= minChunkX && pos.x <= maxChunkX && pos.z >= minChunkZ && pos.z <= maxChunkZ)
                    {
                        for (RadarEntity entity : entry.getValue())
                        {
                            if (entity != null && entity.isValid())
                            {
                                list.add(entity);
                            }
                        }
                    }
                }
                this.accessingData = false;
            }
            return list;
        }

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++)
        {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++)
            {
                collectEntitiesInChunk(chunkX, chunkZ, (entity) -> {
                    if (exact || exact && cube != null && cube.isWithin(entity))
                    {
                        list.add(entity);
                    }
                });
            }
        }
        return list;
    }

    /**
     * Dimension ID this map tracks
     *
     * @return valid dim ID.
     */
    public ResourceKey<Level> dimID()
    {
        return dimID;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == this)
        {
            return true;
        }
        else if (object instanceof RadarMap)
        {
            return ((RadarMap) object).dimID.equals(dimID);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "RadarMap[" + dimID + "]";
    }

    public boolean isEmpty() {
        return chunk_to_entities.isEmpty();
    }
}
