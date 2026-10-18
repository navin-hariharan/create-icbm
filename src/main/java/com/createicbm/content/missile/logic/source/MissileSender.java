package com.createicbm.content.missile.logic.source;

import com.createicbm.api.actions.cause.IActionCause;
import com.createicbm.api.actions.cause.IActionSource;
import com.createicbm.content.blocks.mail.LauncherOwner;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.missile.logic.source.cause.CausedByBlock;
import com.createicbm.content.missile.logic.source.cause.EntityCause;
import com.createicbm.content.reg.AttachmentReg;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Works out which player sent a missile from its launch history: the player who pressed launch, used a remote or
 * fired a rocket launcher, or else the player who placed the launcher that fired it.
 */
public final class MissileSender
{
    private MissileSender()
    {
    }

    @Nullable
    public static LauncherOwner find(@Nullable IActionSource source)
    {
        return find(source, 0);
    }

    @Nullable
    private static LauncherOwner find(@Nullable IActionSource source, int depth)
    {
        if (source == null)
        {
            return null;
        }
        LauncherOwner blockOwner = null;
        IActionCause cause = source.getCause();
        for (int i = 0; cause != null && i < 32; i++)
        {
            if (cause instanceof EntityCause entityCause)
            {
                if (entityCause.isPlayer() && entityCause.getName() != null)
                {
                    final UUID id = entityCause.getId() != null ? entityCause.getId() : new UUID(0, 0);
                    return new LauncherOwner(id, entityCause.getName());
                }
                // A missile set off by another missile, follow that one's history
                final Entity entity = entityCause.getEntity();
                if (depth < 4 && entity instanceof EntityMissile<?> missile)
                {
                    final LauncherOwner sender = find(missile.getMissileCapability().getMissileSource(), depth + 1);
                    if (sender != null)
                    {
                        return sender;
                    }
                }
            }
            else if (blockOwner == null && cause instanceof CausedByBlock blockCause)
            {
                blockOwner = ownerOf(blockCause.getWorld(), blockCause);
            }
            cause = cause.getPreviousCause();
        }
        return blockOwner;
    }

    @Nullable
    private static LauncherOwner ownerOf(@Nullable Level level, CausedByBlock cause)
    {
        if (level == null || cause.getBlockPos() == null || !level.isLoaded(cause.getBlockPos()))
        {
            return null;
        }
        final BlockEntity tile = level.getBlockEntity(cause.getBlockPos());
        if (tile != null && tile.hasData(AttachmentReg.LAUNCHER_OWNER))
        {
            final LauncherOwner owner = tile.getData(AttachmentReg.LAUNCHER_OWNER);
            return owner.isEmpty() ? null : owner;
        }
        return null;
    }
}
