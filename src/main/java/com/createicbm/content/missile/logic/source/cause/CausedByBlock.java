package com.createicbm.content.missile.logic.source.cause;

import com.createicbm.ICBMConstants;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.actions.cause.IActionCause;
import com.createicbm.api.actions.cause.ICausedByBlock;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import com.createicbm.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

/**
 * General purpose block cause
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CausedByBlock extends ActionCause implements ICausedByBlock {

    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "block");

    private Level world;
    private BlockPos blockPos;
    private BlockState blockState;

    private ResourceKey<Level> worldId;

    public CausedByBlock(Level world, BlockPos pos, BlockState state) {
        this.world = world;
        this.worldId = world.dimension();
        this.blockPos = pos;
        this.blockState = state;
    }

    public Level getWorld() {
        if(world == null && worldId != null) {
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                world = server.getLevel(worldId);
            }
        }
        return world;
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

    private static final NbtSaveHandler<CausedByBlock> SAVE_LOGIC = new NbtSaveHandler<CausedByBlock>()
        .mainRoot()
        /* */.nodeResourceLocation("level",
            (c) -> c.getWorldId() != null ? c.getWorldId().location() : null,
            (c, loc) -> c.setWorldId(ResourceKey.create(Registries.DIMENSION, loc)))
        /* */.nodeBlockPos("pos", CausedByBlock::getBlockPos, CausedByBlock::setBlockPos)
        /* */.nodeBlockState("state", CausedByBlock::getBlockState, CausedByBlock::setBlockState)
        .base();
}
