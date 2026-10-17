package com.createicbm.content.blocks.launcher.status;

import com.createicbm.ICBMConstants;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import com.createicbm.content.blocks.launcher.LauncherLangs;
import com.createicbm.lib.buildable.BuildableObject;
import com.createicbm.lib.saving.NbtSaveHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class FiringWithDelay extends BuildableObject<FiringWithDelay, IBuilderRegistry<IActionStatus>> implements IActionStatus {

    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "firing.delayed");

    @Getter @Setter(AccessLevel.PROTECTED)
    private int delay;
    private Component message;

    public FiringWithDelay() {
        super(REG_NAME, CreateICBMAPI.ACTION_STATUS_REGISTRY, SAVE_LOGIC);
    }

    public FiringWithDelay(int delay) {
        this();
        this.delay = delay;
    }

    @Override
    public boolean isError() {
        return false;
    }

    public boolean isBlocking() {
        return true;
    }

    @Override
    public Component message() {
        if(message == null) {
            message = Component.translatable(LauncherLangs.STATUS_FIRING_DELAYED, delay);
        }
        return message;
    }

    private static final NbtSaveHandler<FiringWithDelay> SAVE_LOGIC = new NbtSaveHandler<FiringWithDelay>()
        .mainRoot()
        /* */.nodeInteger("delay", FiringWithDelay::getDelay, FiringWithDelay::setDelay)
        .base();
}
