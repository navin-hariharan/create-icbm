package com.createicbm.content.missile.entity.anti.item;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.lib.capability.holder.ICapabilityHolderItem;
import com.createicbm.prefab.item.ItemICBMBase;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nullable;

/**
 * Surface to Air Missile (SAM), made to attack enemy missiles. It can also target any entity found in the air.
 */
public class ItemSurfaceToAirMissile extends ItemICBMBase implements ICapabilityHolderItem
{
    public ItemSurfaceToAirMissile()
    {
        super("surface_to_air_missile", new Item.Properties().stacksTo(1));
    }

    @Nullable
    @Override
    public <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack)
    {
        if (capability == CreateICBMAPI.MISSILE_STACK_CAPABILITY)
        {
            return (T) new CapabilitySAMStack();
        }
        return null;
    }
}
