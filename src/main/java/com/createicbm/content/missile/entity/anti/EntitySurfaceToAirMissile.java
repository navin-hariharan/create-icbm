package com.createicbm.content.missile.entity.anti;

import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.blast.ICBMDamageTypes;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.missile.logic.flight.FollowTargetLogic;
import com.createicbm.content.reg.EntityReg;
import com.createicbm.content.reg.ItemReg;
import com.createicbm.prefab.entity.EntityICBM;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.EntityCapability;

import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 11/30/2021.
 */
public class EntitySurfaceToAirMissile extends EntityMissile<EntitySurfaceToAirMissile> {

    private ItemStack renderStackCache;
    public final SAMTargetData scanLogic = new SAMTargetData(this);

    private boolean hasStartedFollowing = false;

    /** Ticks without a target after it lost the one it followed, enough for two scans for another missile */
    private static final int LOST_TARGET_TICKS = 25;
    private int lostTargetTicks = 0;

    public EntitySurfaceToAirMissile(EntityType<? extends EntitySurfaceToAirMissile> type, Level world) {
        super(type, world);
        this.getMissileCapability().setTargetData(scanLogic); //TODO create custom missileCap to force getTarget()
        this.initHealth(ConfigMissile.TIER_2_HEALTH);
    }

    public EntitySurfaceToAirMissile(Level world) {
        this(EntityReg.MISSILE_SAM.get(), world);
    }

    @Override
    public void tick() {

        if(!level().isClientSide()) {

            //Scan for targets
            scanLogic.tick();

            final Entity currentTarget = scanLogic.getTarget();

            final IMissileFlightLogic flightLogic = this.getMissileCapability().getFlightLogic();
            if (!hasStartedFollowing && currentTarget != null && (flightLogic == null || flightLogic.canSafelyExitLogic())) {
                hasStartedFollowing = true;
                //TODO play missile lock sound effect

                // Update our targeting system to track sam targets, some flight systems will use their own targeting logic
                this.getMissileCapability().setTargetData(scanLogic);

                // Update out flight logic to follow our sam target
                this.getMissileCapability().switchFlightLogic(new FollowTargetLogic(ConfigMissile.SAM_MISSILE.FUEL));
            }

            // The missile it followed is gone (shot down by another SAM, landed or left the radar): look for another
            //  one close by, and blow up in the air if there is none instead of flying off into the sky
            if (hasStartedFollowing && currentTarget == null) {
                if (++lostTargetTicks > LOST_TARGET_TICKS) {
                    selfDestruct();
                    return;
                }
            }
            else {
                lostTargetTicks = 0;
            }

            //TODO move to object that gets a tick() invoke `ProximityKillHandler`
            //Handle kill target logic
            if (currentTarget != null) {
                final double distance = this.distanceTo(currentTarget);

                if (distance <= ConfigMissile.SAM_MISSILE.FLIGHT_SPEED) {
                    currentTarget.hurt(ICBMDamageTypes.source(level(), ICBMDamageTypes.MISSILE, this, this), ConfigMissile.SAM_MISSILE.ATTACK_DAMAGE);
                    //TODO play sound effect of missile exploding
                    this.discard();
                }
            }
        }

        //Normal update logic
        super.tick();
    }

    /** Harmless burst in the air, no blast and no block damage */
    private void selfDestruct() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 2f, 1.4f);
        }
        this.discard();
    }

    /**
     * @return true when SAMs already following the missile carry enough damage to bring it down, so another turret
     * keeps its SAM instead of firing a second one that would lose its target and be wasted
     */
    public static boolean isTargetCovered(Entity target) {
        if (target == null || !(target.level() instanceof ServerLevel level)) {
            return false;
        }
        final int chasing = level.getEntities(EntityTypeTest.forClass(EntitySurfaceToAirMissile.class),
            sam -> sam.isAlive() && sam.scanLogic.getLockedTarget() == target).size();
        if (chasing == 0) {
            return false;
        }
        final float health = target instanceof EntityICBM icbm ? icbm.getHealth() : 1;
        return ConfigMissile.SAM_MISSILE.ATTACK_DAMAGE <= 0 || chasing * ConfigMissile.SAM_MISSILE.ATTACK_DAMAGE >= health;
    }

    @Nullable
    @Override
    public <T> T getEntityCapability(EntityCapability<T, Void> capability) {
        //TODO add AB capability so radars can redirect targets
        return super.getEntityCapability(capability);
    }

    @Override
    public ItemStack toStack() {
        if(level().isClientSide()) {
            if(renderStackCache == null) {
                renderStackCache = new ItemStack(ItemReg.itemSAM.get());
            }
            return renderStackCache;
        }
        return new ItemStack(ItemReg.itemSAM.get());
    }
}
