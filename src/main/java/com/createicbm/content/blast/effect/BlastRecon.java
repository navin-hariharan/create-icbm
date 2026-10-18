package com.createicbm.content.blast.effect;

import com.createicbm.client.ICBMSounds;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.content.blocks.mail.LauncherOwner;
import com.createicbm.content.missile.logic.source.MissileSender;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.scores.PlayerTeam;

import javax.annotation.Nullable;

/**
 * Outlines every mob and enemy player in range, visible through walls. The player who sent the missile (or lit the
 * crate or cart) and their team (and their pets) are left alone.
 */
public class BlastRecon extends BlastAreaEffect
{
    @Override
    protected ICBMSounds getDetonationSound()
    {
        return ICBMSounds.RECON_PULSE;
    }

    @Override
    protected void applyEffect()
    {
        final LauncherOwner sender = MissileSender.find(getSource());
        // Looked up by name, so the team still counts when the sender is offline or in another dimension
        final PlayerTeam senderTeam = sender != null ? world.getScoreboard().getPlayersTeam(sender.name()) : null;

        for (LivingEntity entity : getInRadius(LivingEntity.class))
        {
            if (!isFriendly(entity, sender, senderTeam))
            {
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, ConfigBlast.recon.duration, 0, false, false, true));
            }
        }

        if (world instanceof ServerLevel server)
        {
            // A ring of sparks showing the scanned area
            final double radius = getBlastRadius();
            final int points = (int) Math.min(96, radius * 4);
            for (int i = 0; i < points; i++)
            {
                final double angle = Math.PI * 2 * i / points;
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    location.x() + Math.cos(angle) * radius, location.y() + 0.5, location.z() + Math.sin(angle) * radius,
                    1, 0, 0, 0, 0);
            }
            server.sendParticles(ParticleTypes.END_ROD, location.x(), location.y(), location.z(), 30, 0.3, 0.3, 0.3, 0.15);
        }
    }

    private static boolean isFriendly(LivingEntity entity, @Nullable LauncherOwner sender, @Nullable PlayerTeam senderTeam)
    {
        if (sender == null)
        {
            return false;
        }
        // The sender themself, and their tamed pets and horses
        if (entity.getUUID().equals(sender.id()) || entity instanceof OwnableEntity pet && sender.id().equals(pet.getOwnerUUID()))
        {
            return true;
        }
        return senderTeam != null && senderTeam.isAlliedTo(entity.getTeam());
    }
}
