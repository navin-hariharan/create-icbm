package com.createicbm.content.blast.effect;

import com.createicbm.client.ICBMSounds;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.lib.network.packet.PacketScreenFlash;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Turns the screen of every player in range white for a moment
 */
public class BlastFlashbang extends BlastAreaEffect
{
    @Override
    protected ICBMSounds getDetonationSound()
    {
        return ICBMSounds.FLASHBANG;
    }

    @Override
    protected void applyEffect()
    {
        for (ServerPlayer player : getInRadius(ServerPlayer.class))
        {
            if (!player.isSpectator())
            {
                PacketScreenFlash.send(player, ConfigBlast.flashbang.duration);
            }
        }
        if (world instanceof ServerLevel server)
        {
            server.sendParticles(ParticleTypes.FLASH, location.x(), location.y(), location.z(), 1, 0, 0, 0, 0);
            server.sendParticles(ParticleTypes.END_ROD, location.x(), location.y(), location.z(), 40, 0.4, 0.4, 0.4, 0.25);
            server.sendParticles(ParticleTypes.CLOUD, location.x(), location.y(), location.z(), 15, 0.6, 0.3, 0.6, 0.05);
        }
    }
}
