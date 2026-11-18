package com.createicbm.api.actions.cause;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.reg.obj.IBuildableObject;
import com.createicbm.api.reg.obj.IBuilderRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Cause of an action. Stored as part of {@link IActionSource}
 * used to track who or what triggered an action. Can be stored as a chain of
 * causes allowing detailed information to be tracked.
 *
 * Example: player -> remote -> screen -> silo -> cluster missile -> missile
 */
public interface IActionCause extends IBuildableObject {

    /**
     * Parent cause in the history
     *
     * @return cause
     */
    @Nullable
    IActionCause getPreviousCause();

    /**
     * Sets cause before this cause. Current should always be end of chain.
     *
     * @param parent to use
     * @return self
     */
    IActionCause setPreviousCause(@Nullable IActionCause parent);

    @Nonnull
    @Override
    default IBuilderRegistry getRegistry() {
        return CreateICBMAPI.ACTION_CAUSE_REGISTRY;
    }
}
