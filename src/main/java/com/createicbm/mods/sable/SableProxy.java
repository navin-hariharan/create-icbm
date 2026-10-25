package com.createicbm.mods.sable;

import com.createicbm.CreateICBM;
import com.createicbm.mods.ModProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Support for Sable (the physics engine behind Create Aeronautics ships). Sable is optional and only reached through
 * its public companion API by reflection, so nothing here can fail when it is missing or changes, every call falls
 * back to doing nothing.
 * <p>
 * Sable keeps each ship's blocks in a far away "plot" of the same level and draws them where the ship is. Ray traces
 * that hit a ship return the hit in plot space, so a missile used to jump to the plot and vanish, a launcher on a
 * ship aimed from the plot and a blast at the world position never touched the ship's blocks. The helpers here move
 * such positions back to where the ship is in the world and let blasts damage nearby ships.
 */
public class SableProxy extends ModProxy
{
    public static final String MOD_ID = "sable";

    public static final SableProxy INSTANCE = new SableProxy();

    /** Largest radius used for damaging ships, the vanilla rays used for it get slow past this */
    private static final float MAX_SHIP_BLAST_RADIUS = 24;

    private boolean resolved = false;
    private boolean enabled = false;

    private Object companion;
    private Method projectOutOfSubLevel;
    private Method isInPlotGrid;
    private Method getTrackingOrVehicleSubLevel;
    @Nullable private Method getVelocity;
    @Nullable private Method getAllIntersecting;
    @Nullable private Constructor<?> boundingBox;

    @Override
    public void init()
    {
        resolve();
    }

    private void resolve()
    {
        if (resolved)
        {
            return;
        }
        resolved = true;
        if (!isModLoaded(MOD_ID))
        {
            return;
        }
        try
        {
            final Class<?> companionClass = Class.forName("dev.ryanhcode.sable.companion.SableCompanion");
            companion = companionClass.getField("INSTANCE").get(null);
            projectOutOfSubLevel = companionClass.getMethod("projectOutOfSubLevel", Level.class, Position.class);
            isInPlotGrid = companionClass.getMethod("isInPlotGrid", Level.class, int.class, int.class);
            getTrackingOrVehicleSubLevel = companionClass.getMethod("getTrackingOrVehicleSubLevel", Entity.class);
            enabled = companion != null;
        }
        catch (Throwable e)
        {
            CreateICBM.logger().warn("Sable support: companion API not found, ships are not supported", e);
            return;
        }

        // Optional parts, missing ones only turn off what they are for
        try
        {
            final Class<?> subLevelAccess = Class.forName("dev.ryanhcode.sable.companion.SubLevelAccess");
            getVelocity = companion.getClass().getMethod("getVelocity", Level.class, subLevelAccess, Position.class);
        }
        catch (Throwable e)
        {
            CreateICBM.logger().warn("Sable support: ship velocity not found, missiles fired from ships won't inherit it", e);
        }
        try
        {
            final Class<?> boxInterface = Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3dc");
            final Class<?> boxClass = Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3d");
            getAllIntersecting = companion.getClass().getMethod("getAllIntersecting", Level.class, boxInterface);
            boundingBox = boxClass.getConstructor(double.class, double.class, double.class, double.class, double.class, double.class);
        }
        catch (Throwable e)
        {
            // Without it every destructive blast runs the ship pass, which is only slower
            getAllIntersecting = null;
            boundingBox = null;
        }
    }

    /**
     * @return true when Sable is installed and its API was found
     */
    public boolean isEnabled()
    {
        resolve();
        return enabled;
    }

    private void fail(String what, Throwable e)
    {
        CreateICBM.logger().error("Sable support: " + what + " failed, ship support is now off", e);
        enabled = false;
    }

    /**
     * @return true if the position is inside the plot grid where Sable stores ship blocks
     */
    public boolean isInPlot(Level level, double x, double z)
    {
        if (!isEnabled())
        {
            return false;
        }
        try
        {
            return (boolean) isInPlotGrid.invoke(companion, level, Mth.floor(x) >> 4, Mth.floor(z) >> 4);
        }
        catch (Throwable e)
        {
            fail("plot check", e);
            return false;
        }
    }

    /**
     * Moves a position in a ship's plot to where that spot of the ship is in the world
     *
     * @return world position, the same position if it isn't on a ship
     */
    public Vec3 toWorld(Level level, Vec3 pos)
    {
        if (!isInPlot(level, pos.x, pos.z))
        {
            return pos;
        }
        try
        {
            final Object result = projectOutOfSubLevel.invoke(companion, level, pos);
            return result instanceof Vec3 vec ? vec : pos;
        }
        catch (Throwable e)
        {
            fail("position conversion", e);
            return pos;
        }
    }

    /**
     * Block hits on a ship come back from ray traces in plot space. This moves the hit location to the world and keeps
     * the block position, so the block can still be looked up.
     */
    public BlockHitResult toWorld(Level level, BlockHitResult hit)
    {
        if (hit.getType() == HitResult.Type.MISS)
        {
            return hit;
        }
        final Vec3 location = toWorld(level, hit.getLocation());
        if (location == hit.getLocation())
        {
            return hit;
        }
        return new BlockHitResult(location, hit.getDirection(), hit.getBlockPos(), hit.isInside());
    }

    /**
     * @see #toWorld(Level, BlockHitResult)
     */
    public HitResult toWorld(Level level, HitResult hit)
    {
        return hit instanceof BlockHitResult blockHit ? toWorld(level, blockHit) : hit;
    }

    /**
     * Moves an entity created on a ship (a launcher on the deck spawns its missile in plot space) to the world, so its
     * flight path is worked out from where it really is
     */
    public void moveToWorld(Entity entity)
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

    /**
     * @return velocity (meters per tick) of the ship the entity stands on or rides, zero if none
     */
    public Vec3 getShipVelocity(Entity entity)
    {
        if (!isEnabled() || getVelocity == null)
        {
            return Vec3.ZERO;
        }
        try
        {
            final Object subLevel = getTrackingOrVehicleSubLevel.invoke(companion, entity);
            if (subLevel == null)
            {
                return Vec3.ZERO;
            }
            final Object velocity = getVelocity.invoke(companion, entity.level(), subLevel, entity.position());
            // Sable works in meters per second
            return velocity instanceof Vec3 vec ? vec.scale(1 / 20.0) : Vec3.ZERO;
        }
        catch (Throwable e)
        {
            CreateICBM.logger().error("Sable support: ship velocity failed, missiles fired from ships won't inherit it", e);
            getVelocity = null;
            return Vec3.ZERO;
        }
    }

    /**
     * Blasts edit the world's blocks, ships nearby live in their plots and would come out untouched. This runs a
     * vanilla explosion that only breaks ship blocks: Sable maps its rays onto ships, pushes them and claim mods are
     * asked as for any explosion. Entities are left alone, the blast itself already hit them.
     *
     * @param radius of the blast, capped at {@link #MAX_SHIP_BLAST_RADIUS}
     */
    public void damageShips(Level level, @Nullable Entity source, double x, double y, double z, float radius)
    {
        if (radius <= 0 || !(level instanceof ServerLevel) || !isEnabled() || isInPlot(level, x, z))
        {
            // A blast already in a plot edits the ship's blocks directly
            return;
        }
        final float size = Math.min(radius, MAX_SHIP_BLAST_RADIUS);
        try
        {
            if (!hasShipsNear(level, x, y, z, size * 2))
            {
                return;
            }
            final Explosion explosion = new Explosion(level, source, null, new ShipOnlyDamage(level), x, y, z, size, false,
                Explosion.BlockInteraction.DESTROY_WITH_DECAY, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.GENERIC_EXPLODE);
            explosion.explode();
            explosion.finalizeExplosion(false);
        }
        catch (Throwable e)
        {
            CreateICBM.logger().error("Sable support: damaging ships near a blast failed", e);
        }
    }

    private boolean hasShipsNear(Level level, double x, double y, double z, double range) throws Exception
    {
        if (getAllIntersecting == null || boundingBox == null)
        {
            return true;
        }
        final Object box = boundingBox.newInstance(x - range, y - range, z - range, x + range, y + range, z + range);
        final Object ships = getAllIntersecting.invoke(companion, level, box);
        return !(ships instanceof Iterable<?> iterable) || iterable.iterator().hasNext();
    }

    /**
     * Breaks blocks in ship plots only, and never hurts or pushes entities
     */
    private class ShipOnlyDamage extends ExplosionDamageCalculator
    {
        private final Level level;

        ShipOnlyDamage(Level level)
        {
            this.level = level;
        }

        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, float power)
        {
            return isInPlot(level, pos.getX() + 0.5, pos.getZ() + 0.5) && super.shouldBlockExplode(explosion, reader, pos, state, power);
        }

        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity)
        {
            return false;
        }

        @Override
        public float getKnockbackMultiplier(Entity entity)
        {
            return 0;
        }
    }
}
