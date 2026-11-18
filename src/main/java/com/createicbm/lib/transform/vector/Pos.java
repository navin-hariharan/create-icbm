package com.createicbm.lib.transform.vector;

import com.createicbm.jlib.data.vector.IPos3D;
import com.createicbm.api.data.IWorldPosition;
import com.createicbm.lib.NBTConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;

/**
 * Basic implementation of Pos3D that contains helper methods for interacting with MC worlds
 * Created by Robin on 1/13/2015.
 */
@Deprecated //Replace with vec3d and helper classes as needed
public class Pos extends AbstractPos<Pos> implements IPos3D
{
    public Pos()
    {
        this(0, 0, 0);
    }

    public Pos(double a)
    {
        this(a, a, a);
    }

    public Pos(double x, double y, double z)
    {
        super(x, y, z);
    }

    public Pos(double yaw, double pitch)
    {
        this(-Math.sin(Math.toRadians(yaw)), Math.sin(Math.toRadians(pitch)), -Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
    }

    public Pos(BlockEntity tile)
    {
        this(tile.getBlockPos());
    }

    public Pos(Entity entity)
    {
        this(entity.getX(), entity.getY(), entity.getZ());
    }

    public Pos(IPos3D vec)
    {
        this(vec.x(), vec.y(), vec.z());
    }

    public Pos(IWorldPosition vec)
    {
        this(vec.x(), vec.y(), vec.z());
    }

    public Pos(CompoundTag nbt)
    {
        this(nbt.getDouble(NBTConstants.X), nbt.getDouble(NBTConstants.Y), nbt.getDouble(NBTConstants.Z));
    }

    public Pos(ByteBuf data)
    {
        this(data.readDouble(), data.readDouble(), data.readDouble());
    }

    public Pos(BlockPos par1)
    {
        this(par1.getX(), par1.getY(), par1.getZ());
    }

    public Pos(Direction dir)
    {
        this(dir.getNormal());
    }

    public Pos(Vec3 vec)
    {
        this(vec.x, vec.y, vec.z);
    }

    public Pos(Vec3i vec)
    {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public Pos newPos(double x, double y, double z)
    {
        return new Pos(x, y, z);
    }

    @Override
    public boolean equals(Object object)
    {
        return object instanceof IPos3D && Math.abs(((IPos3D) object).x() - x()) <= 0.001 && Math.abs(((IPos3D) object).y() - y()) <= 0.001 && Math.abs(((IPos3D) object).z() - z()) <= 0.001;
    }

    @Override
    public int hashCode()
    {
        long x = Double.doubleToLongBits(this.x() * 100 / 100);
        long y = Double.doubleToLongBits(this.y() * 100 / 100);
        long z = Double.doubleToLongBits(this.z() * 100 / 100);
        long hash = (x ^ (x >>> 32));
        hash = 31 * hash + y ^ (y >>> 32);
        hash = 31 * hash + z ^ (z >>> 32);
        return (int) hash;
    }
}
