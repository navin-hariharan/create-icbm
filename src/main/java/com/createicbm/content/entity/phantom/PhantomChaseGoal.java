package com.createicbm.content.entity.phantom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Runs at the player and swings at them (or starts hissing, for a creeper) without ever landing a hit. Unlike the
 * vanilla attack goals it doesn't give up on players in creative mode, so the illusion works for everyone.
 */
public class PhantomChaseGoal<T extends PathfinderMob & PhantomMob> extends Goal
{
    private final T mob;
    private int repathDelay;
    private int swingDelay;

    public PhantomChaseGoal(T mob)
    {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse()
    {
        return mob.phantom().victim() != null;
    }

    @Override
    public boolean requiresUpdateEveryTick()
    {
        return true;
    }

    @Override
    public void start()
    {
        mob.setAggressive(true);
        repathDelay = 0;
    }

    @Override
    public void stop()
    {
        mob.setAggressive(false);
        mob.getNavigation().stop();
        if (mob instanceof Creeper creeper)
        {
            creeper.setSwellDir(-1);
        }
    }

    @Override
    public void tick()
    {
        final Player victim = mob.phantom().victim();
        if (victim == null)
        {
            return;
        }
        mob.getLookControl().setLookAt(victim, 30, 30);

        final double distance = mob.distanceToSqr(victim);
        if (--repathDelay <= 0)
        {
            repathDelay = 4 + mob.getRandom().nextInt(7);
            mob.getNavigation().moveTo(victim, 1.0);
        }

        if (mob instanceof Creeper creeper)
        {
            // Starts to swell up close, the creeper vanishes before it would go off
            creeper.setSwellDir(distance < 9 ? 1 : -1);
            return;
        }

        if (swingDelay > 0)
        {
            swingDelay--;
        }
        final double reach = mob.getBbWidth() * 2.0;
        if (swingDelay == 0 && distance <= reach * reach + victim.getBbWidth())
        {
            mob.swing(InteractionHand.MAIN_HAND);
            swingDelay = 20;
        }
    }
}
