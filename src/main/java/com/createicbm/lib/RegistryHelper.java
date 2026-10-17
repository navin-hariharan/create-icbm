package com.createicbm.lib;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class RegistryHelper
{
    private RegistryHelper()
    {
    }

    /**
     * @return registry access of the client's connection when called from the client thread, otherwise the
     * running server's (integrated or dedicated). Falls back to builtin registries.
     */
    public static HolderLookup.Provider access()
    {
        // Client thread code (entity spawn data, block entity update tags, tooltips) must use the client's own
        //  registries even in singleplayer, the integrated server's data driven holders are different objects
        if (FMLEnvironment.dist.isClient())
        {
            final RegistryAccess clientAccess = ClientRegistryHelper.accessIfClientThread();
            if (clientAccess != null)
            {
                return clientAccess;
            }
        }
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null)
        {
            return server.registryAccess();
        }
        if (FMLEnvironment.dist.isClient())
        {
            final RegistryAccess clientAccess = ClientRegistryHelper.access();
            if (clientAccess != null)
            {
                return clientAccess;
            }
        }
        return RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
    }
}
