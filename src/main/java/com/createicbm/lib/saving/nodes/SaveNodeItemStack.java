package com.createicbm.lib.saving.nodes;

import com.createicbm.lib.saving.NbtSaveNode;
import com.createicbm.lib.RegistryHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeItemStack<E> extends NbtSaveNode<E, CompoundTag>
{
    public SaveNodeItemStack(final String name, Function<E, ItemStack> getter, BiConsumer<E, ItemStack> setter) {
        super(name,
            (obj) -> {
                final ItemStack itemStack = getter.apply(obj);
                if (itemStack != null && !itemStack.isEmpty())
                {
                    return (CompoundTag) itemStack.save(RegistryHelper.access());
                }
                return null;
            },
            (obj, data) -> {
                setter.accept(obj, ItemStack.parseOptional(RegistryHelper.access(), data));
            }
        );
    }
}
