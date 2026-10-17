package com.createicbm.lib.explosive;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.reg.IExplosiveData;
import com.createicbm.content.reg.ComponentReg;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;

/**
 * Helpers for explosive item stacks.
 */
public final class ExplosiveStacks
{
    private ExplosiveStacks()
    {
    }

    /**
     * Creates a stack of size 1 for the item with the explosive set
     *
     * @param item - item or block to create the stack from
     * @param data - explosive, null leaves the component unset
     * @return new stack
     */
    public static ItemStack create(ItemLike item, @Nullable IExplosiveData data)
    {
        return create(item, data, 1);
    }

    /**
     * Creates a stack for the item with the explosive set
     *
     * @param item  - item or block to create the stack from
     * @param data  - explosive, null leaves the component unset
     * @param count - stack size
     * @return new stack
     */
    public static ItemStack create(ItemLike item, @Nullable IExplosiveData data, int count)
    {
        final ItemStack stack = new ItemStack(item, count);
        set(stack, data);
        return stack;
    }

    /**
     * Sets the explosive of an existing stack
     *
     * @param stack - stack to edit
     * @param data  - explosive, null removes the component
     */
    public static void set(ItemStack stack, @Nullable IExplosiveData data)
    {
        if (data != null && data.getRegistryKey() != null)
        {
            stack.set(ComponentReg.EXPLOSIVE.get(), data.getRegistryKey());
        }
        else
        {
            stack.remove(ComponentReg.EXPLOSIVE.get());
        }
    }

    /**
     * @return registry name stored on the stack, or null if none
     */
    @Nullable
    public static ResourceLocation getExplosiveName(ItemStack stack)
    {
        if (stack == null || stack.isEmpty())
        {
            return null;
        }
        return stack.get(ComponentReg.EXPLOSIVE.get());
    }

    /**
     * Gets the explosive stored on the stack
     *
     * @param stack - stack to check
     * @return explosive data, or null if the stack has none or the name isn't registered
     */
    @Nullable
    public static IExplosiveData getExplosive(ItemStack stack)
    {
        final ResourceLocation name = getExplosiveName(stack);
        if (name == null || CreateICBMAPI.EXPLOSIVE_REGISTRY == null)
        {
            return null;
        }
        return CreateICBMAPI.EXPLOSIVE_REGISTRY.getExplosiveData(name, true);
    }
}
