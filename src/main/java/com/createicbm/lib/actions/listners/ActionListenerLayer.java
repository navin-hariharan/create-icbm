package com.createicbm.lib.actions.listners;

import com.createicbm.CreateICBM;
import com.createicbm.api.actions.IAction;
import com.createicbm.api.actions.listener.IActionListener;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.data.meta.MetaTag;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class ActionListenerLayer {

    private final MetaTag layerTag;

    private final Map<MetaTag, ActionListenerLayer> layers = new HashMap<>();
    private final List<IActionListener> listeners = new ArrayList();

    public IActionStatus onAction(IAction action) {
        for(IActionListener listener : listeners) {
            final IActionStatus status = listener.onAction(action);
            if(status != null) {
                return status;
            }
        }
        for(ActionListenerLayer layer: layers.values()) {
            if(action.getActionData().isType(layer.layerTag)) {
                final IActionStatus status = layer.onAction(action);
                if(status != null) {
                    return status;
                }
            }
        }
        return null;
    }

    public void add(final IActionListener listener) {
        if(listeners.contains(listener)) {
            CreateICBM.logger().error("ActionListenerLayer({}): listener is already added to this layer. Listener: {}", this.layerTag, listener);
            return;
        }
        listeners.add(listener);
    }
    public void add(final IActionListener listener, final MetaTag tag) {
        if(listeners.contains(listener)) {
            CreateICBM.logger().error("ActionListenerLayer({}): listener is already added to this layer. Listener: {}", this.layerTag, listener);
            return;
        }

        // This layer
        if(tag == this.layerTag) {
            listeners.add(listener);
            return;
        }

        // One layer down
        if(tag.getParent() == null || tag.getParent() == this.layerTag) {
            layers.computeIfAbsent(tag, k -> new ActionListenerLayer(tag)).add(listener);
            return;
        }

        // Find next layer tag, will be a child of this.layerTag
        MetaTag parent = tag.getParent();
        while(parent.getParent() != null && parent.getParent() != this.layerTag) {
            parent = parent.getParent();
        }

        if(!layers.containsKey(parent)) {
            layers.put(parent, new ActionListenerLayer(parent));
        }
        layers.get(parent).add(listener, tag);
    }

    @Override
    public String toString() {
        return "ActionListenerLayer(" + layerTag + ")@" + hashCode();
    }
}
