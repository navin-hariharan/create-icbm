package com.createicbm.prefab.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * Items with several variants (explosive tiers, missiles, bomb carts...) implement this so the creative tab and JEI
 * list every variant. Items that don't implement it show a single default stack.
 */
public interface ISubItemProvider
{
    void getSubItems(NonNullList<ItemStack> items);
}
