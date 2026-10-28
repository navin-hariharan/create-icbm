package com.createicbm.content.blast;

import com.createicbm.ICBMConstants;
import com.createicbm.client.ICBMSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Explosion sounds for everybody who can see a blast, with the delay sound has: a player 300 blocks away sees the
 * flash first and hears the bang a second later.
 * <p>
 * A vanilla sound only reaches players within 16 blocks per volume, and it is quiet well before that. Each player gets
 * their own sound packet instead, placed a few blocks from them in the direction of the blast, so it is always heard
 * whatever the distance:
 * <ul>
 *     <li>players inside the near radius (50 blocks for a nuke, 100 for antimatter) hear the blast's own sound at full
 *     volume,</li>
 *     <li>everybody else up to the server view distance hears the distant explosion, quieter the further they are.</li>
 * </ul>
 */
@EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public final class BlastSounds
{
    /** Blocks sound travels in a tick (343 m/s), a little faster than real so far blasts are not too late to matter */
    private static final double BLOCKS_PER_TICK = 20.0;
    /** Longest the bang is delayed */
    private static final int MAX_DELAY_TICKS = 40;
    /** How far from the player the sound is placed, close enough to not fade away before it is heard */
    private static final double PLACE_DISTANCE = 4.0;

    private record Pending(ServerLevel level, ServerPlayer player, long dueTick, ClientboundSoundPacket packet)
    {
    }

    private static final List<Pending> PENDING = new ArrayList<>();

    private BlastSounds()
    {
    }

    /**
     * @param near         sound for players inside {@code nearRadius}, null for none
     * @param nearVolume   volume of it
     * @param nearRadius   blocks around the blast where {@code near} is played, 0 for none
     * @param distant      sound for the players beyond that, up to the view distance, null for none
     * @param distantVolume volume of it at its loudest
     */
    public static void play(ServerLevel level, double x, double y, double z,
                            ICBMSounds near, float nearVolume, double nearRadius,
                            ICBMSounds distant, float distantVolume)
    {
        final double viewRange = level.getServer().getPlayerList().getViewDistance() * 16.0;
        final double reach = Math.max(viewRange, nearRadius);
        final double reachSq = reach * reach;
        final long now = level.getGameTime();

        for (ServerPlayer player : level.players())
        {
            final double dx = x - player.getX();
            final double dy = y - player.getY();
            final double dz = z - player.getZ();
            final double distanceSq = dx * dx + dy * dy + dz * dz;
            if (distanceSq > reachSq)
            {
                continue;
            }
            final double distance = Math.sqrt(distanceSq);

            final boolean isNear = near != null && distance <= nearRadius;
            final ICBMSounds sound = isNear ? near : distant;
            if (sound == null)
            {
                continue;
            }

            // The sound comes from the direction of the blast, a few blocks away, or from the blast when it is closer
            final double place = Math.min(distance, PLACE_DISTANCE);
            final double scale = distance < 1.0E-3 ? 0 : place / distance;
            final double sx = player.getX() + dx * scale;
            final double sy = player.getEyeY() + dy * scale;
            final double sz = player.getZ() + dz * scale;

            // Louder close up, never silent while in range
            final float volume = isNear
                ? nearVolume
                : distantVolume * (float) Mth.clamp(1.0 - 0.6 * distance / Math.max(1.0, viewRange), 0.4, 1.0);
            final float pitch = 0.9F + level.random.nextFloat() * 0.2F;

            final ClientboundSoundPacket packet = new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound.getSound()), SoundSource.BLOCKS,
                sx, sy, sz, volume, pitch, level.random.nextLong());

            final int delay = (int) Math.min(MAX_DELAY_TICKS, distance / BLOCKS_PER_TICK);
            if (delay <= 0)
            {
                player.connection.send(packet);
            }
            else
            {
                PENDING.add(new Pending(level, player, now + delay, packet));
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event)
    {
        if (PENDING.isEmpty() || !(event.getLevel() instanceof ServerLevel level))
        {
            return;
        }
        final long now = level.getGameTime();
        final Iterator<Pending> it = PENDING.iterator();
        while (it.hasNext())
        {
            final Pending pending = it.next();
            if (pending.player.isRemoved() || pending.level != pending.player.level())
            {
                // Left or changed dimension in the meantime
                it.remove();
            }
            else if (pending.level == level && now >= pending.dueTick)
            {
                pending.player.connection.send(pending.packet);
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event)
    {
        PENDING.clear();
    }
}
