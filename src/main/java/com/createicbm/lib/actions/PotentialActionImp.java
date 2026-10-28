package com.createicbm.lib.actions;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.actions.IActionData;
import com.createicbm.api.actions.IPotentialAction;
import com.createicbm.api.actions.cause.IActionCause;
import com.createicbm.api.actions.cause.IActionSource;
import com.createicbm.api.actions.conditions.ICondition;
import com.createicbm.api.actions.conditions.IConditionCause;
import com.createicbm.api.actions.data.IActionFieldProvider;
import com.createicbm.api.actions.status.ActionStatusTypes;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.content.missile.logic.source.ActionSource;
import com.createicbm.content.missile.logic.source.cause.CausedByBlock;
import com.createicbm.lib.actions.status.ActionResponses;
import com.createicbm.lib.saving.NbtSaveHandler;
import com.createicbm.lib.tile.ITick;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Pre-built action for general purpose implementation where all components are known.
 */
public abstract class PotentialActionImp<SELF extends PotentialActionImp<SELF>> implements IPotentialAction, ITick, INBTSerializable<CompoundTag> {

    @Getter @Setter(AccessLevel.PRIVATE)
    private ICondition preCheck;

    @Getter @Setter
    private IActionFieldProvider fieldProvider;

    /** True when {@link #preCheck} was loaded from save and has not been given the field provider yet */
    private boolean preCheckNeedsInit = false;

    public SELF withProvider(IActionFieldProvider provider) {
        this.fieldProvider = provider;
        if(preCheck != null) {
            this.preCheckNeedsInit = false;
            preCheck.init(fieldProvider);
        }
        return (SELF) this;
    }

    /**
     * Init it on first server use. Kept server only on purpose: the client copy of a missile (spawn data) must stay
     * blocked, it would otherwise run the action and remove itself.
     */
    private void setLoadedPreCheck(ICondition check) {
        this.preCheck = check;
        this.preCheckNeedsInit = check != null;
    }

    private void initLoadedPreCheck() {
        if(preCheckNeedsInit) {
            preCheckNeedsInit = false;
            if(preCheck != null) {
                preCheck.init(fieldProvider);
            }
        }
    }

    @Nonnull
    @Override
    public IActionStatus checkAction(Level world, double x, double y, double z, @Nullable IActionCause cause) {
        if(world != null && !world.isClientSide()) {
            initLoadedPreCheck();
        }

        // Edge case: lazy init failed to get action data
        if(this.getActionData() == null) {
            return ActionResponses.MISSING_ACTION_DATA;
        }
        return preCheck == null ? ActionResponses.READY : preCheck.getCondition();
    }

    public IActionStatus doAction(BlockEntity tile, @Nullable IActionCause cause) {
        final IActionCause selfCause = new CausedByBlock(tile.getLevel(), tile.getBlockPos(), tile.getBlockState()).setPreviousCause(cause);
        return doAction(tile.getLevel(), tile.getBlockPos(), selfCause);
    }

    public IActionStatus doAction(Level world, BlockPos pos, @Nullable IActionCause cause) {
        return doAction(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, cause);
    }

    @Nonnull
    @Override
    public IActionStatus doAction(Level world, double x, double y, double z, @Nullable IActionCause cause) {
        final IActionData actionData = this.getActionData();

        // Edge case: lazy init failed to get action data
        if(actionData == null) {
            return ActionResponses.MISSING_ACTION_DATA;
        }

        final IActionStatus preCheckStatus = checkAction(world, x, y, z, cause); //TODO if preCheck is a special type use in cause-by chain. Example: timer
        if(preCheckStatus.isType(ActionStatusTypes.BLOCKING)) {
            return preCheckStatus;
        }

        IActionCause causeToUse = cause;
        if(preCheck != null) {
            if(preCheck instanceof IConditionCause) {
                final IActionCause preCheckCause = ((IConditionCause) preCheck).getCause();
                if(preCheckCause != null) {
                    causeToUse = preCheckCause.setPreviousCause(cause);
                }
            }
            preCheck.reset();
        }

        final IActionSource actionSource = new ActionSource(world, new Vec3(x, y, z), causeToUse);
        return CreateICBMAPI.ACTION_LISTENER.runAction(actionData.create(world, x, y, z, actionSource, this.fieldProvider));
    }

    @Override
    public void update(int tick, boolean isServer) {
        if(isServer && preCheck != null) {
            initLoadedPreCheck();
            preCheck.onTick();
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<PotentialActionImp> SAVE_LOGIC = new NbtSaveHandler<PotentialActionImp>()
        .mainRoot()
        /* */.nodeBuildableObject("pre_check", () -> CreateICBMAPI.CONDITION_REGISTRY, PotentialActionImp::getPreCheck, PotentialActionImp::setLoadedPreCheck)
        .base();
}
