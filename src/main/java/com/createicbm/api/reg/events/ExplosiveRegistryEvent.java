package com.createicbm.api.reg.events;

import com.createicbm.CreateICBM;
import com.createicbm.api.EnumTier;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.explosion.IBlastFactory;
import com.createicbm.api.reg.IExplosiveRegistry;
import com.createicbm.api.reg.content.IExplosiveContentRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

/**
 * Fired to allow registering new explosive types
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 1/4/19.
 * @deprecated being removed in 1.13
 */
@Deprecated
public class ExplosiveRegistryEvent extends Event
{

    public final IExplosiveRegistry registry;

    private ResourceLocation lastRegistered;//TODO remove  after moving enable to a separate event

    public ExplosiveRegistryEvent(IExplosiveRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * Called to register a new explosive
     *
     * @param id           - unique id
     * @param tier         - tier of the explosive, optional but should default to something
     * @param blastFactory - handler to build the blast from the explosive type
     * @return this event so we can chain calls //TODO remove return after moving enable to a separate event
     */
    public ExplosiveRegistryEvent register(ResourceLocation id, EnumTier tier, IBlastFactory blastFactory)
    {
        CreateICBMAPI.EXPLOSIVE_REGISTRY.register(id, tier, blastFactory);
        lastRegistered = id;
        return this;
    }

    //TODO move to a separate event
    public ExplosiveRegistryEvent enableContent(ResourceLocation contentID)
    {
        return enableContent(lastRegistered, contentID);
    }

    //TODO move to a separate event
    public ExplosiveRegistryEvent enableContent(ResourceLocation id, ResourceLocation contentID)
    {
        IExplosiveContentRegistry registry = CreateICBMAPI.EXPLOSIVE_REGISTRY.getContentRegistry(contentID);
        if (registry != null)
        {
            registry.enableContent(id);
        }
        else
        {
            CreateICBM.logger().error("ExplosiveRegistryEvent: No content registry found for " + contentID + " while enabling content for " + id);
        }
        return this;
    }

    //TODO remove after moving enable to a separate event
    public void done()
    {
        lastRegistered = null;
    }
}
