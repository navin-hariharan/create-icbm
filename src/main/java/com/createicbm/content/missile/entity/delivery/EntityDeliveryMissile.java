package com.createicbm.content.missile.entity.delivery;

import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.reg.EntityReg;
import com.createicbm.content.reg.ItemReg;
import com.createicbm.lib.saving.NbtSaveHandler;
import lombok.Getter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

/**
 * Missile carrying one stack of items. On impact it puts the stack into the container it hit (chest, barrel,
 * Create vault or anything with an item handler) and drops what doesn't fit where it landed. The missile is used
 * up and does no damage. If it is shot down the cargo falls out where it was.
 */
public class EntityDeliveryMissile extends EntityMissile<EntityDeliveryMissile>
{
    private ItemStack renderStackCache;

    @Getter
    private final ItemStackHandler cargo = new ItemStackHandler(1);

    public EntityDeliveryMissile(EntityType<? extends EntityDeliveryMissile> type, Level world)
    {
        super(type, world);
        this.initHealth(ConfigMissile.TIER_2_HEALTH);
    }

    public EntityDeliveryMissile(Level world)
    {
        this(EntityReg.MISSILE_DELIVERY.get(), world);
    }

    @Override
    protected void actionOnImpact(HitResult hit)
    {
        if (!level().isClientSide())
        {
            deliver(hit);
        }
        super.actionOnImpact(hit);
    }

    /** Lands on whoever it hits instead of hurting them */
    @Override
    protected void onImpactEntity(@Nonnull Entity entityHit, float velocity, @Nonnull HitResult hit)
    {
        if (!level().isClientSide())
        {
            onImpact(hit);
        }
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, HitResult hit)
    {
        return 0;
    }

    @Override
    protected void onDestroyedBy(DamageSource source, float damage)
    {
        dropCargo(position());
        super.onDestroyedBy(source, damage);
    }

    private void deliver(HitResult hit)
    {
        ItemStack rest = cargo.getStackInSlot(0);
        if (rest.isEmpty())
        {
            return;
        }

        // Straight into the container it hit, from the side it came in
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit)
        {
            final IItemHandler handler = level().getCapability(Capabilities.ItemHandler.BLOCK, blockHit.getBlockPos(), blockHit.getDirection());
            if (handler != null)
            {
                rest = ItemHandlerHelper.insertItemStacked(handler, rest, false);
                cargo.setStackInSlot(0, rest);
            }
        }

        final Vec3 at = hit.getLocation();
        dropCargo(at);

        if (level() instanceof ServerLevel server)
        {
            server.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.2, at.z, 12, 0.25, 0.25, 0.25, 0.02);
        }
        level().playSound(null, at.x, at.y, at.z, SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private void dropCargo(Vec3 at)
    {
        final ItemStack stack = cargo.getStackInSlot(0);
        if (!level().isClientSide() && !stack.isEmpty())
        {
            cargo.setStackInSlot(0, ItemStack.EMPTY);
            final ItemEntity item = new ItemEntity(level(), at.x, at.y + 0.25, at.z, stack, 0, 0.1, 0);
            item.setDefaultPickUpDelay();
            level().addFreshEntity(item);
        }
    }

    @Override
    public ItemStack toStack()
    {
        if (level().isClientSide())
        {
            if (renderStackCache == null)
            {
                renderStackCache = genItem();
            }
            return renderStackCache;
        }
        return genItem();
    }

    private ItemStack genItem()
    {
        final ItemStack stack = new ItemStack(ItemReg.itemDeliveryMissile.get());
        ItemDeliveryMissile.setCargo(stack, cargo.getStackInSlot(0).copy());
        return stack;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf additionalMissileData)
    {
        super.writeSpawnData(additionalMissileData);
        additionalMissileData.writeNbt(SAVE_LOGIC.save(this, new CompoundTag()));
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalMissileData)
    {
        super.readSpawnData(additionalMissileData);
        final CompoundTag saveData = additionalMissileData.readNbt();
        if (saveData != null)
        {
            SAVE_LOGIC.load(this, saveData);
        }
    }

    private static final NbtSaveHandler<EntityDeliveryMissile> SAVE_LOGIC = new NbtSaveHandler<EntityDeliveryMissile>()
        .mainRoot()
        /* */.nodeINBTSerializable("cargo", EntityDeliveryMissile::getCargo)
        .base();
}
