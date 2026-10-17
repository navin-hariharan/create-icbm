package com.createicbm.content.blast.thread;

import com.createicbm.jlib.data.vector.IPos3D;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Deprecated
public interface IThreadCallBack
{
    float getResistance(Level world, IPos3D blastCenter, BlockPos pos, Entity source, BlockState state);
}
