package com.createicbm.mods;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;

import java.util.function.Supplier;

/**
 * Base of the support for an optional mod. {@link ModInteraction} only creates and starts one when its mod is
 * installed, so the support code for a missing mod is never loaded.
 */
public abstract class ModProxy
{
    /** Sets up the support, called once in common setup and only when the mod is installed */
    public abstract void init();

    /**
     * @return true when the mod is installed. Works from the start of loading, before the mod list is complete.
     */
    public static boolean isModLoaded(String modId)
    {
        final ModList mods = ModList.get();
        if (mods != null)
        {
            return mods.isLoaded(modId);
        }
        final LoadingModList loading = LoadingModList.get();
        return loading != null && loading.getModFileById(modId) != null;
    }

    /**
     * Picks the support for an optional mod. Only the chosen supplier runs, so a class only reached through the other
     * one is never loaded.
     */
    public static <T> T select(String modId, Supplier<? extends T> whenLoaded, Supplier<? extends T> otherwise)
    {
        return isModLoaded(modId) ? whenLoaded.get() : otherwise.get();
    }
}
