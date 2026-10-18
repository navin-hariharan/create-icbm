package com.createicbm.content.entity;

import com.createicbm.content.missile.logic.source.cause.EntityCause;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Player who lit an explosive crate or set off a bomb cart. Goes into the blast's cause ahead of the crate or cart, the
 * same way a missile's cause leads back to whoever launched it, so blasts like recon know who to spare.
 */
public final class Igniter
{
    private static final String NBT_IGNITER = "igniter";

    private Igniter()
    {
    }

    /**
     * @param entity that lit the explosive, only players are kept
     * @return cause naming the player without holding on to them, null if not a player
     */
    @Nullable
    public static EntityCause of(@Nullable Entity entity)
    {
        if (!(entity instanceof Player player))
        {
            return null;
        }
        final EntityCause cause = new EntityCause();
        cause.setName(player.getGameProfile().getName());
        cause.setId(player.getUUID());
        cause.setPlayer(true);
        return cause;
    }

    /**
     * Cause for the blast of a crate or cart, with the igniter before it if known
     */
    public static EntityCause blastCause(Entity explosive, @Nullable EntityCause igniter)
    {
        final EntityCause cause = new EntityCause(explosive);
        cause.setPreviousCause(igniter);
        return cause;
    }

    public static void save(CompoundTag nbt, HolderLookup.Provider registries, @Nullable EntityCause igniter)
    {
        if (igniter != null)
        {
            nbt.put(NBT_IGNITER, igniter.serializeNBT(registries));
        }
    }

    @Nullable
    public static EntityCause load(CompoundTag nbt, HolderLookup.Provider registries)
    {
        if (!nbt.contains(NBT_IGNITER, Tag.TAG_COMPOUND))
        {
            return null;
        }
        final EntityCause cause = new EntityCause();
        cause.deserializeNBT(registries, nbt.getCompound(NBT_IGNITER));
        return cause;
    }
}
