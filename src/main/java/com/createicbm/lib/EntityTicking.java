package com.createicbm.lib;

import net.minecraft.world.entity.Entity;

/**
 * Marks gameplay-critical ICBM entities as non-cullable for optimizers that honour Entity.noCulling.
 *
 * This flag is only one layer: ServerCore caches activation-range exclusions and Does Potato Tick caches its own
 * always-tick state, so their optional compatibility hooks update those APIs directly. Client render/tick culling is
 * handled separately by EntityCullingCompat.
 */
public final class EntityTicking
{
    private EntityTicking()
    {
    }

    /**
     * Call from an entity constructor. This alone does not override server-side entity schedulers; the optional
     * compatibility hooks update those schedulers' per-entity state directly.
     */
    public static void keepTicking(Entity entity)
    {
        if (entity != null && entity.level() != null && !entity.level().isClientSide())
        {
            entity.noCulling = true;
        }
    }
}
