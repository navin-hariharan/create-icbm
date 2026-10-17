package com.createicbm.api.reg.events;

import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import net.neoforged.bus.api.Event;

/**
 * Fired to allow registering missile flight logic builders
 */
public class MissileFlightLogicRegistryEvent extends Event
{
    public final IBuilderRegistry<IMissileFlightLogic> registry;

    public MissileFlightLogicRegistryEvent(IBuilderRegistry<IMissileFlightLogic> registry)
    {
        this.registry = registry;
    }
}
