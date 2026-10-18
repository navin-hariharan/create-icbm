package com.createicbm.content.blast.effect;

import com.createicbm.client.ICBMSounds;
import com.createicbm.content.blast.Blast;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Blast that breaks nothing and only does something to the mobs and players around it. It still goes off with the
 * usual explosion bang, plus a sound of its own
 */
public abstract class BlastAreaEffect extends Blast
{
    protected abstract ICBMSounds getDetonationSound();

    @Override
    protected void playExplodeSound()
    {
        // The usual missile hit bang, with the warhead's own sound on top
        super.playExplodeSound();
        getDetonationSound().play(world, location.x(), location.y(), location.z(), 4.0F, 0.95F + world.random.nextFloat() * 0.1F, true);
    }

    /**
     * Entities of the type inside the blast sphere
     */
    protected <T extends Entity> List<T> getInRadius(Class<T> type)
    {
        final double radius = getBlastRadius();
        final Vec3 center = new Vec3(location.x(), location.y(), location.z());
        return world.getEntitiesOfClass(type, new AABB(center, center).inflate(radius),
            entity -> entity.isAlive() && entity.distanceToSqr(center) <= radius * radius);
    }

    @Override
    public boolean doExplode(int callCount)
    {
        if (!world.isClientSide())
        {
            applyEffect();
        }
        return true;
    }

    /** Server only, runs once */
    protected abstract void applyEffect();
}
