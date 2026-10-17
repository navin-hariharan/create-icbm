package com.createicbm.content.missile.entity.anti;

import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.blast.ICBMDamageTypes;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.missile.logic.flight.FollowTargetLogic;
import com.createicbm.content.reg.EntityReg;
import com.createicbm.content.reg.ItemReg;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
