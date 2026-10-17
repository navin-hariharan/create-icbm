package com.createicbm.lib.explosive.reg;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.CreateICBMHelpers;
import com.createicbm.api.data.EntityTickFunction;
import com.createicbm.api.data.WorldEntityIntSupplier;
import com.createicbm.api.reg.IExplosiveData;
import com.createicbm.api.reg.content.IExMinecartRegistry;
import com.createicbm.content.reg.ItemReg;
import net.minecraft.world.entity.Entity;
import com.createicbm.lib.explosive.ExplosiveStacks;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class ExMinecartContentReg extends ExplosiveContentRegistry implements IExMinecartRegistry
{
    private final HashMap<ResourceLocation, WorldEntityIntSupplier> fuseSetSupplierMap = new HashMap();
    private final HashMap<ResourceLocation, EntityTickFunction> fuseTickCallbackMap = new HashMap();

    private final Int2ObjectOpenHashMap<WorldEntityIntSupplier> fuseSetSupplier = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<EntityTickFunction> fuseTickCallback = new Int2ObjectOpenHashMap<>();

    public ExMinecartContentReg()
    {
        super(CreateICBMAPI.EX_MINECART);
    }

    @Override
    public ItemStack getDeviceStack(ResourceLocation regName)
    {
        return ExplosiveStacks.create(ItemReg.itemBombCart.get(), CreateICBMHelpers.getExplosive(regName, false));
    }

    @Override
    public void lockRegistry()
    {
        if(!isLocked())
        {
            super.lockRegistry();
            fuseSetSupplierMap.forEach((regName, func) -> {
                final IExplosiveData data = CreateICBMAPI.EXPLOSIVE_REGISTRY.getExplosiveData(regName);
                if (data != null)
                {
                    fuseSetSupplier.put(data.getRegistryID(), func);
                }
            });
            fuseTickCallbackMap.forEach((regName, func) -> {
                final IExplosiveData data = CreateICBMAPI.EXPLOSIVE_REGISTRY.getExplosiveData(regName);
                if (data != null)
                {
                    fuseTickCallback.put(data.getRegistryID(), func);
                }
            });
        }
        else
            throw new RuntimeException(this + ": Registry was locked twice!");
    }

    @Override
    public void setFuseSupplier(ResourceLocation exName, WorldEntityIntSupplier fuseTimer)
    {
        fuseSetSupplierMap.put(exName, fuseTimer);
    }

    @Override
    public void setFuseTickListener(ResourceLocation exName, EntityTickFunction function)
    {
        fuseTickCallbackMap.put(exName, function);
    }

    @Override
    public void tickFuse(Entity entity, IExplosiveData data, int ticksExisted)
    {
        final EntityTickFunction function = fuseTickCallback.get(data != null ? data.getRegistryID() : 0);
        if (function != null)
        {
            function.onTick(entity, ticksExisted);
        }
    }

    @Override
    public int getFuseTime(Entity entity, IExplosiveData data)
    {
        final WorldEntityIntSupplier function = fuseSetSupplier.get(data != null ? data.getRegistryID() : 0);
        if (function != null)
        {
            return function.get(entity);
        }
        return 100;
    }
}
