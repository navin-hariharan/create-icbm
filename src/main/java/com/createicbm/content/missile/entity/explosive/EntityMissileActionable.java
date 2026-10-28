package com.createicbm.content.missile.entity.explosive;

import com.createicbm.api.actions.IActionData;
import com.createicbm.api.actions.data.ActionFields;
import com.createicbm.api.actions.status.ActionStatusTypes;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.missile.logic.source.cause.EntityCause;
import com.createicbm.lib.actions.PotentialAction;
import com.createicbm.lib.actions.fields.ActionFieldProvider;
import com.createicbm.lib.saving.NbtSaveHandler;
import com.createicbm.content.reg.EntityReg;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;


/**
 * Missile with generic action handling
 */
public class EntityMissileActionable extends EntityMissile<EntityMissileActionable>
{
    /** Explosive data and settings */
    @Getter
    private final PotentialAction mainAction = new PotentialAction()
        .withProvider(new ActionFieldProvider()
            .field(ActionFields.IMPACTED, () -> hasImpacted)
            .field(ActionFields.HOST_ENTITY, () -> this)
            .field(ActionFields.HOST_POSITION, this::position) //TODO may need to cache once conditionals get more common
            .field(ActionFields.TARGET_POSITION, () -> this.getMissileCapability().getTargetData() != null ? this.getMissileCapability().getTargetData().getPosition(): null)
        );

    @Getter @Setter @Accessors(chain = true)
    private ItemStack originalStack = ItemStack.EMPTY;

    public EntityMissileActionable(EntityType<? extends EntityMissileActionable> type, Level w)
    {
        super(type, w);
        this.inAirKillTime = 144000 /* 2 hours */;
        // PERF: frustum culled like the other missiles (getBoundingBoxForCulling covers the model), was always rendered
    }

    public EntityMissileActionable(Level w)
    {
        this(EntityReg.MISSILE_GENERIC.get(), w);
    }

    @Override
    public boolean fireImmune()
    {
        return true;
    }

    public EntityMissileActionable setActionData(IActionData actionData) {
        mainAction.setActionData(actionData);
        return this;
    }

    @Override
    protected void onDestroyedBy(DamageSource source, float damage)
    {
       // TODO add config
       // TODO add random chance modifier
       if(source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_FIRE)) {
           final IActionStatus status = this.mainAction.doAction(level(), getX(), getY(), getZ(), new EntityCause(this)); // Add damage source cause
           if(!status.isType(ActionStatusTypes.BLOCKING)) {
               super.onDestroyedBy(source, damage);
           }
       }
    }

    @Override
    protected Component getTypeName()
    {
        final IActionData data = mainAction.getActionData();
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
        super.tick();
        this.mainAction.update(tickCount, !this.level().isClientSide());

        // Ticking trigger
        final IActionStatus status = mainAction.doAction(level(), getX(), getY(), getZ(), new EntityCause(this));
        if(!status.isType(ActionStatusTypes.BLOCKING)) {
            this.destroy();
        }
    }

    @Override
    protected void actionOnImpact(HitResult impactLocation) {
        // TODO add impact cause
        final IActionStatus status = mainAction.doAction(level(), impactLocation.getLocation().x, impactLocation.getLocation().y, impactLocation.getLocation().z, new EntityCause(this));
        if(!status.isType(ActionStatusTypes.BLOCKING)) {
            super.actionOnImpact(impactLocation);
        }
    }

    @Override
    public ItemStack toStack() {
        return originalStack;
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

    private static final NbtSaveHandler<EntityMissileActionable> SAVE_LOGIC = new NbtSaveHandler<EntityMissileActionable>()
        .mainRoot()
        /* */.nodeINBTSerializable("potential_action", EntityMissileActionable::getMainAction)
        /* */.nodeItemStack("original_stack", EntityMissileActionable::getOriginalStack, EntityMissileActionable::setOriginalStack)
        .base();
}
