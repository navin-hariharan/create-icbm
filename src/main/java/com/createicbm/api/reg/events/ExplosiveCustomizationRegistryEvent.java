package com.createicbm.api.reg.events;

import com.createicbm.api.reg.IExplosiveCustomization;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import net.neoforged.bus.api.Event;

/**
 * Fired to allow registering customization types
 */
public class ExplosiveCustomizationRegistryEvent extends Event
{
    public final IBuilderRegistry<IExplosiveCustomization> registry;

    public ExplosiveCustomizationRegistryEvent(IBuilderRegistry<IExplosiveCustomization> registry)
    {
        this.registry = registry;
    }
}
