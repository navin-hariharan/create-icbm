package com.createicbm.api.events;

import com.createicbm.api.actions.IAction;
import com.createicbm.api.explosion.IBlast;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/3/19.
 *
 * @deprecated will be replaced with an event providing {@link com.createicbm.api.actions.IAction}
 */
@Deprecated
public abstract class BlastEvent<B extends IBlast> extends Event
{
    /** Source of the event */
    public final IAction blast;

    public BlastEvent(IAction blast)
    {
        this.blast = blast;
    }

    /**
     * Source of the blast.
     */
    public Level world()
    {
        return blast.getWorld();
    }

    /**
     * Source of the blast.
     */
    public double x()
    {
        return blast.getPosition().x;
    }

    /**
     * Source of the blast.
     */
    public double y()
    {
        return blast.getPosition().y;
    }

    /**
     * Source of the blast.
     */
    public double z()
    {
        return blast.getPosition().z;
    }

}
