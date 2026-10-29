package com.createicbm.mods.servercore;

import com.createicbm.CreateICBM;
import com.createicbm.ICBMConstants;
import com.createicbm.content.reg.EntityReg;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
import com.createicbm.mods.ModProxy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * ServerCore's activation range (off by default) can stop entities far from players. All registered Create ICBM
 * entity types are added to its exclusions when the server starts. ServerCore stays optional through reflection.
 */
public class ServerCoreProxy extends ModProxy
{
    public static final String MOD_ID = "servercore";

    public static final ServerCoreProxy INSTANCE = new ServerCoreProxy();

    /** ServerCore listens for this entity tag and updates its cached activation-range exclusion immediately. */
    private static final String SERVERCORE_EXCLUDE_TAG = "exclude_ear";

    /** All Create ICBM entities, so activation-range changes cannot stall gameplay or effects. */
    private static final List<Supplier<? extends EntityType<?>>> ALWAYS_TICK = EntityReg.ALL_ENTITY_TYPES;

    @Override
    public void init()
    {
        if (isModLoaded(MOD_ID))
        {
            NeoForge.EVENT_BUS.addListener(ServerAboutToStartEvent.class, event -> exclude());
            // ServerCore caches its exclusion flag inside Entity during construction. Updating only the config set
            // is not reliable if its cached value was already initialized; add its documented opt-out tag on join.
            NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, this::excludeJoinedEntity);
        }
    }

    private void excludeJoinedEntity(EntityJoinLevelEvent event)
    {
        if (event.getLevel().isClientSide())
        {
            return;
        }

        final Entity entity = event.getEntity();
        final ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (key == null || !ICBMConstants.DOMAIN.equals(key.getNamespace()))
        {
            return;
        }

        // The ServerCore Entity mixin injects into addTag and flips its cached `servercore$excluded` flag.
        // This remains effective even when the activation type and excluded-type set were cached earlier.
        final boolean tagAdded = entity.addTag(SERVERCORE_EXCLUDE_TAG);
        entity.noCulling = true;
        if (ICBMDiagnostics.enabled())
        {
            ICBMDiagnostics.info(
                "SERVERCORE_ENTITY_EXEMPTED id={} type={} dim={} pos={} tag={} tagAdded={} tagPresent={} serverCoreExcluded={} noCulling={}",
                entity.getId(), entity.getClass().getSimpleName(), entity.level().dimension().location(), entity.position(),
                SERVERCORE_EXCLUDE_TAG, tagAdded, entity.getTags().contains(SERVERCORE_EXCLUDE_TAG),
                readCachedServerCoreExclusion(entity), entity.noCulling);
        }
    }

    /** Read ServerCore's cached flag reflectively so Create: ICBM remains loadable without ServerCore. */
    private String readCachedServerCoreExclusion(Entity entity)
    {
        try
        {
            final Method getter = entity.getClass().getMethod("servercore$isExcluded");
            return String.valueOf(getter.invoke(entity));
        }
        catch (Throwable ignored)
        {
            return "unavailable";
        }
    }

    @SuppressWarnings("unchecked")
    private void exclude()
    {
        try
        {
            final Class<?> config = Class.forName("me.wesley1808.servercore.common.config.Config");
            final Object main = config.getMethod("getSafe").invoke(null);
            if (main == null)
            {
                return;
            }
            final Method activationRange = Class.forName("me.wesley1808.servercore.common.config.MainConfig").getMethod("activationRange");
            final Object range = activationRange.invoke(main);
            final Method excluded = Class.forName("me.wesley1808.servercore.common.config.data.activation_range.ActivationRangeConfig").getMethod("excludedEntityTypes");
            final Set<EntityType<?>> types = (Set<EntityType<?>>) excluded.invoke(range);
            int added = 0;
            for (Supplier<? extends EntityType<?>> type : ALWAYS_TICK)
            {
                if (types.add(type.get()))
                {
                    added++;
                }
            }
            CreateICBM.logger().info(
                "ServerCore detected; added {} Create ICBM entity types to activation-range exclusions ({} total exclusions now).",
                added, types.size());
        }
        catch (Throwable e)
        {
            CreateICBM.logger().warn("ServerCore support: could not exclude missiles from its activation range. If it is enabled, add create_icbm entities to 'excluded-entity-types' in ServerCore's config", e);
        }
    }
}
