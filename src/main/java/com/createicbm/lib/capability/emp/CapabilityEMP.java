package com.createicbm.lib.capability.emp;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.caps.IEMPReceiver;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * The capabilities are declared in {@link CreateICBMAPI} (EMP_ENTITY/BLOCK/ITEM_CAPABILITY) and vanilla providers are
 * registered in {@link #registerCapabilities}. The wrappers held no state, so a new instance is returned for each
 * lookup.
 */
public class CapabilityEMP
{
    @Deprecated
    public static final EntityCapability<IEMPReceiver, Void> EMP = CreateICBMAPI.EMP_ENTITY_CAPABILITY;

    /**
     * Registers EMP handling for vanilla objects, called from CapabilityReg on the mod bus.
     * Energy storages (tiles, entities, items) are drained by {@link com.createicbm.content.actions.emp.ActionEmpArea}
     * through the energy system and inventories are wrapped on demand by {@link CapabilityEmpInventory}, neither needs
     * a registered provider.
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.registerEntity(CreateICBMAPI.EMP_ENTITY_CAPABILITY, EntityType.ITEM, (entity, ctx) -> new CapabilityEmpEntityItem((ItemEntity) entity));
        event.registerEntity(CreateICBMAPI.EMP_ENTITY_CAPABILITY, EntityType.CREEPER, (entity, ctx) -> new CapabilityEmpCreeper((Creeper) entity));
    }
}
