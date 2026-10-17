package com.createicbm.content.items;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.reg.IExplosiveData;
import com.createicbm.api.reg.content.IExplosiveContentRegistry;
import com.createicbm.content.reg.ComponentReg;
import com.createicbm.lib.explosive.ExplosiveStacks;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public final class ExplosiveItemHelper
{
    private ExplosiveItemHelper()
    {
    }

    /**
     * Explosive of a stack.
     *
     * @return explosive data, or null if the registry isn't ready or the id is unknown
     */
    @Nullable
    public static IExplosiveData getExplosiveData(ItemStack stack)
    {
        if (CreateICBMAPI.EXPLOSIVE_REGISTRY == null || stack.isEmpty())
        {
            return null;
        }
        final ResourceLocation name = ExplosiveStacks.getExplosiveName(stack);
        if (name != null)
        {
            return CreateICBMAPI.EXPLOSIVE_REGISTRY.getExplosiveData(name, true);
        }
        return CreateICBMAPI.EXPLOSIVE_REGISTRY.getExplosiveData(0);
    }

    /** @return translation key prefix + "." + explosive registry name, or the prefix alone */
    public static String getVariantKey(String prefix, ItemStack stack)
    {
        final IExplosiveData data = getExplosiveData(stack);
        if (data != null)
        {
            return prefix + "." + data.getRegistryKey();
        }
        return prefix;
    }

    /** Creates a stack of the item holding the explosive */
    public static ItemStack create(Item item, IExplosiveData data)
    {
        return ExplosiveStacks.create(item, data);
    }

    public static ItemStack create(Item item, int explosiveId)
    {
        final IExplosiveData data = CreateICBMAPI.EXPLOSIVE_REGISTRY != null ? CreateICBMAPI.EXPLOSIVE_REGISTRY.getExplosiveData(explosiveId) : null;
        if (data != null)
        {
            return create(item, data);
        }
        return new ItemStack(item);
    }

    public static void addSubItems(Item item, @Nullable IExplosiveContentRegistry contentRegistry, NonNullList<ItemStack> items)
    {
        if (contentRegistry == null || contentRegistry.getExplosivesIDs() == null)
        {
            return;
        }
        contentRegistry.getExplosivesIDs().stream().sorted().forEach(id -> {
            final ItemStack stack = create(item, id);
            if (stack.has(ComponentReg.EXPLOSIVE.get()))
            {
                items.add(stack);
            }
        });
    }
}
