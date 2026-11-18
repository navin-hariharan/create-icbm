package com.createicbm.mods;

import com.createicbm.mods.bluemap.BlueMapProxy;
import com.createicbm.mods.doespotatotick.DoesPotatoTickProxy;
import com.createicbm.mods.immersiveoptimization.ImmersiveOptimizationProxy;
import com.createicbm.mods.mekanism.MekProxy;
import com.createicbm.mods.sable.SableProxy;
import com.createicbm.mods.servercore.ServerCoreProxy;

/**
 * Starts the support for each optional mod that is installed. The support of a mod that is missing is never loaded:
 * its class is only reached inside the lambda, which runs only when the mod is there. Client only support (Entity
 * Culling) is started by the client setup, JEI finds its plugin itself, and the Storage Drawers fix is a mixin that
 * is only applied when Storage Drawers and Sable are installed.
 */
public final class ModInteraction
{
    private ModInteraction()
    {
    }

    /** Called during common setup */
    public static void init()
    {
        start(MekProxy.MOD_ID, () -> MekProxy.INSTANCE.init());
        start(BlueMapProxy.MOD_ID, () -> BlueMapProxy.INSTANCE.init());
        start(ServerCoreProxy.MOD_ID, () -> ServerCoreProxy.INSTANCE.init());
        start(DoesPotatoTickProxy.MOD_ID, () -> DoesPotatoTickProxy.INSTANCE.init());
        start(ImmersiveOptimizationProxy.MOD_ID, () -> ImmersiveOptimizationProxy.INSTANCE.init());
        // Looks up Sable's API now, so a Sable that changed is reported at startup
        start(SableProxy.MOD_ID, () -> SableProxy.INSTANCE.isEnabled());
    }

    private static void start(String modId, Runnable init)
    {
        if (ModProxy.isModLoaded(modId))
        {
            init.run();
        }
    }
}
