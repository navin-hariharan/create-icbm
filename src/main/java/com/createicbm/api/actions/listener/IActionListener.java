package com.createicbm.api.actions.listener;

import com.createicbm.api.actions.IAction;
import com.createicbm.api.actions.status.IActionStatus;

import javax.annotation.Nullable;

@FunctionalInterface
public interface IActionListener {

    /**
     * Called when action is created but not yet run. This allows for
     * blocking the action, modifying the action, or replacing the action.
     *
     * To block an action return any status, even if {@link com.createicbm.api.actions.status.ActionStatusTypes#GREEN}.
     * This will be returned in place of invoking the action. As it is assumed any status
     * means the listener acted on the action.
     *
     * Modifying is not recommended due to lack of visibility into the edit. As well
     * something upstream may need the data included. If desired action can be
     * instanceof checked or updated via the field provider system.
     *
     * Better option is to replace the action. This can be done by returning a status
     * that notes the replacement. Then firing a new action off back into the listener
     * system. Ensuring that this listener doesn't spawn another action from the same
     * one generated.
     *
     * @param action created, can be modified if needed
     *
     * @return null to do nothing, status to block the action and note why
     */
    @Nullable
    IActionStatus onAction(IAction action);
}
