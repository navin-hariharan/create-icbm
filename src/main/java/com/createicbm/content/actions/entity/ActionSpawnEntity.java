package com.createicbm.content.actions.entity;

import com.google.common.collect.ImmutableList;
import com.createicbm.api.actions.IActionData;
import com.createicbm.api.actions.cause.IActionSource;
import com.createicbm.api.actions.data.ActionField;
import com.createicbm.api.actions.data.ActionFields;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.lib.actions.ActionBase;
import com.createicbm.lib.actions.status.ActionResponses;
import com.createicbm.lib.actions.status.MissingFieldStatus;
import com.createicbm.lib.projectile.EntityProjectile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ActionSpawnEntity extends ActionBase {

    public static final List<ActionField> FIELDS = ImmutableList.of(
        ActionFields.YAW, ActionFields.PITCH, ActionFields.MOTION_VECTOR,
        ActionFields.ENTITY_REG_NAME, ActionFields.ENTITY_DATA);

    private ResourceLocation entityID;
    private CompoundTag entityData;
    private Vec3 motion;
    private Float yaw;
    private Float pitch;

    public ActionSpawnEntity(Level world, Vec3 position, IActionSource source, IActionData actionData) {
        super(world, position, source, actionData);
    }

    @Nonnull
    @Override
    public IActionStatus doAction() {
        if (entityID == null) {
            return new MissingFieldStatus().setSource("ActionSpawnEntity").setField("entityId");
        }
        final Entity entity = BuiltInRegistries.ENTITY_TYPE.getOptional(entityID)
            .map(type -> (Entity) type.create(getWorld()))
            .orElse(null);
        if (entity == null) {
            return new MissingFieldStatus().setSource("ActionSpawnEntity#doAction()").setField("entityInstance");
        }

        entity.setPos(getPosition().x, getPosition().y, getPosition().z);
        if (motion != null) {
            if(entity instanceof EntityProjectile) {
                ((EntityProjectile<?>) entity).setMotionVector(motion.x, motion.y, motion.z);
            }
            else {
                entity.setDeltaMovement(motion.x, motion.y, motion.z);
            }
        }

        // Apply yaw
        if (yaw != null) {
            entity.setYRot(yaw);
            entity.yRotO = yaw;
        } else {
            final float randomYaw = Mth.wrapDegrees(getWorld().random.nextFloat() * 360.0F);
            entity.setYRot(randomYaw);
            entity.yRotO = randomYaw;
        }
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setYHeadRot(entity.getYRot());
            ((LivingEntity) entity).setYBodyRot(entity.getYRot());
        }

        // Apply pitch
        if (pitch != null) {
            entity.setXRot(pitch);
            entity.xRotO = pitch;
        } else {
            final float randomYaw = Mth.wrapDegrees(getWorld().random.nextFloat() * 360.0F);
            entity.setYRot(randomYaw);
            entity.yRotO = randomYaw;
        }

        // Handle data loading
        if (entityData != null) {
            final UUID uuid = entity.getUUID();

            CompoundTag mergedData = entity.saveWithoutId(new CompoundTag());
            mergedData.merge(entityData);
            entity.load(mergedData);

            entity.setUUID(uuid);

        }

        if (!getWorld().addFreshEntity(entity)) {
            return ActionResponses.ENTITY_SPAWN_FAILED;
        }
        return ActionResponses.COMPLETED;
    }

    @Override
    public <VALUE, TAG extends Tag> void setValue(ActionField<VALUE, TAG> key, VALUE value) {
        if (key == ActionFields.YAW) {
            this.yaw = ActionFields.YAW.cast(value);
        }
        if (key == ActionFields.PITCH) {
            this.pitch = ActionFields.PITCH.cast(value);
        }
        if (key == ActionFields.MOTION_VECTOR) {
            this.motion = (Vec3) value;
        }
        if (key == ActionFields.ENTITY_REG_NAME) {
            this.entityID = (ResourceLocation) value;
        }
        if (key == ActionFields.ENTITY_DATA) {
            this.entityData = (CompoundTag) value;
        }
    }

    @Override
    public <VALUE, TAG extends Tag> VALUE getValue(ActionField<VALUE, TAG> key) {
        if (key == ActionFields.YAW) {
            return key.cast(yaw);
        }
        if (key == ActionFields.PITCH) {
            return key.cast(pitch);
        }
        if (key == ActionFields.MOTION_VECTOR) {
            return key.cast(motion);
        }
        if (key == ActionFields.ENTITY_REG_NAME) {
            return key.cast(entityID);
        }
        if (key == ActionFields.ENTITY_DATA) {
            return key.cast(entityData);
        }
        return null;
    }

    @Override
    public Collection<ActionField> getFields() {
        return FIELDS;
    }
}
