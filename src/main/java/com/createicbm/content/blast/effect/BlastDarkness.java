package com.createicbm.content.blast.effect;

import com.createicbm.client.ICBMSounds;
import com.createicbm.config.blast.ConfigBlast;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Gives every player and mob in range the Darkness effect
 */
public class BlastDarkness extends BlastAreaEffect
{
    @Override
    protected ICBMSounds getDetonationSound()
    {
        return ICBMSounds.DARKNESS;
    }

    @Override
    protected void applyEffect()
    {
        for (LivingEntity entity : getInRadius(LivingEntity.class))
        {
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, ConfigBlast.darkness.duration, 0, false, true, true));
        }
        if (world instanceof ServerLevel server)
        {
            final double spread = Math.min(getBlastRadius() / 3, 6);
            server.sendParticles(ParticleTypes.SQUID_INK, location.x(), location.y(), location.z(), 80, spread, spread / 2, spread, 0.1);
            server.sendParticles(ParticleTypes.SCULK_SOUL, location.x(), location.y(), location.z(), 30, spread, spread / 2, spread, 0.02);
            server.sendParticles(ParticleTypes.LARGE_SMOKE, location.x(), location.y(), location.z(), 40, spread, spread / 3, spread, 0.02);
        }
    }
}
