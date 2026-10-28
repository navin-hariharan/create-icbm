package com.createicbm.lib;

import net.minecraft.world.entity.Entity;

/**
 * Entity tick throttling mods common in large modpacks (Immersive Optimization and the like) slow down or pause
 * entities far from players. Missiles and blasts then stalled mid-air until a player came close. Those mods let
 * entities with {@link Entity#noCulling} tick every tick, so the server copy is flagged; the client copy keeps normal
 * render culling.
 * <p>
 * Not every optimizer honours the flag (Immersive Optimization documents a projectile exemption and a namespace
 * blacklist, not noCulling, and these entities are not vanilla projectiles). If missiles still stall far from players,
 * blacklist the {@code create_icbm} namespace in the optimizer's config. Missiles in flight also hold their own chunk
 * tickets, see {@link com.createicbm.content.missile.tracker.MissileChunkLoader}.
 */
public final class EntityTicking
{
    private EntityTicking()
    {
    }

    /**
     * Call from the entity constructor
     */
    public static void keepTicking(Entity entity)
    {
        if (entity.level() != null && !entity.level().isClientSide())
        {
            entity.noCulling = true;
        }
    }
}
