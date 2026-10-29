package com.createicbm.mods.doespotatotick;

import com.createicbm.CreateICBM;
import com.createicbm.ICBMConstants;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
import com.createicbm.mods.ModProxy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.lang.reflect.Method;

/**
 * Does Potato Tick has its own per-entity tick policy. Setting Entity.noCulling or changing a config list does not
 * update its cached dpt$alwaysTick flag, so use its Tickable interface reflectively and exempt every entity in the
 * Create: ICBM namespace. The dependency stays optional and no optimizer implementation type appears in a signature.
 */
public final class DoesPotatoTickProxy extends ModProxy
{
    public static final String MOD_ID = "doespotatotick";
    private static final String TICKABLE_INTERFACE = "me.kall.doespotatotick.ext.Tickable";

    public static final DoesPotatoTickProxy INSTANCE = new DoesPotatoTickProxy();

    private TickableAccess access;
    private boolean failureLogged;

    private DoesPotatoTickProxy()
    {
    }

    @Override
    public void init()
    {
        if (!isModLoaded(MOD_ID))
        {
            return;
        }

        try
        {
            access = TickableAccess.resolve();
            NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, this::onEntityJoinLevel);
            CreateICBM.logger().info("Does Potato Tick detected; applying always-tick exemptions to all Create: ICBM entities.");
        }
        catch (Throwable error)
        {
            logFailure(error);
        }
    }

    private void onEntityJoinLevel(EntityJoinLevelEvent event)
    {
        final Entity entity = event.getEntity();
        if (isICBMEntity(entity))
        {
            exempt(entity, event.getLevel().isClientSide() ? "client_entity_join" : "server_entity_join");
        }
    }

    private static boolean isICBMEntity(Entity entity)
    {
        final ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null && ICBMConstants.DOMAIN.equals(key.getNamespace());
    }

    /** Apply and verify the optimizer's real per-entity escape hatch before the entity gets its first normal tick. */
    private void exempt(Entity entity, String source)
    {
        try
        {
            final boolean wasAlwaysTick = (boolean) access.getAlwaysTick.invoke(entity);
            final boolean wasTickable = (boolean) access.getTickable.invoke(entity);
            access.setAlwaysTick.invoke(entity, true);
            access.setTickable.invoke(entity, true);

            final boolean alwaysTick = (boolean) access.getAlwaysTick.invoke(entity);
            final boolean tickable = (boolean) access.getTickable.invoke(entity);
            final boolean verified = alwaysTick && tickable;
            ICBMDiagnostics.info(
                "DOESPOTATOTICK_ENTITY_EXEMPTED source={} id={} type={} dim={} pos={} wasAlwaysTick={} wasTickable={} alwaysTick={} tickable={} verified={} side={}",
                source, entity.getId(), entity.getType(), entity.level().dimension().location(), entity.position(),
                wasAlwaysTick, wasTickable, alwaysTick, tickable, verified,
                entity.level().isClientSide() ? "client" : "server");

            if (!verified)
            {
                CreateICBM.logger().warn("Does Potato Tick could not verify its always-tick exemption for Create: ICBM entity {} ({})", entity.getId(), entity.getType());
            }
        }
        catch (Throwable error)
        {
            logFailure(error);
        }
    }

    private void logFailure(Throwable error)
    {
        if (failureLogged)
        {
            return;
        }
        failureLogged = true;
        CreateICBM.logger().warn(
            "Does Potato Tick is installed, but Create: ICBM could not apply its per-entity tick exemption. Add 'create_icbm' to 'EntitiesOfTheModsThatWillAlwaysTick' in doespotatotick-common.toml.",
            error);
        ICBMDiagnostics.error("DOESPOTATOTICK_COMPATIBILITY_INIT_OR_APPLY_FAILED", error);
    }

    private static final class TickableAccess
    {
        private final Method getAlwaysTick;
        private final Method getTickable;
        private final Method setAlwaysTick;
        private final Method setTickable;

        private TickableAccess(Method getAlwaysTick, Method getTickable, Method setAlwaysTick, Method setTickable)
        {
            this.getAlwaysTick = getAlwaysTick;
            this.getTickable = getTickable;
            this.setAlwaysTick = setAlwaysTick;
            this.setTickable = setTickable;
        }

        private static TickableAccess resolve() throws ReflectiveOperationException
        {
            final Class<?> tickable = Class.forName(TICKABLE_INTERFACE);
            return new TickableAccess(
                tickable.getMethod("dpt$alwaysTick"),
                tickable.getMethod("dpt$tickable"),
                tickable.getMethod("dpt$setAlwaysTick", boolean.class),
                tickable.getMethod("dpt$setTickable", boolean.class));
        }
    }
}
