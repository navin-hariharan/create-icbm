package com.createicbm.lib.capability.holder;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

/**
 * {@link com.createicbm.content.reg.CapabilityReg} registers a provider for every ICBM block entity type that forwards
 * to this method.
 */
public interface ICapabilityHolderTile
{
    /**
     * @param capability being requested, compare with == against ICBM/NeoForge capability constants
     * @param side side being accessed, may be null for internal access
     * @return capability instance or null if not supported
     */
    @Nullable
    <T> T getCapability(BlockCapability<T, Direction> capability, @Nullable Direction side);
}
