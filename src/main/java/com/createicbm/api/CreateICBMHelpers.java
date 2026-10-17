package com.createicbm.api;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.createicbm.CreateICBM;
import com.createicbm.api.caps.IExplosive;
import com.createicbm.api.caps.IGPSData;
import com.createicbm.api.launcher.IMissileLauncher;
import com.createicbm.api.missiles.IMissile;
import com.createicbm.api.refs.ICBMExplosives;
import com.createicbm.api.reg.IExplosiveData;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public final class CreateICBMHelpers
{

    /**
     * Called to get explosive
     *
     * @param explosive
     * @return explosive desired, or default TNT
     */
    public static IExplosiveData getExplosive(int explosive, boolean returnNull)
    {
        IExplosiveData data = CreateICBMAPI.EXPLOSIVE_REGISTRY.getExplosiveData(explosive);
        if (data != null)
        {
            return data;
        }
        if(CreateICBM.runningAsDev)
        {
            CreateICBM.logger().error("CreateICBMAPI: Error - Failed to locate explosive for " +
                    "ID[" + explosive + "] this may cause unexpected logic", new RuntimeException());
        }
        return returnNull ? null : ICBMExplosives.CONDENSED;
    }

    /**
     * Called to get explosive
     *
     * @param name - registry name of the explosive
     * @return explosive desired, or default TNT
     */
    public static IExplosiveData getExplosive(String name, boolean returnNull)
    {
        return getExplosive(ResourceLocation.parse(name), returnNull);
    }

    /**
     * Called to get explosive
     *
     * @param name - registry name of the explosive
     * @return explosive desired, or default TNT
     */
    public static IExplosiveData getExplosive(ResourceLocation name, boolean returnNull)
    {
        final IExplosiveData data = CreateICBMAPI.EXPLOSIVE_REGISTRY.getExplosiveData(name);
        if (data != null)
        {
            return data;
        }
        System.out.println("CreateICBMAPI: Error - Failed to locate explosive for Name[" + name + "] this may cause unexpected logic");
        return returnNull ? null : ICBMExplosives.CONDENSED;
    }

    /**
     * Checks if the entity is a missile
     *
     * @param entity
     * @return
     */
    public static boolean isMissile(Entity entity)
    {
        return entity != null && entity.getCapability(CreateICBMAPI.MISSILE_CAPABILITY) != null;
    }

    public static boolean isMissile(ItemStack stack)
    {
        return !stack.isEmpty() && stack.getCapability(CreateICBMAPI.MISSILE_STACK_CAPABILITY) != null;
    }

    /**
     * Helper to get the capability of {@link IMissile} from an entity
     *
     * @param entity to pull from
     * @return capability
     */
    public static IMissile getMissile(Entity entity)
    {
        return entity.getCapability(CreateICBMAPI.MISSILE_CAPABILITY);
    }

    public static boolean isExplosive(Entity entity)
    {
        return entity != null && entity.getCapability(CreateICBMAPI.EXPLOSIVE_ENTITY_CAPABILITY) != null;
    }

    public static IExplosive getExplosive(Entity entity)
    {
        return entity.getCapability(CreateICBMAPI.EXPLOSIVE_ENTITY_CAPABILITY);
    }

    public static boolean isLauncher(BlockEntity tileEntity, Direction side)
    {
        return getLauncher(tileEntity, side) != null;
    }

    public static IMissileLauncher getLauncher(BlockEntity tileEntity, Direction side)
    {
        if (tileEntity == null || tileEntity.getLevel() == null)
        {
            return null;
        }
        return tileEntity.getLevel().getCapability(CreateICBMAPI.MISSILE_LAUNCHER_CAPABILITY, tileEntity.getBlockPos(), tileEntity.getBlockState(), tileEntity, side);
    }

    public static IExplosive getExplosive(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return null;
        }
        return stack.getCapability(CreateICBMAPI.EXPLOSIVE_ITEM_CAPABILITY);
    }

    /**
     * Gets the {@link IGPSData} capability from an {@link ItemStack}
     * @param stack to access
     * @return data if present, or null otherwise
     */
    public static IGPSData getGPSData(ItemStack stack) {
        if(stack.isEmpty()) {
            return null;
        }
        return stack.getCapability(CreateICBMAPI.GPS_CAPABILITY);
    }

    /**
     * Gets the explosive capability of a block entity
     *
     * @param tile to access
     * @param side to access from, may be null
     * @return explosive or null
     */
    public static IExplosive getExplosive(BlockEntity tile, Direction side)
    {
        if (tile == null || tile.getLevel() == null)
        {
            return null;
        }
        return tile.getLevel().getCapability(CreateICBMAPI.EXPLOSIVE_BLOCK_CAPABILITY, tile.getBlockPos(), tile.getBlockState(), tile, side);
    }
}
