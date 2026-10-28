package com.createicbm.lib.transform.vector;

import com.createicbm.jlib.data.vector.IPos3D;
import com.createicbm.api.data.IWorldPosition;
import com.createicbm.lib.NBTConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

@Deprecated
public class Location extends AbstractLocation<Location> implements IWorldPosition, IPos3D, Comparable<IWorldPosition>
{
    public Location(Level world, double x, double y, double z)
    {
        super(world, x, y, z);
    }

    public Location(CompoundTag nbt)
    {
        this(lookupWorld(readDimension(nbt)), nbt.getDouble(NBTConstants.X), nbt.getDouble(NBTConstants.Y), nbt.getDouble(NBTConstants.Z));
    }

    public Location(ByteBuf data)
    {
        super(data);
    }

    public Location(Entity entity)
    {
        this(entity.level(), entity.getX(), entity.getY(), entity.getZ());
    }

    public Location(BlockEntity tile)
    {
        super(tile);
    }

    public Location(IWorldPosition vec)
    {
        this(vec.world(), vec.x(), vec.y(), vec.z());
    }

    public Location(Level world, IPos3D vector)
    {
        this(world, vector.x(), vector.y(), vector.z());
    }

    public Location(Level world, BlockPos pos)
    {
        this(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public Location(Level world, Vec3 vec)
    {
        this(world, vec.x, vec.y, vec.z);
    }

    public Location(Level world, HitResult target)
    {
        this(world, target.getLocation());
    }

    @Override
    public Location newPos(double x, double y, double z)
    {
        return new Location(world, x, y, z);
    }

    public void playSound(SoundEvent soundIn, SoundSource category, float volume, float pitch, boolean distanceDelay)
    {
        world().playLocalSound(x(), y(), z(), soundIn, category, volume, pitch, distanceDelay);
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof IWorldPosition && this.world == ((IWorldPosition) o).world() && ((IWorldPosition) o).x() == x() && ((IWorldPosition) o).y() == y() && ((IWorldPosition) o).z() == z();
    }

    @Override
    public int compareTo(IWorldPosition that)
    {
        final int dimCompare = world().dimension().location().compareTo(that.world().dimension().location());
        if (dimCompare < 0 || x() < that.x() || y() < that.y() || z() < that.z())
        {
            return -1;
        }

        if (dimCompare > 0 || x() > that.x() || y() > that.y() || z() > that.z())
        {
            return 1;
        }

        return 0;
    }
}
