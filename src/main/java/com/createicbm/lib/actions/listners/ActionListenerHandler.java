package com.createicbm.lib.actions.listners;

import com.createicbm.api.actions.IAction;
import com.createicbm.api.actions.IActionData;
import com.createicbm.api.actions.listener.IActionListener;
import com.createicbm.api.actions.listener.IActionListenerHandler;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.data.meta.MetaTag;
import lombok.NoArgsConstructor;

/**
 * Handles firing events for actions using {@link IActionData#getTypeTags()} as the event channels
 */
@NoArgsConstructor()
public class ActionListenerHandler implements IActionListenerHandler {

    private final ActionListenerLayer root = new ActionListenerLayer(null);

    @Override
    public void addListener(IActionListener listener, MetaTag... tags) {
        for (MetaTag tag : tags) {
            root.add(listener, tag);
        }
    }

    @Override
    public IActionStatus runAction(IAction action) {
        final IActionStatus status = root.onAction(action);
        if(status != null) {
            return status;
        }
        return action.doAction();
    }
}
