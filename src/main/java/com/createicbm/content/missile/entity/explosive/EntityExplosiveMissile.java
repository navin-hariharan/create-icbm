package com.createicbm.content.missile.entity.explosive;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.reg.IExplosiveData;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.missile.logic.TargetRangeDet;
import com.createicbm.content.missile.logic.source.ActionSource;
import com.createicbm.content.missile.logic.source.cause.EntityCause;
import com.createicbm.content.reg.EntityReg;
import com.createicbm.lib.capability.ex.CapabilityExplosiveEntity;
import com.createicbm.lib.saving.NbtSaveHandler;
import com.createicbm.lib.saving.NbtSaveNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.EntityCapability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Entity version of the missile
 * @deprecated replacing with {@link EntityMissileActionable}
 */
public class EntityExplosiveMissile extends EntityMissile<EntityExplosiveMissile>
{
    /** Targeting range handler and settings for triggering explosive before impact */
    public final TargetRangeDet targetRangeDet = new TargetRangeDet(this);

    /** Explosive data and settings */
    public final CapabilityExplosiveEntity explosive = new CapabilityExplosiveEntity(this);

    public EntityExplosiveMissile(EntityType<? extends EntityExplosiveMissile> type, Level w)
    {
        super(type, w);
        this.inAirKillTime = 144000 /* 2 hours */;
        // PERF: frustum culled like the other missiles (getBoundingBoxForCulling covers the model), was always rendered

        // Init health as explosive field is not set at time of health registration
        setHealth(getMaxHealth());
    }

    public EntityExplosiveMissile(Level w)
    {
        this(EntityReg.MISSILE_EXPLOSIVE.get(), w);
    }

    @Override
    public boolean fireImmune()
    {
        return true;
    }

    @Override
    public float getMaxHealth()
    {
        if(explosive != null && explosive.getExplosiveData() != null ) {
            switch (explosive.getExplosiveData().getTier()) {
                case TWO:
                    return ConfigMissile.TIER_2_HEALTH;
                case THREE:
                    return ConfigMissile.TIER_3_HEALTH;
                case FOUR:
                    return ConfigMissile.TIER_4_HEALTH;
            }
        }
        return ConfigMissile.TIER_1_HEALTH;
    }

    @Override
    protected void onDestroyedBy(DamageSource source, float damage)
    {
       super.onDestroyedBy(source, damage);
       // TODO add config
       // TODO add random chance modifier
       if(source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_FIRE)) {
           explosive.doExplosion(getX(), getY(), getZ(), new ActionSource(level(), new Vec3(getX(), getY(), getZ()), new EntityCause(this)));
       }
    }

    @Nullable
    @Override
    public <T> T getEntityCapability(EntityCapability<T, Void> capability)
    {
        if(capability == CreateICBMAPI.EXPLOSIVE_ENTITY_CAPABILITY) {
            return (T) explosive;
        }
        return super.getEntityCapability(capability);
    }

    @Override
    protected Component getTypeName()
    {
        final IExplosiveData data = explosive.getExplosiveData();
        if (data != null)
        {
            return Component.translatable("missile." + data.getRegistryKey().toString() + ".name");
        }
        return Component.translatable("missile.create_icbm:generic.name");
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf additionalMissileData)
    {
        final CompoundTag saveData = SAVE_LOGIC.save(this, new CompoundTag());
        additionalMissileData.writeNbt(saveData);
        super.writeSpawnData(additionalMissileData);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalMissileData)
    {
        final CompoundTag saveData = additionalMissileData.readNbt();
        if (saveData != null)
        {
            SAVE_LOGIC.load(this, saveData);
        }
        super.readSpawnData(additionalMissileData);
    }

    @Override
    public void tick()
    {
        targetRangeDet.update();
        super.tick();
    }

    @Override
    public InteractionResult interact(@Nonnull Player player, @Nonnull InteractionHand hand)
    {
        //Allow missile to override interaction
        if (CreateICBMAPI.EX_MISSILE_REGISTRY.onInteraction(this, player, hand))
        {
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    protected void actionOnImpact(HitResult impactLocation) {
        super.actionOnImpact(impactLocation);
        explosive.doExplosion(impactLocation.getLocation().x, impactLocation.getLocation().y, impactLocation.getLocation().z, new ActionSource(level(), new Vec3(getX(), getY(), getZ()), new EntityCause(this)));
    }

    @Override
    public ItemStack toStack() {
        return explosive.toStack();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        SAVE_LOGIC.load(this, nbt);
        super.readAdditionalSaveData(nbt);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<EntityExplosiveMissile> SAVE_LOGIC = new NbtSaveHandler<EntityExplosiveMissile>()
        .mainRoot()
        /* */.node(new NbtSaveNode<EntityExplosiveMissile, CompoundTag>("explosive",
            (missile) -> missile.explosive.serializeNBT(),
            (missile, data) -> missile.explosive.deserializeNBT(data))
        )
        .base();
}
