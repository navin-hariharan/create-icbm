package com.createicbm.api.actions.listener;

import com.createicbm.api.actions.IAction;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.data.meta.MetaTag;

/**
 * Event system for actions. Allows for firing actions for listeners to interact.
 */
public interface IActionListenerHandler {

    /**
     * Called to add a listener
     *
     * @param listener to add
     * @param tags to watch
     */
    void addListener(IActionListener listener, MetaTag... tags);

    /**
     * Called by {@link com.createicbm.api.actions.IPotentialAction} or other systems to run
     * an action. It is expected all created actions go through this before {@link IAction#doAction()}
     * is invoked. Otherwise, external systems and mods may not have the chance to interact. Which is
     * critical for anything editing the world or creating an impact breaking protection mods.
     *
     * @param action created and ready to fire
     * @return status from the action or listener if result was changed.
     */
    IActionStatus runAction(IAction action);
}
