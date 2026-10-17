package com.createicbm.content.reg;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.lib.capability.emp.CapabilityEMP;
import com.createicbm.lib.capability.holder.ICapabilityHolderEntity;
import com.createicbm.lib.capability.holder.ICapabilityHolderItem;
import com.createicbm.lib.capability.holder.ICapabilityHolderTile;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CapabilityReg
{
    /** Block capabilities ICBM block entities may expose */
    @SuppressWarnings("unchecked")
    private static final List<BlockCapability<?, @Nullable Direction>> BLOCK_CAPS = List.of(
        CreateICBMAPI.EMP_BLOCK_CAPABILITY,
        CreateICBMAPI.MISSILE_HOLDER_CAPABILITY,
        CreateICBMAPI.MISSILE_LAUNCHER_CAPABILITY,
        CreateICBMAPI.RADIO_CAPABILITY,
        CreateICBMAPI.EXPLOSIVE_BLOCK_CAPABILITY,
        Capabilities.ItemHandler.BLOCK
    );

    /** Entity capabilities ICBM entities may expose */
    private static final List<EntityCapability<?, Void>> ENTITY_CAPS = List.of(
        CreateICBMAPI.EMP_ENTITY_CAPABILITY,
        CreateICBMAPI.MISSILE_CAPABILITY,
        CreateICBMAPI.EXPLOSIVE_ENTITY_CAPABILITY,
        CreateICBMAPI.BLAST_CAPABILITY,
        CreateICBMAPI.BLAST_VELOCITY_CAPABILITY
    );

    /** Sided entity capabilities ICBM entities may expose */
    private static final List<EntityCapability<?, @Nullable Direction>> ENTITY_SIDED_CAPS = List.of(
        Capabilities.EnergyStorage.ENTITY,
        Capabilities.ItemHandler.ENTITY_AUTOMATION
    );

    /** Item capabilities ICBM items may expose */
    private static final List<ItemCapability<?, Void>> ITEM_CAPS = List.of(
        CreateICBMAPI.EMP_ITEM_CAPABILITY,
        CreateICBMAPI.RADIO_ITEM_CAPABILITY,
        CreateICBMAPI.GPS_CAPABILITY,
        CreateICBMAPI.EXPLOSIVE_ITEM_CAPABILITY,
        CreateICBMAPI.MISSILE_STACK_CAPABILITY,
        Capabilities.ItemHandler.ITEM
    );

    private CapabilityReg()
    {
    }

    /** Mod bus listener */
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        for (DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<?>> holder : BlockReg.BLOCK_ENTITIES.getEntries())
        {
            registerTile(event, holder.get());
        }
        for (DeferredHolder<EntityType<?>, ? extends EntityType<?>> holder : EntityReg.ENTITIES.getEntries())
        {
            registerEntity(event, holder.get());
        }
        for (DeferredHolder<Item, ? extends Item> holder : ItemReg.ITEMS.getEntries())
        {
            if (holder.get() instanceof ICapabilityHolderItem)
            {
                registerItem(event, holder.get());
            }
        }

        // Vanilla objects (item entities, creepers, energy items...)
        CapabilityEMP.registerCapabilities(event);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerTile(RegisterCapabilitiesEvent event, BlockEntityType<?> type)
    {
        for (BlockCapability cap : BLOCK_CAPS)
        {
            event.registerBlockEntity(cap, (BlockEntityType<BlockEntity>) type, (tile, side) -> {
                if (tile instanceof ICapabilityHolderTile holder)
                {
                    return holder.getCapability(cap, (Direction) side);
                }
                return null;
            });
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerEntity(RegisterCapabilitiesEvent event, EntityType<?> type)
    {
        for (EntityCapability cap : ENTITY_CAPS)
        {
            event.registerEntity(cap, (EntityType<Entity>) type, (entity, ctx) -> {
                if (entity instanceof ICapabilityHolderEntity holder)
                {
                    return holder.getEntityCapability(cap);
                }
                return null;
            });
        }
        for (EntityCapability cap : ENTITY_SIDED_CAPS)
        {
            event.registerEntity(cap, (EntityType<Entity>) type, (entity, side) -> {
                if (entity instanceof ICapabilityHolderEntity holder)
                {
                    return holder.getSidedCapability(cap, (Direction) side);
                }
                return null;
            });
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerItem(RegisterCapabilitiesEvent event, Item item)
    {
        for (ItemCapability cap : ITEM_CAPS)
        {
            // Raw capability makes the lambda parameters Object, so cast the stack explicitly
            event.registerItem(cap, (stack, ctx) -> ((ICapabilityHolderItem) ((ItemStack) stack).getItem()).getCapability(cap, (ItemStack) stack), item);
        }
    }
}
