package com.createicbm.api.actions.cause;

import com.createicbm.api.actions.IActionData;

/**
 * Cause containing action information
 */
public interface ICausedByAction extends IActionCause {
    IActionSource getSource();

    IActionData getActionData();
}
