package com.createicbm.content.missile.entity.delivery;

import com.createicbm.ICBMConstants;
import com.createicbm.api.missiles.ICapabilityMissileStack;
import com.createicbm.api.missiles.IMissile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Builds a {@link EntityDeliveryMissile} carrying the stack's cargo
 */
public class CapabilityDeliveryMissile implements ICapabilityMissileStack
{
    private final ItemStack host;

    public CapabilityDeliveryMissile(ItemStack host)
    {
        this.host = host;
    }

    @Override
    public String getMissileId()
    {
        return ICBMConstants.PREFIX + "missile.delivery";
    }

    @Override
    public IMissile newMissile(Level world)
    {
        final EntityDeliveryMissile missile = new EntityDeliveryMissile(world);
        missile.getCargo().setStackInSlot(0, ItemDeliveryMissile.getCargo(host));
        return missile.getMissileCapability();
    }
}
