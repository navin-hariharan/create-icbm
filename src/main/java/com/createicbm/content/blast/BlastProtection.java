package com.createicbm.content.blast;

import com.createicbm.CreateICBM;
import com.createicbm.config.blast.ConfigBlast;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Asks claim and protection mods (FTB Chunks, Open Parties and Claims...) which block edits a blast may make, through
 * the same {@link ExplosionEvent.Detonate} they already listen to for vanilla explosions. Every blast and Redmatter
 * use this one helper.
 * <p>
 * Listeners take out the positions they protect. Positions a listener adds are ignored, a blast only edits what it
 * worked out itself. If a listener fails the check fails closed: nothing in the batch is edited, so a broken mod can
 * never open up a claim.
 */
public final class BlastProtection
{
    /** Errors from listeners are logged at most this often (ms), a failing mod would otherwise flood the log */
    private static final long ERROR_LOG_INTERVAL = 60_000;
    private static long lastErrorLog = 0;

    private BlastProtection()
    {
    }

    /**
     * Server thread only.
     *
     * @param explosion the blast as a vanilla explosion, its center is where the blast is now
     * @param positions blocks about to be edited, not changed
     * @return the positions that must not be edited, empty when all are allowed
     */
    public static Set<BlockPos> denied(Level level, Explosion explosion, Collection<BlockPos> positions)
    {
        if (!ConfigBlast.PROTECTION_EVENTS || positions.isEmpty() || !(level instanceof ServerLevel))
        {
            return Collections.emptySet();
        }
        final List<BlockPos> toBlow = explosion.getToBlow();
        toBlow.clear();
        toBlow.addAll(positions);
        try
        {
            NeoForge.EVENT_BUS.post(new ExplosionEvent.Detonate(level, explosion, new ArrayList<>()));
            // Compared by content, a listener adding positions while another removes as many keeps the size
            final Set<BlockPos> allowed = new HashSet<>(toBlow);
            Set<BlockPos> denied = null;
            for (BlockPos pos : positions)
            {
                if (!allowed.contains(pos))
                {
                    if (denied == null)
                    {
                        denied = new HashSet<>();
                    }
                    denied.add(pos);
                }
            }
            return denied == null ? Collections.emptySet() : denied;
        }
        catch (Throwable e)
        {
            final long now = System.currentTimeMillis();
            if (now - lastErrorLog >= ERROR_LOG_INTERVAL)
            {
                lastErrorLog = now;
                CreateICBM.logger().error("A mod failed while checking a blast for claims, the blocks in this batch were left alone. Logged once a minute.", e);
            }
            return positions instanceof Set<BlockPos> set ? set : new HashSet<>(positions);
        }
        finally
        {
            toBlow.clear();
        }
    }

    /**
     * @return true if the single position may be edited, see {@link #denied}
     */
    public static boolean allowed(Level level, Explosion explosion, BlockPos pos)
    {
        return denied(level, explosion, List.of(pos)).isEmpty();
    }
}
