package com.createicbm.lib.capability.emp;

import com.createicbm.api.actions.IAction;
import com.createicbm.api.caps.IEMPReceiver;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.lib.InventoryUtility;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Wrapper to trigger EMP calls on ItemStack contained inside of {@link ItemEntity}
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/12/2018.
 */
public class CapabilityEmpEntityItem implements IEMPReceiver
{
    public final ItemEntity entityItem;

    public CapabilityEmpEntityItem(ItemEntity entityItem)
    {
        this.entityItem = entityItem;
    }

    @Override
    public float applyEmpAction(Level world, double x, double y, double z, IAction emp_blast, float power, boolean doAction)
    {
        if (ConfigBlast.emp.ALLOW_GROUND_ITEMS)
        {
            ItemStack stack = entityItem.getItem();
            if (!stack.isEmpty())
            {
                //Copy to prevent changes on real item
                stack = stack.copy();

                //Run call
                power = CapabilityEmpInventory.empItemStack(stack, world, x, y, z, entityItem, emp_blast, power, doAction);

                //Check for delta
                if (doAction && !InventoryUtility.stacksMatchExact(stack, entityItem.getItem()))
                {
                    entityItem.setItem(stack);
                }
            }
        }
        return power;
    }
}
