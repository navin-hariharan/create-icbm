package com.createicbm.api.reg.events;

import com.createicbm.api.actions.cause.IActionCause;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import net.neoforged.bus.api.Event;

/**
 * Fired to allow registering missile source builders
 */
public class ActionCauseRegistryEvent extends Event
{
    public final IBuilderRegistry<IActionCause> registry;

    public ActionCauseRegistryEvent(IBuilderRegistry<IActionCause> registry)
    {
        this.registry = registry;
    }
}
