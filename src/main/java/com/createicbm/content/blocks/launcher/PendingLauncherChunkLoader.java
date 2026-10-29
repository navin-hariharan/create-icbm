package com.createicbm.content.blocks.launcher;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.world.chunk.TicketController;

/**
 * Keeps a launcher block entity ticking while it has a queued firing package.
 *
 * <p>RadioCruise acknowledges a received trigger when it queues the firing package. Without a chunk ticket, the
 * launcher block entity can then stop ticking outside a player's simulation distance and the missile appears to
 * launch only when a player returns. NeoForge's persistent ticking ticket lets the countdown/aiming complete with
 * no nearby (or online) players, and resumes a queued launch after a server restart.</p>
 *
 * <p>Tickets are owned by the block position and must be removed when the package finishes or the launcher is broken.
 * On chunk load, launchers call {@link #sync(BlockEntity, boolean)} so a stale ticket left by a crash is removed when
 * there is no saved firing package.</p>
 */
public final class PendingLauncherChunkLoader
{
    public static final TicketController TICKET_CONTROLLER = new TicketController(
        ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "pending_launcher"));

    private PendingLauncherChunkLoader()
    {
    }

    /** Syncs the persistent ticking ticket with the launcher's saved/runtime state. Server-thread only. */
    public static void sync(BlockEntity launcher, boolean hasPendingLaunch)
    {
        final Level level = launcher.getLevel();
        if (!(level instanceof ServerLevel serverLevel) || level.isClientSide())
        {
            return;
        }

        final BlockPos pos = launcher.getBlockPos().immutable();
        final boolean changed;
        try
        {
            changed = TICKET_CONTROLLER.forceChunk(
                serverLevel, pos, pos.getX() >> 4, pos.getZ() >> 4, hasPendingLaunch, true);
        }
        catch (RuntimeException exception)
        {
            ICBMDiagnostics.error("PENDING_LAUNCHER_TICKET_FAILED pos=" + pos + " dim=" + level.dimension().location()
                + " pending=" + hasPendingLaunch, exception);
            return;
        }

        ICBMDiagnostics.info("PENDING_LAUNCHER_TICKET action={} pos={} dim={} chunk={},{} changed={} pending={}",
            hasPendingLaunch ? "ADD_OR_KEEP" : "REMOVE", pos, level.dimension().location(), pos.getX() >> 4,
            pos.getZ() >> 4, changed, hasPendingLaunch);
    }

    /** Called when the launcher block itself is removed; it also cleans up persistent tickets. */
    public static void removeAt(Level level, BlockPos pos)
    {
        if (!(level instanceof ServerLevel serverLevel) || level.isClientSide())
        {
            return;
        }

        final BlockPos immutablePos = pos.immutable();
        try
        {
            final boolean changed = TICKET_CONTROLLER.forceChunk(
                serverLevel, immutablePos, immutablePos.getX() >> 4, immutablePos.getZ() >> 4, false, true);
            ICBMDiagnostics.info("PENDING_LAUNCHER_TICKET action=BLOCK_REMOVED pos={} dim={} changed={}",
                immutablePos, level.dimension().location(), changed);
        }
        catch (RuntimeException exception)
        {
            ICBMDiagnostics.error("PENDING_LAUNCHER_TICKET_REMOVE_FAILED pos=" + immutablePos
                + " dim=" + level.dimension().location(), exception);
        }
    }
}
