package com.createicbm.api.reg.events;

import com.createicbm.api.missiles.parts.IMissileTarget;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import net.neoforged.bus.api.Event;

/**
 * Fired to allow registering missile target builders
 */
public class MissileTargetRegistryEvent extends Event
{
    public final IBuilderRegistry<IMissileTarget> registry;

    public MissileTargetRegistryEvent(IBuilderRegistry<IMissileTarget> registry)
    {
        this.registry = registry;
    }
}
