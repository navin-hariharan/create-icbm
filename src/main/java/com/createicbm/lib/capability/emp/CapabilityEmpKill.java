package com.createicbm.lib.capability.emp;

import com.createicbm.api.actions.IAction;
import com.createicbm.api.caps.IEMPReceiver;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Capability that destroys the host
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/12/2018.
 */
public class CapabilityEmpKill<E extends Entity> implements IEMPReceiver
{
    public final E entity;

    public CapabilityEmpKill(E entity)
    {
        this.entity = entity;
    }

    @Override
    public float applyEmpAction(Level world, double x, double y, double z, IAction emp_blast, float power, boolean doAction)
    {
        if (doAction)
        {
            setDeadEmp(emp_blast, power); //TODO drop item?
        }
        return power;
    }

    protected void setDeadEmp(IAction emp_blast, float power)
    {
        entity.discard();
    }
}
