package com.createicbm.lib.capability.emp;

import com.createicbm.api.actions.IAction;
import com.createicbm.api.caps.IEMPReceiver;
import com.createicbm.config.blast.ConfigBlast;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/12/2018.
 */
public class CapabilityEmpCreeper implements IEMPReceiver
{
    public final Creeper creeper;

    public CapabilityEmpCreeper(Creeper creeper)
    {
        this.creeper = creeper;
    }

    @Override
    public float applyEmpAction(Level world, double x, double y, double z, IAction emp_blast, float power, boolean doAction)
    {
        if (ConfigBlast.emp.ALLOW_LIGHTING_CREEPER && world instanceof ServerLevel serverLevel)
        {
            //Attack creeper with lighting TODO replace with data manager call
            final LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(world);
            if (bolt != null)
            {
                bolt.moveTo(creeper.getX(), creeper.getY(), creeper.getZ());
                bolt.setVisualOnly(true);
                creeper.thunderHit(serverLevel, bolt);
            }
        }
        return power;
    }
}
