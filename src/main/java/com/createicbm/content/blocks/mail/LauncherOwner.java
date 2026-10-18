package com.createicbm.content.blocks.mail;

import com.createicbm.content.blocks.launcher.base.TileLauncherBase;
import com.createicbm.content.blocks.launcher.cruise.TileCruiseLauncher;
import com.createicbm.content.blocks.launcher.screen.TileLauncherScreen;
import com.createicbm.content.reg.AttachmentReg;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.UUID;

/**
 * Player who placed a launcher block
 */
public record LauncherOwner(UUID id, String name)
{
    public static final LauncherOwner NONE = new LauncherOwner(new UUID(0, 0), "");

    public static final Codec<LauncherOwner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("id").forGetter(LauncherOwner::id),
        Codec.STRING.fieldOf("name").forGetter(LauncherOwner::name)
    ).apply(instance, LauncherOwner::new));

    public boolean isEmpty()
    {
        return name.isEmpty();
    }

    /** Game bus listener, remembers who placed each launcher */
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event)
    {
        if (event.getEntity() instanceof Player player && !event.getLevel().isClientSide())
        {
            final BlockEntity tile = event.getLevel().getBlockEntity(event.getPos());
            if (tile instanceof TileLauncherBase || tile instanceof TileLauncherScreen || tile instanceof TileCruiseLauncher)
            {
                tile.setData(AttachmentReg.LAUNCHER_OWNER, new LauncherOwner(player.getUUID(), player.getGameProfile().getName()));
                tile.setChanged();
            }
        }
    }
}
