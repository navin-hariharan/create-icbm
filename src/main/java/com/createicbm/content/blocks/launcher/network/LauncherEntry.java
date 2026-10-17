package com.createicbm.content.blocks.launcher.network;

import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.launcher.IMissileLauncher;
import com.createicbm.content.blocks.launcher.FiringPackage;
import lombok.Data;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;

@Data
public class LauncherEntry {
    private final IMissileLauncher launcher;
    private final BlockEntity host;
    private final Direction side;

    // Internal data, don't use in mods as this will be moved to launcher itself and exposed as an event list
    private IActionStatus lastFiringStatus;
    private FiringPackage lastFiringPackage;
    private long lastFiringTime;
}
