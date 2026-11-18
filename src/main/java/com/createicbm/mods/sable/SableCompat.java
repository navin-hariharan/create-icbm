package com.createicbm.mods.sable;

import com.createicbm.CreateICBM;
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
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Sable support, only created when Sable is installed (see {@link SableProxy#INSTANCE}). Sable is reached through its
 * public companion API by reflection, so nothing here can fail when Sable changes: a call that fails turns its part
 * off and falls back to what {@link SableProxy} does without Sable.
 */
final class SableCompat extends SableProxy
{
    /** Largest radius used for damaging ships, the vanilla rays used for it get slow past this */
    private static final float MAX_SHIP_BLAST_RADIUS = 24;

    private boolean enabled;

    private Object companion;
    private Method projectOutOfSubLevel;
    private Method isInPlotGrid;
    private Method getTrackingOrVehicleSubLevel;
    @Nullable private Method getVelocity;
    @Nullable private Method getAllIntersecting;
    @Nullable private Constructor<?> boundingBox;

    // Ship frames: which ship holds a plot position and where that ship is (position and rotation)
    @Nullable private Method getContaining;
    @Nullable private Method logicalPose;
    @Nullable private Method transformPosition;
    @Nullable private Method transformPositionInverse;
    @Nullable private Method transformNormal;

    SableCompat()
    {
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
        try
        {
            final Class<?> subLevelAccess = Class.forName("dev.ryanhcode.sable.companion.SubLevelAccess");
            final Class<?> pose = Class.forName("dev.ryanhcode.sable.companion.math.Pose3dc");
            getContaining = Class.forName("dev.ryanhcode.sable.companion.SableCompanion").getMethod("getContaining", Level.class, int.class, int.class);
            logicalPose = subLevelAccess.getMethod("logicalPose");
            transformPosition = pose.getMethod("transformPosition", Vec3.class);
            transformPositionInverse = pose.getMethod("transformPositionInverse", Vec3.class);
            transformNormal = pose.getMethod("transformNormal", Vec3.class);
        }
        catch (Throwable e)
        {
            getContaining = null;
            CreateICBM.logger().warn("Sable support: ship rotation not found, machines on ships aim and launch as if the ship was level", e);
        }
        if (enabled)
        {
            CreateICBM.logger().info("Sable found, machines work on Create Aeronautics ships");
        }
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    private void fail(String what, Throwable e)
    {
        CreateICBM.logger().error("Sable support: " + what + " failed, ship support is now off", e);
        enabled = false;
    }

    @Override
    public boolean isInPlot(Level level, double x, double z)
    {
        if (!enabled)
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

    @Override
    public Vec3 toWorld(Level level, Vec3 pos)
    {
        if (!isInPlot(level, pos.x, pos.z))
        {
            return pos;
        }
        try
        {
            return projectOutOfSubLevel.invoke(companion, level, pos) instanceof Vec3 vec ? vec : pos;
        }
        catch (Throwable e)
        {
            fail("position conversion", e);
            return pos;
        }
    }

    @Override
    public Vec3 getShipVelocity(Entity entity)
    {
        if (!enabled || getVelocity == null)
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
            // Sable wants the spot on the ship in plot space, the entity stands in the world. Passing the world
            // position gave the wrong lever for a turning ship, so the spin part of the speed was wrong.
            Vec3 plotPos = entity.position();
            if (getContaining != null)
            {
                final Object pose = logicalPose.invoke(subLevel);
                if (pose != null && transformPositionInverse.invoke(pose, plotPos) instanceof Vec3 local)
                {
                    plotPos = local;
                }
            }
            return velocityOf(entity.level(), subLevel, plotPos);
        }
        catch (Throwable e)
        {
            CreateICBM.logger().error("Sable support: ship velocity failed, missiles fired from ships won't inherit it", e);
            getVelocity = null;
            return Vec3.ZERO;
        }
    }

    /** @return velocity in meters per tick of a ship spot given in plot space */
    private Vec3 velocityOf(Level level, Object subLevel, Vec3 plotPos) throws Exception
    {
        if (getVelocity == null)
        {
            return Vec3.ZERO;
        }
        // Sable works in meters per second
        if (getVelocity.invoke(companion, level, subLevel, plotPos) instanceof Vec3 vec && isFinite(vec))
        {
            return vec.scale(1 / 20.0);
        }
        return Vec3.ZERO;
    }

    @Nullable
    @Override
    public ShipFrame getShipFrame(Level level, Vec3 plotPos)
    {
        if (level == null || getContaining == null || !isInPlot(level, plotPos.x, plotPos.z))
        {
            return null;
        }
        try
        {
            final Object subLevel = getContaining.invoke(companion, level, Mth.floor(plotPos.x) >> 4, Mth.floor(plotPos.z) >> 4);
            final Object pose = subLevel == null ? null : logicalPose.invoke(subLevel);
            return pose == null ? null : new Frame(level, subLevel, pose);
        }
        catch (Throwable e)
        {
            CreateICBM.logger().error("Sable support: finding the ship of a block failed, machines on ships work as if the ship was level", e);
            getContaining = null;
            return null;
        }
    }

    private static boolean isFinite(Vec3 vec)
    {
        return Double.isFinite(vec.x) && Double.isFinite(vec.y) && Double.isFinite(vec.z);
    }

    /** Every method falls back to returning its input unchanged if Sable fails */
    private final class Frame implements ShipFrame
    {
        private final Level level;
        private final Object subLevel;
        private final Object pose;

        private Frame(Level level, Object subLevel, Object pose)
        {
            this.level = level;
            this.subLevel = subLevel;
            this.pose = pose;
        }

        @Override
        public Vec3 toWorld(Vec3 plotPos)
        {
            return apply(transformPosition, plotPos);
        }

        @Override
        public Vec3 toShip(Vec3 worldPos)
        {
            return apply(transformPositionInverse, worldPos);
        }

        @Override
        public Vec3 directionToWorld(Vec3 direction)
        {
            final Vec3 turned = apply(transformNormal, direction);
            final double length = turned.length();
            return length < 1.0E-9 ? direction : turned.scale(direction.length() / length);
        }

        @Override
        public Vec3 velocityAt(Vec3 plotPos)
        {
            try
            {
                return velocityOf(level, subLevel, plotPos);
            }
            catch (Throwable e)
            {
                CreateICBM.logger().error("Sable support: ship velocity failed, missiles fired from ships won't inherit it", e);
                getVelocity = null;
                return Vec3.ZERO;
            }
        }

        private Vec3 apply(@Nullable Method transform, Vec3 input)
        {
            if (transform == null)
            {
                return input;
            }
            try
            {
                return transform.invoke(pose, input) instanceof Vec3 out && isFinite(out) ? out : input;
            }
            catch (Throwable e)
            {
                CreateICBM.logger().error("Sable support: ship transform failed, machines on ships work as if the ship was level", e);
                getContaining = null;
                return input;
            }
        }
    }

    /**
     * Runs a vanilla explosion that only breaks ship blocks: Sable maps its rays onto ships, pushes them and claim mods
     * are asked as for any explosion. Entities are left alone, the blast itself already hit them.
     */
    @Override
    public void damageShips(Level level, @Nullable Entity source, double x, double y, double z, float radius)
    {
        if (radius <= 0 || !(level instanceof ServerLevel) || !enabled || isInPlot(level, x, z))
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
        return !(getAllIntersecting.invoke(companion, level, box) instanceof Iterable<?> ships) || ships.iterator().hasNext();
    }

    /** Breaks blocks in ship plots only, and never hurts or pushes entities */
    private final class ShipOnlyDamage extends ExplosionDamageCalculator
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
