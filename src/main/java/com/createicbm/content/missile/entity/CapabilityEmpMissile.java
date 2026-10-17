package com.createicbm.content.missile.entity;

import com.createicbm.api.actions.IAction;
import com.createicbm.api.caps.IEMPReceiver;
import com.createicbm.api.missiles.IMissile;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.content.missile.entity.anti.EntitySurfaceToAirMissile;
import com.createicbm.content.missile.entity.explosive.EntityExplosiveMissile;
import com.createicbm.content.missile.entity.explosive.EntityMissileActionable;
import com.createicbm.content.missile.logic.flight.DeadFlightLogic;
import net.minecraft.world.level.Level;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/12/2018.
 */
public class CapabilityEmpMissile implements IEMPReceiver
{
    final IMissile missile;
    public CapabilityEmpMissile(IMissile missile)
    {
       this.missile = missile;
    }

    @Override
    public float applyEmpAction(Level world, double x, double y, double z, IAction emp_blast, float power, boolean doAction)
    {
        if(ConfigBlast.emp.missiles.enabled && missile.getMissileEntity() != null && missile.getMissileEntity().isAlive())
        {
            if (doAction)
            {
                if(world.random.nextFloat() <= ConfigBlast.emp.missiles.missileKillChance) {
                    missile.getMissileEntity().discard();

                    // Boom
                    if(world.random.nextFloat() <= ConfigBlast.emp.missiles.engineFullBlowChance) {
                        world.explode(missile.getMissileEntity(), x, y, z, ConfigBlast.emp.missiles.engineFullBlowStrength, Level.ExplosionInteraction.NONE);
                        // TODO drop fragments of missile that can still cause damage
                    }
                    //TODO else drop item or dead entity?
                }
                // Fry guidance systems
                else if(world.random.nextFloat() <= ConfigBlast.emp.missiles.engineKillChance) {
                    missile.setFlightLogic(new DeadFlightLogic(0));
                }
                // Trigger firing circuits
                else if(world.random.nextFloat() <= ConfigBlast.emp.missiles.missileTriggerChance) {
                    if(missile.getMissileEntity() instanceof EntityExplosiveMissile) {
                        ((EntityExplosiveMissile) missile.getMissileEntity()).explosive
                            .doExplosion(x, y, z, emp_blast.getSource()); //TODO encode source to note emp death
                    }
                    else if(missile.getMissileEntity() instanceof EntityMissileActionable) {
                        ((EntityMissileActionable) missile.getMissileEntity()).getMainAction().
                            doAction(world, x, y, z, emp_blast.getSource().getCause()); //TODO encode source to note emp death
                    }
                    else if(missile.getMissileEntity() instanceof EntitySurfaceToAirMissile) {
                        world.explode(missile.getMissileEntity(), x, y, z, ConfigBlast.emp.missiles.engineFullBlowStrength, Level.ExplosionInteraction.NONE);
                    }
                }
                // TODO chance for missile to fry trigger but still land
            }
        }
        return power;
    }
}
