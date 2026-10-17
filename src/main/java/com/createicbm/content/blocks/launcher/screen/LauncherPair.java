package com.createicbm.content.blocks.launcher.screen;

import com.createicbm.api.actions.status.IActionStatus;
import lombok.Data;

@Data
public class LauncherPair {
    private final Integer group;
    private final Integer index;
    private final IActionStatus status;
}
