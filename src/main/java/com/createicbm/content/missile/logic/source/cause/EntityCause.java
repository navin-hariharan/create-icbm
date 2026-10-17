package com.createicbm.content.missile.logic.source.cause;

import com.createicbm.ICBMConstants;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.actions.cause.IActionCause;
import com.createicbm.api.actions.cause.ICausedByEntity;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import com.createicbm.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * General purpose entity cause
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EntityCause extends ActionCause implements ICausedByEntity {

    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "entity");

    private String name;
    private UUID id;
    private boolean isPlayer;

    // runtime cache vars
    private Entity entity;

    public EntityCause(Entity source) {
        entity = source;
        name = source.getName().getString();
        id = source.getUUID();
        isPlayer = source instanceof Player;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return REG_NAME;
    }

    @Nonnull
    @Override
    public IBuilderRegistry<IActionCause> getRegistry() {
        return CreateICBMAPI.ACTION_CAUSE_REGISTRY;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this, super.serializeNBT(provider));
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        super.deserializeNBT(provider, nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<EntityCause> SAVE_LOGIC = new NbtSaveHandler<EntityCause>()
        .mainRoot()
        /* */.nodeString("name", EntityCause::getName, EntityCause::setName)
        /* */.nodeUUID("uuid", EntityCause::getId, EntityCause::setId)
        /* */.nodeBoolean("player", EntityCause::isPlayer, EntityCause::setPlayer)
        .base();
}
