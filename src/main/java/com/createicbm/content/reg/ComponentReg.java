package com.createicbm.content.reg;

import com.mojang.serialization.Codec;
import com.createicbm.ICBMConstants;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item stack data components. Other legacy stack NBT is stored through {@link com.createicbm.lib.StackNbt} in vanilla
 * custom data.
 */
public final class ComponentReg
{
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(ICBMConstants.DOMAIN);

    /** Explosive type of a stack, registry name from {@link com.createicbm.api.reg.IExplosiveRegistry}. Replaces explosive metadata. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> EXPLOSIVE = COMPONENTS.registerComponentType("explosive",
        builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC));

    /** Stored energy for electrical items, used with NeoForge's ComponentEnergyStorage */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY = COMPONENTS.registerComponentType("energy",
        builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    private ComponentReg()
    {
    }

    public static void register(IEventBus modBus)
    {
        COMPONENTS.register(modBus);
    }
}
