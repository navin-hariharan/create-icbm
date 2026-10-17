package com.createicbm.api.reg.events;

import com.createicbm.api.reg.IExplosiveRegistry;
import com.createicbm.api.reg.content.IExplosiveContentRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

/**
 * Used to register new content types before explosives are registered.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 1/4/19.
 * @deprecated being removed in 1.13
 */
@Deprecated
public class ExplosiveContentRegistryEvent extends Event
{

    public final IExplosiveRegistry registry;

    public ExplosiveContentRegistryEvent(IExplosiveRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * Called to register a new content type
     *
     * @param id              - unique name of the content type
     * @param contentRegistry - content type
     */
    public void register(ResourceLocation id, IExplosiveContentRegistry contentRegistry)
    {
        registry.registerContentRegistry(id, contentRegistry);
    }
}
