package com.createicbm.api.reg.events;

import com.createicbm.api.actions.IActionData;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import net.neoforged.bus.api.Event;

/**
 * Fired to allow registering action data message types
 */
public class ActionRegistryEvent extends Event
{
    public final IBuilderRegistry<IActionData> registry;

    public ActionRegistryEvent(IBuilderRegistry<IActionData> registry)
    {
        this.registry = registry;
    }
}
