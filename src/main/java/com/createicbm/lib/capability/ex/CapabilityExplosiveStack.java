package com.createicbm.lib.capability.ex;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.caps.IExplosive;
import com.createicbm.api.explosion.IBlast;
import com.createicbm.api.reg.IExplosiveCustomization;
import com.createicbm.api.reg.IExplosiveData;
import com.createicbm.lib.RegistryHelper;
import com.createicbm.lib.StackNbt;
import com.createicbm.content.items.ExplosiveItemHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Used by any item that has an explosive capability
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class CapabilityExplosiveStack implements IExplosive, INBTSerializable<CompoundTag>
{
    public static final String STACK_KEY = "explosive";

    @Nullable
    private final ItemStack stack;
    private final List<IExplosiveCustomization> customizationList = new ArrayList();

    public CapabilityExplosiveStack(@Nullable ItemStack stack)
    {
        this.stack = stack;
        if (stack != null && StackNbt.has(stack, STACK_KEY))
        {
            deserializeNBT(RegistryHelper.access(), StackNbt.getCompound(stack, STACK_KEY));
        }
    }

    @Nullable
    @Override
    public IExplosiveData getExplosiveData()
    {
        if (stack == null)
        {
            return null;
        }
        return ExplosiveItemHelper.getExplosiveData(stack);
    }

    @Override
    public void applyCustomizations(IBlast blast) {
        customizationList.forEach(c -> c.apply(getExplosiveData(), blast));
    }

    @Override
    public void addCustomization(IExplosiveCustomization customization) {
        customizationList.add(customization);
        if (stack != null && !stack.isEmpty())
        {
            StackNbt.putCompound(stack, STACK_KEY, serializeNBT(RegistryHelper.access()));
        }
    }

    @Override
    public void collectInformation(Consumer<String> collector){
        //TODO debug explosive id and name if F3+something keybind is enabled
        customizationList.forEach(c -> c.collectCustomizationInformation(collector));
    }

    @Nullable
    @Override
    public ItemStack toStack()
    {
        if (stack == null)
        {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(1);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider)
    {
        final CompoundTag save = new CompoundTag();
        if (!customizationList.isEmpty())
        {
            save.put("customizations", CreateICBMAPI.EXPLOSIVE_CUSTOMIZATION_REGISTRY.save(customizationList));
        }
        return save;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt)
    {
        if(nbt.contains("customizations")) {
            CreateICBMAPI.EXPLOSIVE_CUSTOMIZATION_REGISTRY.load(nbt.getList("customizations", Tag.TAG_COMPOUND), customizationList);
        }
    }
}
