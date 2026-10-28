package com.createicbm.content.blast;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BlastMutation extends Blast
{
    @Override
    public boolean doExplode(int callCount)
    {
        if (!this.world().isClientSide())
        {
            final AABB bounds = new AABB(location.x() - this.getBlastRadius(), location.y() - this.getBlastRadius(), location.z() - this.getBlastRadius(), location.x() + this.getBlastRadius(), location.y() + this.getBlastRadius(), location.z() + this.getBlastRadius());
            final List<Mob> entitiesNearby = world().getEntitiesOfClass(Mob.class, bounds);

            for (Mob entity : entitiesNearby)
            {
                applyMutationEffect(entity);
            }
        }
        return true;
    }

    public static boolean applyMutationEffect(final LivingEntity entity)
    {
        if (entity instanceof Pig pig)
        {
            return pig.convertTo(EntityType.ZOMBIFIED_PIGLIN, false) != null;
        }
        else if (entity instanceof Villager villager)
        {
            final ZombieVillager newEntity = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
            if (newEntity != null)
            {
                newEntity.setVillagerData(villager.getVillagerData());
                return true;
            }
        }
        return false;
    }

    @Override //disable the sound for this explosive
    protected void playExplodeSound()
    {
    }

    /** Not an explosion, so no distant boom for people who can see it */
    @Override
    protected boolean playsDistantSound()
    {
        return false;
    }
}
