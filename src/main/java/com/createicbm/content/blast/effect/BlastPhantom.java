package com.createicbm.content.blast.effect;

import com.createicbm.client.ICBMSounds;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.content.entity.phantom.PhantomLogic;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Surrounds every player in range with harmless phantom mobs that come at them as if to attack
 */
public class BlastPhantom extends BlastAreaEffect
{
    @Override
    protected ICBMSounds getDetonationSound()
    {
        return ICBMSounds.PHANTOM_SUMMON;
    }

    @Override
    protected void applyEffect()
    {
        if (!(world instanceof ServerLevel server))
        {
            return;
        }
        for (ServerPlayer player : getInRadius(ServerPlayer.class))
        {
            if (!player.isSpectator())
            {
                PhantomLogic.haunt(server, player, ConfigBlast.phantom.perPlayer);
            }
        }
        server.sendParticles(ParticleTypes.LARGE_SMOKE, location.x(), location.y(), location.z(), 40, 1.5, 0.6, 1.5, 0.02);
        server.sendParticles(ParticleTypes.WITCH, location.x(), location.y(), location.z(), 40, 1.5, 0.8, 1.5, 0.4);
        server.sendParticles(ParticleTypes.SOUL, location.x(), location.y(), location.z(), 20, 1.2, 0.5, 1.2, 0.03);
    }
}
