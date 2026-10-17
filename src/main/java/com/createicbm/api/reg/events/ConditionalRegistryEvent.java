package com.createicbm.api.reg.events;

import com.createicbm.api.actions.conditions.ICondition;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import net.neoforged.bus.api.Event;

/**
 * Fired to allow registering conditional types
 */
public class ConditionalRegistryEvent extends Event
{
    public final IBuilderRegistry<ICondition> registry;

    public ConditionalRegistryEvent(IBuilderRegistry<ICondition> registry)
    {
        this.registry = registry;
    }
}
