package com.createicbm.lib.capability.ex;

import com.createicbm.CreateICBM;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.actions.cause.IActionSource;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.caps.IExplosive;
import com.createicbm.api.explosion.IBlast;
import com.createicbm.api.refs.ICBMExplosives;
import com.createicbm.api.reg.IExplosiveCustomization;
import com.createicbm.api.reg.IExplosiveData;
import com.createicbm.content.blast.BlastQueue;
import com.createicbm.lib.RegistryHelper;
import com.createicbm.lib.actions.status.ActionResponses;
import com.createicbm.lib.explosive.ExplosiveStacks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class CapabilityExplosiveEntity implements IExplosive
{
    public final Entity entity;
    private ItemStack stack = ItemStack.EMPTY;

    private boolean isExploding = false;

    public CapabilityExplosiveEntity(@Nonnull Entity entity)
    {
        this.entity = entity;
    }

    public CompoundTag serializeNBT()
    {
        return (CompoundTag) toStack().saveOptional(RegistryHelper.access()); // saveOptional is declared to return Tag
    }

    public void deserializeNBT(@Nonnull CompoundTag nbt)
    {
        if (nbt.isEmpty())
        {
            stack = ItemStack.EMPTY;
        }
        else
        {
            stack = ItemStack.parseOptional(RegistryHelper.access(), nbt);
        }
    }

    public IActionStatus doExplosion(double x, double y, double z, IActionSource source)
    {
        if (this.isExploding) {
            return ActionResponses.COMPLETED;
        }

        //Make sure to note we are currently exploding
        this.isExploding = true;

        if (!this.entity.level().isClientSide())
        {
            final Level level = this.entity.level();
            final IExplosiveData data = this.getExplosiveData();

            // PERF: when many go off in the same tick (missile salvo) the rest wait for the next ticks, see BlastQueue
            if (BlastQueue.shouldQueue(level))
            {
                BlastQueue.queue(level, () -> data.create(level, x, y, z, source, null).doAction());
                return ActionResponses.COMPLETED;
            }
            return BlastQueue.runTimed(level, () -> data.create(level, x, y, z, source, null).doAction());
        }
        return ActionResponses.COMPLETED;
    }

    @Nonnull
    @Override
    public IExplosiveData getExplosiveData()
    {
        final IExplosive explosive = toStack().getCapability(CreateICBMAPI.EXPLOSIVE_ITEM_CAPABILITY);
        if (explosive != null && explosive.getExplosiveData() != null)
        {
            return explosive.getExplosiveData();
        }
        return ICBMExplosives.CONDENSED;
    }

    @Override
    public void applyCustomizations(IBlast blast) {
        final IExplosive explosive = toStack().getCapability(CreateICBMAPI.EXPLOSIVE_ITEM_CAPABILITY);
        if (explosive != null)
        {
            explosive.applyCustomizations(blast);
        }
    }

    @Override
    public void addCustomization(IExplosiveCustomization customization) {
        final IExplosive explosive = toStack().isEmpty() ? null : stack.getCapability(CreateICBMAPI.EXPLOSIVE_ITEM_CAPABILITY);
        if (explosive != null)
        {
            explosive.addCustomization(customization);
        }
    }

    @Nonnull
    @Override
    public ItemStack toStack()
    {
        if (stack == null)
        {
            stack = ItemStack.EMPTY;
        }
        return stack;
    }

    @Override
    public void onDefuse()
    {
        entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), toStack().copy()));
        entity.discard();
    }

    public void setStack(@Nonnull ItemStack stack)
    {
        if (stack.getCapability(CreateICBMAPI.EXPLOSIVE_ITEM_CAPABILITY) == null)
        {
            CreateICBM.logger().error("CapabilityExplosive[" + entity + "] Was set with a stack that is not an explosive [" + stack + "]");
        }
        this.stack = stack.copyWithCount(1);
    }

    @Override
    public int hashCode() {
        return 31 * stack.getItem().hashCode() + Objects.hashCode(ExplosiveStacks.getExplosiveName(stack));
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) || other instanceof IExplosive && sameItemAndExplosive(((IExplosive) other).toStack(), toStack());
    }

    private static boolean sameItemAndExplosive(ItemStack a, ItemStack b) {
        if (a == null || b == null) {
            return a == b;
        }
        return ItemStack.isSameItem(a, b) && Objects.equals(ExplosiveStacks.getExplosiveName(a), ExplosiveStacks.getExplosiveName(b));
    }

    @Override
    public String toString() {
        return String.format("CapabilityExplosiveEntity[%s]@%s", toStack(), super.hashCode());
    }
}
