package com.createicbm.content.blocks.launcher.connector;

import com.createicbm.content.blocks.launcher.frame.TileLauncherFrame;
import com.createicbm.content.reg.BlockReg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileLauncherConnector extends TileLauncherFrame {

    public TileLauncherConnector(BlockPos pos, BlockState state)
    {
        super(BlockReg.TILE_LAUNCHER_CONNECTOR.get(), pos, state);
    }
}
