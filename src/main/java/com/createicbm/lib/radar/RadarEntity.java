package com.createicbm.lib.radar;

import com.createicbm.api.data.IWorldPosition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * Special type of weak reference used to track radar objects. This prevents the radar system from holding on to
 * references that should be unloaded from the map.
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/5/2016.
 */
public class RadarEntity implements IWorldPosition
{
    public Entity entity;

    public RadarEntity(Entity referent)
    {
        this.entity = referent;
    }

    public boolean isValid()
    {
        return entity != null && entity.isAlive() && entity.level() != null;
    }

    @Override
    public Level world()
    {
        return entity != null ? entity.level() : null;
    }

    @Override
    public double x()
    {
        return entity != null ? entity.getX() : 0;
    }

    @Override
    public double y()
    {
        return entity != null ? entity.getY() : 0;
    }

    @Override
    public double z()
    {
        return entity != null ? entity.getZ() : 0;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == this)
        {
            return true;
        }
        if (object instanceof RadarEntity && ((RadarEntity) object).isValid())
        {
            return ((RadarEntity) object).entity == entity || ((RadarEntity) object).entity != null && entity != null && ((RadarEntity) object).entity.getId() == entity.getId();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        if (entity != null)
        {
            return entity.hashCode();
        }
        return super.hashCode();
    }

    public ChunkPos getChunkPos()
    {
        return new ChunkPos(Mth.floor(x()) >> 4, Mth.floor(z()) >> 4);
    }
}
