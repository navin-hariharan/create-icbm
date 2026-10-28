package com.createicbm.content.missile.logic.flight;

import com.createicbm.ICBMConstants;
import com.createicbm.api.missiles.IMissile;
import com.createicbm.api.missiles.parts.IMissileTarget;
import com.createicbm.config.missile.ConfigMissile;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Created by Robin Seifert on 2/8/2022.
 */
public class DirectFlightLogic extends DeadFlightLogic
{
    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "direct");
    // Not saved: the entity saves its own motion, and this is only read once by start() when the missile launches
    private double motionX;
    private double motionY;
    private double motionZ;

    public DirectFlightLogic() {
        super();
    }

    public DirectFlightLogic(int fuelTicks)
    {
        super(fuelTicks);
    }

    @Override
    public void calculateFlightPath(Level world, double startX, double startY, double startZ, IMissileTarget targetData)
    {
        if(targetData != null) //TODO if we have no target data have missile fly around in random directions until it hits something
        {
            motionX = targetData.getX() - startX;
            motionY = targetData.getY() - startY;
            motionZ = targetData.getZ() - startZ;

            //Normalize
            final double velocity = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            if (velocity < 1.0E-6)
            {
                // Target on top of the launcher, dividing by zero made the motion NaN and the missile vanish
                motionX = 0;
                motionY = ConfigMissile.DIRECT_FLIGHT_SPEED;
                motionZ = 0;
                return;
            }
            motionX /= velocity;
            motionY /= velocity;
            motionZ /= velocity;

            //Add multiplier
            motionX *= ConfigMissile.DIRECT_FLIGHT_SPEED;
            motionY *= ConfigMissile.DIRECT_FLIGHT_SPEED;
            motionZ *= ConfigMissile.DIRECT_FLIGHT_SPEED;
        }
    }

    @Override
    public void start(Entity entity, IMissile missile)
    {
        //Set motion
        MotionHelper.setMotionX(entity, motionX);
        MotionHelper.setMotionY(entity, motionY);
        MotionHelper.setMotionZ(entity, motionZ);

        //Update rotation
        float f3 = (float) Math.sqrt(motionX * motionX + motionZ * motionZ);
        MotionHelper.setYawAndPrev(entity, (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI));
        MotionHelper.setPitchAndPrev(entity, (float) (Math.atan2(motionY, (double) f3) * 180.0D / Math.PI));
    }

    //TODO rework to use current pitch and yaw to set motion
    //TODO update motion as long as we have fuel (ticks of motion time)

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey()
    {
        return REG_NAME;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof DirectFlightLogic) {
            return
                super.equals(other) //super does some checks on it's own fields
                && Math.abs(((DirectFlightLogic) other).motionX - motionX) <= 0.0001 //floating error handling
                && Math.abs(((DirectFlightLogic) other).motionY - motionY) <= 0.0001
                && Math.abs(((DirectFlightLogic) other).motionZ - motionZ) <= 0.0001;
        }
        return false;
    }
}
