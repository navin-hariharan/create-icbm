package com.createicbm.lib.actions;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.actions.IActionData;
import com.createicbm.api.actions.data.IActionFieldProvider;
import com.createicbm.lib.data.LazyBuilder;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Pre-built action for general purpose implementation where action is known
 */
public final class PotentialActionKnown extends PotentialActionImp<PotentialActionKnown> implements IActionFieldProvider {

    private final LazyBuilder<IActionData> actionData;

    public PotentialActionKnown(ResourceLocation key) {
        this.actionData = new LazyBuilder<>(() -> CreateICBMAPI.ACTION_REGISTRY.getOrBuild(key));
    }

    @Nonnull
    @Override
    public IActionData getActionData() {
        return actionData.get();
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return null;
    }
}
