package com.createicbm.api.reg.events;

import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import net.neoforged.bus.api.Event;

/**
 * Fired to allow registering action status message types
 */
public class ActionStatusRegistryEvent extends Event
{
    public final IBuilderRegistry<IActionStatus> registry;

    public ActionStatusRegistryEvent(IBuilderRegistry<IActionStatus> registry)
    {
        this.registry = registry;
    }
}
