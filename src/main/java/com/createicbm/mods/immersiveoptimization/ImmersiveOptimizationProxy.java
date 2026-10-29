package com.createicbm.mods.immersiveoptimization;

import com.createicbm.CreateICBM;
import com.createicbm.mods.ModProxy;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Immersive Optimization supports namespace rules in its JSON config. Add the Create: ICBM namespace to that
 * blacklist when the optimizer is present, without a hard dependency on its classes or version.
 */
public final class ImmersiveOptimizationProxy extends ModProxy
{
    public static final String MOD_ID = "immersive_optimization";
    public static final ImmersiveOptimizationProxy INSTANCE = new ImmersiveOptimizationProxy();
    private static final String ICBM_NAMESPACE = "create_icbm";

    private ImmersiveOptimizationProxy()
    {
    }

    @Override
    public void init()
    {
        if (isModLoaded(MOD_ID))
        {
            NeoForge.EVENT_BUS.addListener(ServerAboutToStartEvent.class, event -> exclude());
        }
    }

    @SuppressWarnings("unchecked")
    private void exclude()
    {
        final Class<?> configClass;
        final Object config;
        try
        {
            configClass = Class.forName("net.conczin.immersive_optimization.config.Config");
            config = configClass.getMethod("getInstance").invoke(null);
            if (config == null)
            {
                return;
            }
        }
        catch (Throwable e)
        {
            CreateICBM.logger().warn("Immersive Optimization detected, but its configuration API could not be accessed.", e);
            return;
        }

        // Entity tick scheduling has a namespace blacklist. Add ICBM only if it is not already excluded.
        try
        {
            final Map<String, Boolean> entityRules = (Map<String, Boolean>) configClass.getField("entities").get(config);
            if (!Boolean.FALSE.equals(entityRules.get(ICBM_NAMESPACE)))
            {
                final Method toggleRule = configClass.getMethod("toggleEntityRule", String.class, boolean.class);
                final boolean changed = (boolean) toggleRule.invoke(config, ICBM_NAMESPACE, false);
                if (changed)
                {
                    configClass.getMethod("save").invoke(config);
                    CreateICBM.logger().info(
                        "Immersive Optimization detected; blacklisted the '{}' namespace so its entity scheduler will not throttle Create ICBM",
                        ICBM_NAMESPACE);
                }
            }
        }
        catch (Throwable e)
        {
            CreateICBM.logger().warn(
                "Immersive Optimization detected, but automatic Create ICBM entity exclusion failed. Add 'create_icbm': false to the 'entities' map in config/immersive_optimization.json",
                e);
        }

        // Immersive Optimization has no per-block-entity whitelist. If its optional block-entity scheduler is enabled,
        // it can throttle launch pads, cruise launchers, radar stations and explosive controllers. Turn that optional
        // scheduler off rather than allowing critical ICBM machines to run late; the upstream default is already false.
        try
        {
            final java.lang.reflect.Field blockEntityOptimization = configClass.getField("enableBlockEntities");
            if (blockEntityOptimization.getBoolean(config))
            {
                blockEntityOptimization.setBoolean(config, false);
                configClass.getMethod("save").invoke(config);
                CreateICBM.logger().warn(
                    "Immersive Optimization block-entity optimization was enabled, but it has no per-type exclusion API. Disabled that optional scheduler so Create ICBM launchers and radar stations retain normal server ticks; other Immersive Optimization entity settings remain enabled.");
            }
        }
        catch (NoSuchFieldException ignored)
        {
            // Older versions may not expose block-entity optimization; entity compatibility above remains applied.
        }
        catch (Throwable e)
        {
            CreateICBM.logger().warn(
                "Immersive Optimization entity compatibility was applied, but Create ICBM could not verify or disable its optional block-entity scheduler. If 'enableBlockEntities' is true in config/immersive_optimization.json, set it to false to preserve launchpad and radar timing.",
                e);
        }
    }
}
