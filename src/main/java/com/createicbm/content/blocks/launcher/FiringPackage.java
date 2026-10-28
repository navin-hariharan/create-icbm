package com.createicbm.content.blocks.launcher;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.launcher.IMissileLauncher;
import com.createicbm.api.actions.cause.IActionCause;
import com.createicbm.api.missiles.parts.IMissileTarget;
import com.createicbm.api.missiles.parts.IMissileTargetDelayed;
import com.createicbm.lib.saving.NbtSaveHandler;
import com.createicbm.lib.tile.ITick;
import lombok.Data;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.function.Consumer;

/**
 * Used to store firing information when working with countdowns/delays
 */
@Data
public class FiringPackage implements INBTSerializable<CompoundTag>, ITick {

    /** Input: Target data */
    private IMissileTarget targetData;

    /** Input: Cause of firing the missile */
    private IActionCause cause;

    /** Counter: Time to tick down before firing */
    private int countDown = -1;

    private boolean hasFired = false;

    private Consumer<IActionStatus> onTriggerCallback;

    public FiringPackage(IMissileTarget targetData, IActionCause cause, int countDown) {
        this.targetData = targetData;
        this.cause = cause;
        this.countDown = countDown;
    }

    public FiringPackage(IMissileTarget targetData, IActionCause cause) {
        this.targetData = targetData;
        this.cause = cause;
    }

    public void launch(IMissileLauncher missileLauncher) {

        if(!hasFired) {
            hasFired = true;

            if (targetData instanceof IMissileTargetDelayed) {
                targetData = ((IMissileTargetDelayed) targetData).cloneWithoutDelay();
            }

            // Invoke normal launch so we fire events and handle logic consistently
            final IActionStatus status = missileLauncher.launch((launcher) -> targetData, cause, false);
            if(onTriggerCallback != null) {
                onTriggerCallback.accept(status);
            }
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this, new CompoundTag());
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<FiringPackage> SAVE_LOGIC = new NbtSaveHandler<FiringPackage>()
        .mainRoot()
        /* */.nodeInteger("countdown", FiringPackage::getCountDown, FiringPackage::setCountDown)
        /* */.nodeBuildableObject("target", () -> CreateICBMAPI.MISSILE_TARGET_DATA_REGISTRY, FiringPackage::getTargetData, FiringPackage::setTargetData)
        /* */.nodeBuildableObject("cause", () -> CreateICBMAPI.ACTION_CAUSE_REGISTRY, FiringPackage::getCause, FiringPackage::setCause)
        .base();

    @Override
    public void update(int tick, boolean isServer) {
        if(isServer && !hasFired) {
            this.countDown--;
            if(countDown <= 0) {
                // TODO launch();
            }
        }
    }
}
