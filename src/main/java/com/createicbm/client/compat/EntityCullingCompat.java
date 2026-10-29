package com.createicbm.client.compat;

import com.createicbm.CreateICBM;
import com.createicbm.content.reg.BlockReg;
import com.createicbm.content.reg.EntityReg;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Client-side compatibility for Entity Culling. Its whitelists are per registry entry rather than per namespace, so
 * add every registered Create: ICBM entity and block-entity ID. Reflection keeps the mod optional.
 */
public final class EntityCullingCompat
{
    private static final String MOD_ID = "entityculling";
    private static boolean complete;
    private static boolean failureLogged;

    private EntityCullingCompat()
    {
    }

    @SuppressWarnings("unchecked")
    public static void onClientTick(ClientTickEvent.Post event)
    {
        if (complete)
        {
            return;
        }
        if (!ModList.get().isLoaded(MOD_ID))
        {
            complete = true; // The loaded mod list is stable for this client session.
            return;
        }

        try
        {
            final Class<?> modClass = Class.forName("dev.tr7zw.entityculling.EntityCullingModBase");
            final Object mod = modClass.getField("instance").get(null);
            if (mod == null)
            {
                return; // Entity Culling may still be completing client initialization; retry next tick.
            }

            final Object config = modClass.getField("config").get(mod);
            if (config == null)
            {
                return;
            }

            final Class<?> configClass = config.getClass();
            final Set<String> renderConfig = (Set<String>) configClass.getField("entityWhitelist").get(config);
            final Set<String> tickConfig = (Set<String>) configClass.getField("tickCullingWhitelist").get(config);
            final Set<Object> renderRuntime = (Set<Object>) modClass.getField("entityWhitelist").get(mod);
            final Set<Object> tickRuntime = (Set<Object>) modClass.getField("tickCullWhitelists").get(mod);

            boolean configChanged = false;
            int entityRuntimeEntriesAdded = 0;
            int blockRuntimeEntriesAdded = 0;
            boolean blockWhitelistApplied = false;
            for (Supplier<? extends EntityType<?>> holder : EntityReg.ALL_ENTITY_TYPES)
            {
                final EntityType<?> type = holder.get();
                final String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
                configChanged |= renderConfig.add(id);
                configChanged |= tickConfig.add(id);

                if (renderRuntime.add(type))
                {
                    entityRuntimeEntriesAdded++;
                }
                if (tickRuntime.add(type))
                {
                    entityRuntimeEntriesAdded++;
                }
            }

            // Block-entity whitelist fields differ in some Entity Culling versions. Keep entity exemptions active even
            // if an older version lacks the block-entity config/runtime sets.
            try
            {
                final Set<String> blockConfig = (Set<String>) configClass.getField("blockEntityWhitelist").get(config);
                final Set<Object> blockRuntime = (Set<Object>) modClass.getField("blockEntityWhitelist").get(mod);
                for (Supplier<? extends BlockEntityType<?>> holder : BlockReg.ALL_BLOCK_ENTITY_TYPES)
                {
                    final BlockEntityType<?> type = holder.get();
                    final String id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type).toString();
                    configChanged |= blockConfig.add(id);
                    if (blockRuntime.add(type))
                    {
                        blockRuntimeEntriesAdded++;
                    }
                }
                blockWhitelistApplied = true;
            }
            catch (Throwable blockError)
            {
                CreateICBM.logger().warn(
                    "Entity Culling entity exemptions succeeded, but block-entity render exemptions are unavailable in this version.", blockError);
            }

            if (configChanged)
            {
                final Method writeConfig = modClass.getMethod("writeConfig");
                writeConfig.invoke(mod);
            }

            if (configChanged || entityRuntimeEntriesAdded > 0 || blockRuntimeEntriesAdded > 0)
            {
                CreateICBM.logger().info(
                    "Entity Culling detected; exempted {} Create ICBM entity types from render/tick culling and {} block entity types from render culling",
                    EntityReg.ALL_ENTITY_TYPES.size(), blockWhitelistApplied ? BlockReg.ALL_BLOCK_ENTITY_TYPES.size() : 0);
            }
            complete = true;
        }
        catch (Throwable e)
        {
            if (!failureLogged)
            {
                CreateICBM.logger().warn("Entity Culling is installed, but automatic Create ICBM whitelisting failed. Add Create ICBM entity IDs to entityWhitelist and tickCullingWhitelist in config/entityculling.json", e);
                failureLogged = true;
            }
            complete = true;
        }
    }
}
