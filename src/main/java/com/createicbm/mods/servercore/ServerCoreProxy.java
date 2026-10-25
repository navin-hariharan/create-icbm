package com.createicbm.mods.servercore;

import com.createicbm.CreateICBM;
import com.createicbm.content.reg.EntityReg;
import com.createicbm.mods.ModProxy;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * ServerCore's activation range (off by default) stops entities far from players. Missiles and blasts then froze in
 * the air, so they are added to its excluded entity types when the server starts. ServerCore is optional and only
 * reached by reflection, a failure only logs a warning.
 */
public class ServerCoreProxy extends ModProxy
{
    public static final String MOD_ID = "servercore";

    public static final ServerCoreProxy INSTANCE = new ServerCoreProxy();

    /** Entities that have to keep ticking far from players */
    private static final List<Supplier<? extends EntityType<?>>> ALWAYS_TICK = List.of(
        EntityReg.MISSILE_EXPLOSIVE, EntityReg.MISSILE_GENERIC, EntityReg.MISSILE_SAM, EntityReg.MISSILE_DELIVERY,
        EntityReg.EXPLOSION, EntityReg.REDMATTER, EntityReg.BEAM, EntityReg.BLOCK_EXPLOSIVE, EntityReg.BOMB_CART,
        EntityReg.BLOCK_FRAGMENT, EntityReg.BLOCK_GRAVITY);

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
            for (Supplier<? extends EntityType<?>> type : ALWAYS_TICK)
            {
                types.add(type.get());
            }
        }
        catch (Throwable e)
        {
            CreateICBM.logger().warn("ServerCore support: could not exclude missiles from its activation range. If it is enabled, add create_icbm entities to 'excluded-entity-types' in ServerCore's config", e);
        }
    }
}
