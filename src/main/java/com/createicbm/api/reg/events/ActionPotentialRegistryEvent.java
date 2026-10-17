package com.createicbm.api.reg.events;

import com.createicbm.api.actions.IPotentialAction;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import net.neoforged.bus.api.Event;

/**
 * Fired to allow registering potential action builders
 */
public class ActionPotentialRegistryEvent extends Event
{
    public final IBuilderRegistry<IPotentialAction> registry;

    public ActionPotentialRegistryEvent(IBuilderRegistry<IPotentialAction> registry)
    {
        this.registry = registry;
    }
}
