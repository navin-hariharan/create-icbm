package com.createicbm.lib.capability.missile;

import com.createicbm.ICBMConstants;
import com.createicbm.api.CreateICBMHelpers;
import com.createicbm.api.caps.IExplosive;
import com.createicbm.api.missiles.ICapabilityMissileStack;
import com.createicbm.api.missiles.IMissile;
import com.createicbm.api.reg.IExplosiveData;
import com.createicbm.content.missile.entity.explosive.EntityExplosiveMissile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Applied to {@link ItemStack} that are missiles
 */
public class CapabilityMissileStack implements ICapabilityMissileStack
{
    private final ItemStack stack; //TODO decouple from stack and directly save init data

    public CapabilityMissileStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public String getMissileId() {
        return ICBMConstants.PREFIX + "missile["
            + Optional.ofNullable(CreateICBMHelpers.getExplosive(stack))
            .map(IExplosive::getExplosiveData)
            .map(IExplosiveData::getRegistryKey)
            .map(Object::toString)
            .orElse("unknown")
            + "]";
    }

    @Override
    public IMissile newMissile(Level world)
    {
        final EntityExplosiveMissile missile = new EntityExplosiveMissile(world);
        missile.explosive.setStack(stack);
        missile.setHealth(missile.getMaxHealth());
        return missile.getMissileCapability();
    }

}
