package com.createicbm.mods.sable;

import com.createicbm.mods.ModProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Ship support for Sable (the physics engine behind Create Aeronautics). Sable keeps each ship's blocks in a far away
 * "plot" of the same level and draws them where the ship is. The helpers here move such positions back to where the
 * ship is in the world and let blasts damage nearby ships.
 * <p>
 * Without Sable this class does nothing: every position is in the world and no block is on a ship. The Sable code in
 * {@link SableCompat} is only loaded when Sable is installed.
 */
public class SableProxy
{
    public static final String MOD_ID = "sable";

    public static final SableProxy INSTANCE = ModProxy.select(MOD_ID, SableCompat::new, SableProxy::new);

    /** Where a ship is in the world at the moment it was looked up, see {@link #getShipFrame(Level, Vec3)} */
    public interface ShipFrame
    {
        /** @return world position of a position on the ship (in its plot) */
        Vec3 toWorld(Vec3 plotPos);

        /** @return position on the ship (in its plot) of a world position */
        Vec3 toShip(Vec3 worldPos);

        /** @return world direction of a direction on the ship, same length as the input */
        Vec3 directionToWorld(Vec3 direction);

        /** @return velocity in meters per tick of a spot on the ship, given in plot space */
        Vec3 velocityAt(Vec3 plotPos);
    }

    protected SableProxy()
    {
    }

    /** @return true when Sable is installed and its API was found */
    public boolean isEnabled()
    {
        return false;
    }

    /** @return true if the position is inside the plot grid where Sable stores ship blocks */
    public boolean isInPlot(Level level, double x, double z)
    {
        return false;
    }

    /** @return world position of a position in a ship's plot, the same position if it isn't on a ship */
    public Vec3 toWorld(Level level, Vec3 pos)
    {
        return pos;
    }

    /** @return velocity (meters per tick) of the ship the entity stands on or rides, zero if none */
    public Vec3 getShipVelocity(Entity entity)
    {
        return Vec3.ZERO;
    }

    /**
     * Finds the ship that holds a plot position, for turning positions, directions and speeds between the ship and
     * the world
     *
     * @param plotPos any position inside the ship's plot, usually a block of the ship
     * @return the ship's frame, null when the position is not on a ship
     */
    @Nullable
    public ShipFrame getShipFrame(Level level, Vec3 plotPos)
    {
        return null;
    }

    /**
     * Blasts edit the world's blocks, ships nearby live in their plots and would come out untouched. Breaks the blocks
     * of ships near a blast.
     */
    public void damageShips(Level level, @Nullable Entity source, double x, double y, double z, float radius)
    {
    }

    /**
     * Block hits on a ship come back from ray traces in plot space. This moves the hit location to the world and keeps
     * the block position, so the block can still be looked up.
     */
    public final BlockHitResult toWorld(Level level, BlockHitResult hit)
    {
        if (hit.getType() == HitResult.Type.MISS)
        {
            return hit;
        }
        final Vec3 location = toWorld(level, hit.getLocation());
        return location == hit.getLocation() ? hit : new BlockHitResult(location, hit.getDirection(), hit.getBlockPos(), hit.isInside());
    }

    /** @see #toWorld(Level, BlockHitResult) */
    public final HitResult toWorld(Level level, HitResult hit)
    {
        return hit instanceof BlockHitResult blockHit ? toWorld(level, blockHit) : hit;
    }

    /**
     * Moves an entity created on a ship (in plot space) to the world, so its flight path is worked out from where it
     * really is. Catches missiles launched by other mods from a ship.
     */
    public final void moveToWorld(Entity entity)
    {
        final Vec3 pos = entity.position();
        final Vec3 world = toWorld(entity.level(), pos);
        if (world != pos)
        {
            entity.setPos(world.x, world.y, world.z);
            entity.xo = world.x;
            entity.yo = world.y;
            entity.zo = world.z;
        }
    }

    /** @return true if the block is part of a Sable ship (it lives in a plot) */
    public final boolean isOnShip(Level level, BlockPos pos)
    {
        return level != null && isInPlot(level, pos.getX() + 0.5, pos.getZ() + 0.5);
    }

    /** @return where the center of the block is in the world, its own center when it is not on a ship */
    public final Vec3 worldCenter(Level level, BlockPos pos)
    {
        final Vec3 center = Vec3.atCenterOf(pos);
        return level == null ? center : toWorld(level, center);
    }

    /**
     * Brings a world position onto the ship a block belongs to, so it can be compared with the block's own position.
     * Used by renderers of other mods that measure the player's distance to a block: a block on a ship is stored in
     * the ship's plot, far away from the player.
     *
     * @return the position in the ship's plot, the same position if the block isn't on a ship
     */
    public final Vec3 toShipOf(Level level, BlockPos blockOnShip, Vec3 worldPos)
    {
        final ShipFrame frame = getShipFrame(level, Vec3.atCenterOf(blockOnShip));
        return frame == null ? worldPos : frame.toShip(worldPos);
    }
}
