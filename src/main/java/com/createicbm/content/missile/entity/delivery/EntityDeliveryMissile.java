package com.createicbm.content.missile.entity.delivery;

import com.createicbm.content.blocks.mail.BlockMail;
import com.createicbm.content.blocks.mail.LauncherOwner;
import com.createicbm.content.blocks.mail.MailDelivery;
import com.createicbm.content.missile.logic.source.MissileSender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Missile carrying one stack of items. On impact it puts the stack into the container it hit (chest, barrel,
 * Create vault or anything with an item handler) and packs what doesn't fit into a {@link BlockMail} parcel where it
 * landed, labelled with the sender. The missile is used up and does no damage. If it is shot down, removed by a command or times out,
 * the parcel lands on the ground below.
 */
public class EntityDeliveryMissile extends EntityMissile<EntityDeliveryMissile>
{
    private ItemStack renderStackCache;

    @Getter
    private final ItemStackHandler cargo = new ItemStackHandler(1);

    /** Set once the cargo went into a Mail block, not saved since the missile is gone right after */
    private boolean leftMail = false;

    /** Player who sent the missile, worked out once in flight while the launcher is still loaded */
    @Nullable
    private LauncherOwner sender;
    private boolean senderResolved = false;

    public EntityDeliveryMissile(EntityType<? extends EntityDeliveryMissile> type, Level world)
    {
        super(type, world);
        this.initHealth(ConfigMissile.TIER_2_HEALTH);
        // Same as explosive missiles, the default of a minute ended long deliveries in mid air
        this.inAirKillTime = 144000 /* 2 hours */;
    }

    public EntityDeliveryMissile(Level world)
    {
        this(EntityReg.MISSILE_DELIVERY.get(), world);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (!senderResolved && !level().isClientSide() && getMissileCapability().getMissileSource() != null)
        {
            resolveSender();
        }
    }

    private void resolveSender()
    {
        senderResolved = true;
        // The capability works the sender out at launch and saves it, the same name shows on web maps
        sender = getMissileCapability().getSender();
        if (sender == null)
        {
            sender = MissileSender.find(getMissileCapability().getMissileSource());
        }
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
        if (!level().isClientSide())
        {
            packCargo(BlockPos.containing(position()), position());
        }
        super.onDestroyedBy(source, damage);
    }

    /**
     * Any other end of the flight (timing out, removed by a command) still leaves the cargo in a parcel. Impacts
     * and hits have already emptied it, unloading and changing dimension keep it with the missile.
     */
    @Override
    public void remove(RemovalReason reason)
    {
        if (!level().isClientSide() && !hasLeftForSimulation()
            && (reason == RemovalReason.KILLED || reason == RemovalReason.DISCARDED))
        {
            packCargo(BlockPos.containing(position()), position());
        }
        super.remove(reason);
    }

    /**
     * @return true if the cargo was left in a Mail block, false if it all went into a container or was dropped
     */
    public boolean hasLeftMail()
    {
        return leftMail;
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
        // In front of the block face it hit, or where it hit a mob
        final BlockPos landing = hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit
            ? blockHit.getBlockPos().relative(blockHit.getDirection())
            : BlockPos.containing(at);
        packCargo(landing, at);

        if (level() instanceof ServerLevel server)
        {
            server.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.2, at.z, 12, 0.25, 0.25, 0.25, 0.02);
        }
        level().playSound(null, at.x, at.y, at.z, SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    /**
     * Leaves the cargo in a mail parcel near the position, falling back to dropping it if there is no room at all
     */
    private void packCargo(BlockPos landing, Vec3 dropAt)
    {
        final ItemStack stack = cargo.getStackInSlot(0);
        if (stack.isEmpty())
        {
            return;
        }
        if (!senderResolved)
        {
            resolveSender();
        }
        final Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(level().random);
        if (MailDelivery.deliver(level(), landing, facing, stack, sender))
        {
            cargo.setStackInSlot(0, ItemStack.EMPTY);
            leftMail = true;
        }
        else
        {
            dropCargo(dropAt);
        }
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
        if (nbt.contains("sender_name"))
        {
            senderResolved = true;
            sender = new LauncherOwner(nbt.hasUUID("sender_id") ? nbt.getUUID("sender_id") : new UUID(0, 0), nbt.getString("sender_name"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        SAVE_LOGIC.save(this, nbt);
        if (sender != null)
        {
            nbt.putString("sender_name", sender.name());
            nbt.putUUID("sender_id", sender.id());
        }
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
