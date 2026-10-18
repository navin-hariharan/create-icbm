package com.createicbm.api.reg.content;

import com.createicbm.api.missiles.IMissile;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * @deprecated being removed in 1.13
 */
@Deprecated
public interface IExMissileRegistry extends IExplosiveContentRegistry
{

    /**
     * Adds a simple callback for when a missile launches
     * <p>
     * * Do not use this in place of normal events, this is designed to add logic for
     * * specific missile types.
     *
     * @param exName        - id of the explosive/missile
     * @param eventCallback - function to call
     */
    void setLaunchListener(ResourceLocation exName, Consumer<IMissile> eventCallback);

    /**
     * Adds a simple callback for when a missile updates.
     * <p>
     * Do not use this in place of normal events, this is designed to add logic for
     * specific missile types.
     *
     * @param exName        - id of the explosive/missile
     * @param eventCallback - function to call
     */
    void setMissileUpdateListener(ResourceLocation exName, Consumer<IMissile> eventCallback);

    //TODO add handling for insert into launcher (cruise launcher prevents cluster and homing)

    void triggerLaunch(IMissile missile);

    void triggerFlightUpdate(IMissile missile);
}
